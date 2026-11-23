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
        peerId: String = "",
        peerName: String = "",
        peerMoodHex: String = "#F7A3A9",
        isVideo: Boolean = false,
        isIncoming: Boolean = false,
        callId: String = "",
        conversationId: String = "",
    ) {
        ensureCallSession(
            peerId = peerId,
            peerName = peerName,
            peerMoodHex = peerMoodHex,
            isVideo = isVideo,
            isIncoming = isIncoming,
            callId = callId,
            conversationId = conversationId,
        )
    }

    @Suppress("LongParameterList")
    fun ensureCallSession(
        peerId: String = "",
        peerName: String = "",
        peerMoodHex: String = "#F7A3A9",
        isVideo: Boolean = false,
        isIncoming: Boolean = false,
        callId: String = "",
        conversationId: String = "",
    ) {
        sendAction(
            CallAction.SetPeerMetadata(
                peerId = peerId,
                peerName = peerName,
                peerMoodHex = peerMoodHex,
            ),
        )
        val current = callRepository.currentCallSession.value ?: uiStateFlow.value.callSession
        val desiredType = if (isVideo) CallType.VIDEO else CallType.AUDIO
        val isInactiveSession = current == null ||
            current.status == CallStatus.IDLE ||
            current.status == CallStatus.ENDED

        if (isIncoming) {
            val matchesActiveIncoming = !isInactiveSession &&
                current.isIncoming &&
                (callId.isBlank() || current.callId == callId)
            if (matchesActiveIncoming) {
                sendAction(CallAction.UpdateSession(current))
            } else if (callId.isNotBlank()) {
                val incomingSession = CallSession(
                    callId = callId,
                    callerId = peerId,
                    callerName = peerName,
                    calleeId = "user_me",
                    calleeName = "Tôi",
                    status = CallStatus.RINGING,
                    type = desiredType,
                    isIncoming = true,
                    isSpeakerOn = isVideo,
                    isVideoEnabled = isVideo,
                    conversationId = conversationId,
                    peerMoodHex = peerMoodHex,
                )
                viewModelScope.launch {
                    callRepository.onIncomingCallReceived(incomingSession)
                }
            }
        } else if (isInactiveSession) {
            startOutgoingCall(
                calleeId = peerId,
                calleeName = peerName,
                callType = desiredType,
                conversationId = conversationId,
            )
        }
    }

    fun startOutgoingCall(
        calleeId: String,
        calleeName: String,
        callType: CallType,
        conversationId: String = "",
    ) {
        viewModelScope.launch {
            startCallUseCase(
                calleeId = calleeId,
                calleeName = calleeName,
                callType = callType,
                conversationId = conversationId,
            )
        }
    }

    fun onPermissionsResult(
        micGranted: Boolean,
        cameraGranted: Boolean,
    ) {
        viewModelScope.launch {
            callRepository.syncMediaPermissions(
                micGranted = micGranted,
                cameraGranted = cameraGranted,
            )
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
            endCallUseCase(session.callId, CallEndReason.REJECTED, message)
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
