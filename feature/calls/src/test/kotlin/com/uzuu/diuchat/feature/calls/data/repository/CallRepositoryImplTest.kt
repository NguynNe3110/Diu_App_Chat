package com.uzuu.diuchat.feature.calls.data.repository

import com.uzuu.diuchat.feature.base.domain.media.AppSoundPlayer
import com.uzuu.diuchat.feature.base.domain.media.DiuSoundEffect
import com.uzuu.diuchat.feature.calls.data.CallMediaEngine
import com.uzuu.diuchat.feature.calls.data.CallServiceCoordinator
import com.uzuu.diuchat.feature.calls.data.CallSupabaseService
import com.uzuu.diuchat.feature.calls.data.CallTokenService
import com.uzuu.diuchat.feature.calls.data.LiveKitTokenPayload
import com.uzuu.diuchat.feature.calls.data.WebRtcSignalingAdapter
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.model.RtcSignalingMessage
import com.uzuu.diuchat.feature.calls.domain.model.SignalingType
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class CallRepositoryImplTest {

    @JvmField
    @RegisterExtension
    val coroutinesTestDispatcherExtension = CoroutinesTestDispatcherExtension()

    private val testDispatcher = StandardTestDispatcher()
    private val signalingFlow = MutableSharedFlow<RtcSignalingMessage>(extraBufferCapacity = 16)
    private val callServiceCoordinator: CallServiceCoordinator = mockk(relaxed = true)
    private val webRtcSignalingAdapter: WebRtcSignalingAdapter = mockk(relaxed = true) {
        every { signalingEvents } returns signalingFlow
    }
    private val callMediaEngine: CallMediaEngine = mockk(relaxed = true)
    private val appSoundPlayer: AppSoundPlayer = mockk(relaxed = true)
    private val callTokenService: CallTokenService = mockk(relaxed = true) {
        coEvery { fetchToken(any()) } returns Result.success(
            LiveKitTokenPayload(token = "lk_token", url = "wss://livekit.example.com"),
        )
    }
    private val callSupabaseService: CallSupabaseService = mockk(relaxed = true) {
        every { isConfigured() } returns false
        every { currentUserId() } returns "user_me"
        every { currentUserDisplayName() } returns "Tôi"
        coEvery { createCallLog(any(), any(), any(), any()) } answers { arg(3) }
    }

    lateinit var sut: CallRepositoryImpl

    @BeforeEach
    fun setUp() {
        sut = CallRepositoryImpl(
            callServiceCoordinator = callServiceCoordinator,
            webRtcSignalingAdapter = webRtcSignalingAdapter,
            coroutineDispatcher = testDispatcher,
            callTokenService = callTokenService,
            callMediaEngine = callMediaEngine,
            callSupabaseService = callSupabaseService,
            ringingTimeoutMs = 45_000L,
            appSoundPlayer = appSoundPlayer,
        )
    }

    @Test
    fun `startCall transitions to INVITING and notifies coordinator and signaling`() = runTest {
        val result = sut.startCall("peer_1", "Linh", CallType.AUDIO)

        result.isSuccess shouldBeEqualTo true
        val session = result.getOrNull().shouldNotBeNull()
        session.status shouldBeEqualTo CallStatus.INVITING
        session.calleeName shouldBeEqualTo "Linh"

        verify { callServiceCoordinator.startCallService(session.callId, "Linh", false) }
    }

    @Test
    fun `acceptCall from INVITING transitions to ACTIVE`() = runTest {
        val startResult = sut.startCall("peer_1", "Linh", CallType.AUDIO)
        val session = startResult.getOrNull().shouldNotBeNull()

        val acceptResult = sut.acceptCall(session.callId)
        acceptResult.isSuccess shouldBeEqualTo true
        acceptResult.getOrNull()?.status shouldBeEqualTo CallStatus.ACTIVE
    }

    @Test
    fun `endCall transitions to ENDED and appends call log`() = runTest {
        val startResult = sut.startCall("peer_1", "Linh", CallType.AUDIO)
        val session = startResult.getOrNull().shouldNotBeNull()

        val endResult = sut.endCall(session.callId, CallEndReason.COMPLETED)
        endResult.isSuccess shouldBeEqualTo true

        val current = sut.currentCallSession.value.shouldNotBeNull()
        current.status shouldBeEqualTo CallStatus.ENDED
        current.endReason shouldBeEqualTo CallEndReason.COMPLETED

        val logs = sut.getCallLogs().first()
        logs.any { it.callId == session.callId && it.endReason == CallEndReason.COMPLETED } shouldBeEqualTo true
        verify { callServiceCoordinator.stopCallService() }
    }

    @Test
    fun `monotonic check - cannot transition from ENDED to ACTIVE`() = runTest {
        val startResult = sut.startCall("peer_1", "Linh", CallType.AUDIO)
        val session = startResult.getOrNull().shouldNotBeNull()

        sut.endCall(session.callId, CallEndReason.COMPLETED)

        val secondAccept = sut.acceptCall(session.callId)
        secondAccept.isFailure shouldBeEqualTo true
    }

    @Test
    fun `toggle controls update session state correctly`() = runTest {
        val startResult = sut.startCall("peer_1", "Linh", CallType.AUDIO)
        val session = startResult.getOrNull().shouldNotBeNull()

        val micResult = sut.toggleMic(session.callId)
        micResult.getOrNull() shouldBeEqualTo true
        sut.currentCallSession.value?.isAudioMuted shouldBeEqualTo true

        val speakerResult = sut.toggleSpeaker(session.callId)
        speakerResult.getOrNull() shouldBeEqualTo true
        sut.currentCallSession.value?.isSpeakerOn shouldBeEqualTo true

        val videoResult = sut.toggleCamera(session.callId)
        videoResult.getOrNull() shouldBeEqualTo true
        sut.currentCallSession.value?.isVideoEnabled shouldBeEqualTo true
    }

    @Test
    fun `acceptCall sends ANSWER signaling and rejectCall sends DECLINE signaling`() = runTest {
        val session1 = sut.startCall("peer_1", "Linh", CallType.AUDIO).getOrNull().shouldNotBeNull()
        sut.acceptCall(session1.callId)

        coVerify {
            webRtcSignalingAdapter.sendSignaling(
                match { it.sessionId == session1.callId && it.type == SignalingType.ANSWER },
            )
        }

        sut.endCall(session1.callId, CallEndReason.COMPLETED)

        val session2 = sut.startCall("peer_2", "Minh", CallType.VIDEO).getOrNull().shouldNotBeNull()
        sut.rejectCall(session2.callId, CallEndReason.REJECTED)

        coVerify {
            webRtcSignalingAdapter.sendSignaling(
                match { it.sessionId == session2.callId && it.type == SignalingType.DECLINE },
            )
        }
    }

    @Test
    fun `incoming ANSWER transitions INVITING call to ACTIVE and remote BYE ends call`() = runTest(testDispatcher) {
        val session = sut.startCall("peer_1", "Linh", CallType.AUDIO).getOrNull().shouldNotBeNull()
        session.status shouldBeEqualTo CallStatus.INVITING

        signalingFlow.emit(
            RtcSignalingMessage(
                sessionId = session.callId,
                senderId = "remote_peer",
                targetId = "local_peer",
                type = SignalingType.ANSWER,
            ),
        )
        testDispatcher.scheduler.runCurrent()

        sut.currentCallSession.value?.status shouldBeEqualTo CallStatus.ACTIVE

        signalingFlow.emit(
            RtcSignalingMessage(
                sessionId = session.callId,
                senderId = "remote_peer",
                targetId = "local_peer",
                type = SignalingType.BYE,
            ),
        )
        testDispatcher.scheduler.runCurrent()

        sut.currentCallSession.value?.status shouldBeEqualTo CallStatus.ENDED
        sut.currentCallSession.value?.endReason shouldBeEqualTo CallEndReason.COMPLETED
    }

    @Test
    fun `startCall creates call log and unanswered call times out after 45s`() = runTest(testDispatcher) {
        val session = sut.startCall("peer_uuid", "Minh", CallType.VIDEO, "conv_uuid").getOrNull().shouldNotBeNull()

        coVerify(exactly = 1) {
            callSupabaseService.createCallLog(session.callId, "peer_uuid", true, "conv_uuid")
        }

        testDispatcher.scheduler.advanceTimeBy(45_001L)
        testDispatcher.scheduler.runCurrent()

        sut.currentCallSession.value?.status shouldBeEqualTo CallStatus.ENDED
        sut.currentCallSession.value?.endReason shouldBeEqualTo CallEndReason.MISSED
        coVerify(exactly = 1) {
            callMediaEngine.disconnect()
            callSupabaseService.updateCallLog(session.callId, CallEndReason.MISSED, 0L, null)
        }
    }

    @Test
    fun `onIncomingCallReceived connects signaling and acceptCall joins LiveKit room`() = runTest(testDispatcher) {
        val incoming = CallSession(
            callId = "real_call_uuid",
            callerId = "caller_uuid",
            callerName = "An",
            calleeId = "user_me",
            calleeName = "Tôi",
            status = CallStatus.RINGING,
            type = CallType.VIDEO,
            isIncoming = true,
            conversationId = "conv_uuid",
        )

        sut.onIncomingCallReceived(incoming)
        verify { appSoundPlayer.startLooping(DiuSoundEffect.CALL_INCOMING_RINGTONE) }
        coVerify { webRtcSignalingAdapter.connect("real_call_uuid") }

        sut.acceptCall("real_call_uuid")
        testDispatcher.scheduler.runCurrent()

        verify { appSoundPlayer.stopLooping() }
        sut.currentCallSession.value?.status shouldBeEqualTo CallStatus.ACTIVE
        coVerify {
            callTokenService.fetchToken("real_call_uuid")
            callMediaEngine.connect("wss://livekit.example.com", "lk_token", true)
        }

        sut.syncMediaPermissions(micGranted = true, cameraGranted = true)
        coVerify {
            callMediaEngine.setMicrophoneMuted(false)
            callMediaEngine.setCameraEnabled(true)
        }

        sut.endCall("real_call_uuid", CallEndReason.COMPLETED)
        testDispatcher.scheduler.runCurrent()
    }
}
