package com.uzuu.diuchat.feature.calls.data.repository

import com.uzuu.diuchat.feature.calls.data.CallServiceCoordinator
import com.uzuu.diuchat.feature.calls.data.WebRtcSignalingAdapter
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val callServiceCoordinator: CallServiceCoordinator = mockk(relaxed = true)
    private val webRtcSignalingAdapter: WebRtcSignalingAdapter = mockk(relaxed = true)

    lateinit var sut: CallRepositoryImpl

    @BeforeEach
    fun setUp() {
        sut = CallRepositoryImpl(
            callServiceCoordinator = callServiceCoordinator,
            webRtcSignalingAdapter = webRtcSignalingAdapter,
            coroutineDispatcher = testDispatcher,
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
}
