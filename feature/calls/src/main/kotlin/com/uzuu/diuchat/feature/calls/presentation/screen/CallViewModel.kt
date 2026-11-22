package com.uzuu.diuchat.feature.calls.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import com.uzuu.diuchat.feature.calls.domain.usecase.AcceptCallUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.EndCallUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.StartCallUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class CallViewModel(
    private val acceptCallUseCase: AcceptCallUseCase,
    private val callRepository: CallRepository,
    private val endCallUseCase: EndCallUseCase,
    private val startCallUseCase: StartCallUseCase,
) : BaseViewModel<CallUiState, CallAction>(CallUiState()) {

    init {
        observeCallSession()
    }

    private fun observeCallSession() {
        callRepository.currentCallSession
            .onEach { session ->
                sendAction(CallAction.UpdateSession(session))
            }
            .launchIn(viewModelScope)
    }

    fun initializeCallFromRoute(
        peerId: String = "minh",
        peerName: String = "Minh",
        peerMoodHex: String = "#F7A3A9",
        isVideo: Boolean = false,
        isIncoming: Boolean = false,
    ) {
        ensureCallSession(
            peerId = peerId,
            peerName = peerName,
            peerMoodHex = peerMoodHex,
            isVideo = isVideo,
            isIncoming = isIncoming,
        )
    }

    fun ensureCallSession(
        peerId: String = "minh",
        peerName: String = "Minh",
        peerMoodHex: String = "#F7A3A9",
        isVideo: Boolean = false,
        isIncoming: Boolean = false,
    ) {
        sendAction(
            CallAction.SetPeerMetadata(
                peerId = peerId,
                peerName = peerName,
                peerMoodHex = peerMoodHex,
            ),
        )
        val current = uiStateFlow.value.callSession ?: callRepository.currentCallSession.value
        val desiredType = if (isVideo) CallType.VIDEO else CallType.AUDIO
        val isInactiveSession = current == null ||
            current.status == CallStatus.IDLE ||
            current.status == CallStatus.ENDED

        if (isIncoming) {
            if (isInactiveSession || !current.isIncoming) {
                val incomingSession = CallSession(
                    callId = "call_in_$peerId",
                    callerId = peerId,
                    callerName = peerName,
                    calleeId = "user_me",
                    calleeName = "Tôi",
                    status = CallStatus.RINGING,
                    type = desiredType,
                    isIncoming = true,
                    isSpeakerOn = isVideo,
                    isVideoEnabled = isVideo,
                )
                sendAction(CallAction.UpdateSession(incomingSession))
            }
        } else if (isInactiveSession) {
            startOutgoingCall(
                calleeId = peerId,
                calleeName = peerName,
                callType = desiredType,
            )
        }
    }

    fun startOutgoingCall(calleeId: String, calleeName: String, callType: CallType) {
        viewModelScope.launch {
            startCallUseCase(calleeId, calleeName, callType)
        }
    }

    fun acceptIncomingCall() {
        val session = uiStateFlow.value.callSession ?: return
        viewModelScope.launch {
            acceptCallUseCase(session.callId)
        }
    }

    fun rejectIncomingCall() {
        val session = uiStateFlow.value.callSession ?: return
        viewModelScope.launch {
            endCallUseCase(session.callId, CallEndReason.REJECTED)
        }
    }

    fun toggleQuickReplyOptions() {
        sendAction(CallAction.SetQuickReplyExpanded(!uiStateFlow.value.isQuickReplyExpanded))
    }

    fun sendQuickReplyAndDecline(message: String) {
        sendAction(CallAction.RecordQuickReply(message))
        val session = uiStateFlow.value.callSession ?: return
        viewModelScope.launch {
            endCallUseCase(session.callId, CallEndReason.REJECTED)
        }
    }

    fun scheduleCallReminder() {
        sendAction(CallAction.SetReminderScheduled(true))
        val session = uiStateFlow.value.callSession ?: return
        viewModelScope.launch {
            endCallUseCase(session.callId, CallEndReason.REJECTED)
        }
    }

    fun hangUpCall() {
        val session = uiStateFlow.value.callSession ?: return
        viewModelScope.launch {
            endCallUseCase(session.callId, CallEndReason.COMPLETED)
        }
    }

    fun toggleMicrophone() {
        val session = uiStateFlow.value.callSession ?: return
        viewModelScope.launch {
            val result = callRepository.toggleMic(session.callId)
            result.onSuccess { sendAction(CallAction.SetMuted(it)) }
        }
    }

    fun toggleSpeakerphone() {
        val session = uiStateFlow.value.callSession ?: return
        viewModelScope.launch {
            val result = callRepository.toggleSpeaker(session.callId)
            result.onSuccess { sendAction(CallAction.SetSpeaker(it)) }
        }
    }

    fun toggleCameraVideo() {
        val session = uiStateFlow.value.callSession ?: return
        viewModelScope.launch {
            val result = callRepository.toggleCamera(session.callId)
            result.onSuccess { sendAction(CallAction.SetVideoEnabled(it)) }
        }
    }

    fun flipCamera() {
        val session = uiStateFlow.value.callSession ?: return
        viewModelScope.launch {
            val result = callRepository.switchCamera(session.callId)
            result.onSuccess { sendAction(CallAction.SetFrontCamera(it)) }
        }
    }

    fun toggleOverlayMinimized() {
        sendAction(CallAction.ToggleOverlay(!uiStateFlow.value.isOverlayMinimized))
    }
}
