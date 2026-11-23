package com.uzuu.diuchat.feature.calls.presentation.screen

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import com.uzuu.diuchat.feature.calls.domain.usecase.AcceptCallUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.EndCallUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.StartCallUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class CallViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutinesTestDispatcherExtension = CoroutinesTestDispatcherExtension()

    private val acceptCallUseCase: AcceptCallUseCase = mockk(relaxed = true)
    private val callRepository: CallRepository = mockk(relaxed = true)
    private val endCallUseCase: EndCallUseCase = mockk(relaxed = true)
    private val startCallUseCase: StartCallUseCase = mockk(relaxed = true)

    private val callSessionFlow = MutableStateFlow<CallSession?>(null)

    lateinit var sut: CallViewModel

    @BeforeEach
    fun setUp() {
        every { callRepository.currentCallSession } returns callSessionFlow

        sut = CallViewModel(
            acceptCallUseCase = acceptCallUseCase,
            callRepository = callRepository,
            endCallUseCase = endCallUseCase,
            startCallUseCase = startCallUseCase,
        )
    }

    @Test
    fun `initial state reflects null session`() = runTest {
        advanceUntilIdle()
        sut.uiStateFlow.value.callSession shouldBeEqualTo null
        sut.uiStateFlow.value.formattedDuration shouldBeEqualTo "00:00"
    }

    @Test
    fun `observing session update updates ui state and duration`() = runTest {
        val session = CallSession(
            callId = "call_test_1",
            callerId = "user_me",
            callerName = "Tôi",
            calleeId = "user_linh",
            calleeName = "Linh",
            status = CallStatus.ACTIVE,
            type = CallType.VIDEO,
            durationSeconds = 125L,
            isVideoEnabled = true,
        )
        callSessionFlow.value = session
        advanceUntilIdle()

        sut.uiStateFlow.value.callSession shouldBeEqualTo session
        sut.uiStateFlow.value.formattedDuration shouldBeEqualTo "02:05"
        sut.uiStateFlow.value.isVideoEnabled shouldBeEqualTo true
    }

    @Test
    fun `startOutgoingCall triggers StartCallUseCase`() = runTest {
        sut.startOutgoingCall("user_linh", "Linh", CallType.AUDIO)
        advanceUntilIdle()

        coVerify { startCallUseCase("user_linh", "Linh", CallType.AUDIO) }
    }

    @Test
    fun `initializeCallFromRoute sets peer metadata and starts outgoing call when no active session`() = runTest {
        sut.initializeCallFromRoute(
            peerId = "thao",
            peerName = "Thảo",
            peerMoodHex = "#C5B3EA",
            isVideo = true,
            isIncoming = false,
        )
        advanceUntilIdle()

        sut.uiStateFlow.value.peerId shouldBeEqualTo "thao"
        sut.uiStateFlow.value.peerName shouldBeEqualTo "Thảo"
        sut.uiStateFlow.value.peerMoodHex shouldBeEqualTo "#C5B3EA"
        coVerify { startCallUseCase("thao", "Thảo", CallType.VIDEO) }
    }

    @Test
    fun `acceptIncomingCall triggers AcceptCallUseCase`() = runTest {
        val incomingSession = CallSession(
            callId = "call_inc",
            callerId = "user_linh",
            callerName = "Linh",
            calleeId = "user_me",
            calleeName = "Tôi",
            status = CallStatus.RINGING,
            isIncoming = true,
        )
        callSessionFlow.value = incomingSession
        advanceUntilIdle()

        sut.acceptIncomingCall()
        advanceUntilIdle()

        coVerify { acceptCallUseCase("call_inc") }
    }

    @Test
    fun `rejectIncomingCall triggers EndCallUseCase with REJECTED reason`() = runTest {
        val incomingSession = CallSession(
            callId = "call_inc",
            callerId = "user_linh",
            callerName = "Linh",
            calleeId = "user_me",
            calleeName = "Tôi",
            status = CallStatus.RINGING,
            isIncoming = true,
        )
        callSessionFlow.value = incomingSession
        advanceUntilIdle()

        sut.rejectIncomingCall()
        advanceUntilIdle()

        coVerify { endCallUseCase("call_inc", CallEndReason.REJECTED) }
    }

    @Test
    fun `sendQuickReplyAndDecline records quick reply message and ends call with REJECTED`() = runTest {
        val incomingSession = CallSession(
            callId = "call_inc_qr",
            callerId = "minh",
            callerName = "Minh",
            calleeId = "me",
            calleeName = "Tôi",
            status = CallStatus.RINGING,
            isIncoming = true,
        )
        callSessionFlow.value = incomingSession
        advanceUntilIdle()

        sut.toggleQuickReplyOptions()
        sut.uiStateFlow.value.isQuickReplyExpanded shouldBeEqualTo true

        sut.sendQuickReplyAndDecline("Thương, tí gọi lại")
        advanceUntilIdle()

        sut.uiStateFlow.value.lastQuickReplyMessage shouldBeEqualTo "Thương, tí gọi lại"
        sut.uiStateFlow.value.isQuickReplyExpanded shouldBeEqualTo false
        coVerify { endCallUseCase("call_inc_qr", CallEndReason.REJECTED, "Thương, tí gọi lại") }
    }

    @Test
    fun `hangUpCall triggers EndCallUseCase with COMPLETED reason`() = runTest {
        val activeSession = CallSession(
            callId = "call_active",
            callerId = "user_me",
            callerName = "Tôi",
            calleeId = "user_linh",
            calleeName = "Linh",
            status = CallStatus.ACTIVE,
        )
        callSessionFlow.value = activeSession
        advanceUntilIdle()

        sut.hangUpCall()
        advanceUntilIdle()

        coVerify { endCallUseCase("call_active", CallEndReason.COMPLETED) }
    }

    @Test
    fun `toggleMicrophone delegates to repository and updates ui state`() = runTest {
        val session = CallSession(
            callId = "call_mic",
            callerId = "user_me",
            callerName = "Tôi",
            calleeId = "user_linh",
            calleeName = "Linh",
            status = CallStatus.ACTIVE,
        )
        callSessionFlow.value = session
        coEvery { callRepository.toggleMic("call_mic") } returns Result.success(true)
        advanceUntilIdle()

        sut.toggleMicrophone()
        advanceUntilIdle()

        sut.uiStateFlow.value.isMuted shouldBeEqualTo true
    }

    @Test
    fun `toggleOverlayMinimized toggles boolean flag`() = runTest {
        sut.uiStateFlow.value.isOverlayMinimized shouldBeEqualTo false

        sut.toggleOverlayMinimized()
        sut.uiStateFlow.value.isOverlayMinimized shouldBeEqualTo true

        sut.toggleOverlayMinimized()
        sut.uiStateFlow.value.isOverlayMinimized shouldBeEqualTo false
    }

    @Test
    fun `ensureCallSession for incoming call keeps real callId from repository and onPermissionsResult syncs media`() = runTest {
        val realIncoming = CallSession(
            callId = "real_uuid_123",
            callerId = "caller_uuid",
            callerName = "Linh",
            calleeId = "user_me",
            calleeName = "Tôi",
            status = CallStatus.RINGING,
            type = CallType.VIDEO,
            isIncoming = true,
        )
        callSessionFlow.value = realIncoming
        advanceUntilIdle()

        sut.initializeCallFromRoute(
            callId = "real_uuid_123",
            peerId = "caller_uuid",
            peerName = "Linh",
            isVideo = true,
            isIncoming = true,
        )
        advanceUntilIdle()

        sut.uiStateFlow.value.callSession?.callId shouldBeEqualTo "real_uuid_123"

        sut.onPermissionsResult(micGranted = true, cameraGranted = true)
        advanceUntilIdle()

        coVerify(exactly = 1) { callRepository.syncMediaPermissions(true, true) }
    }
}
