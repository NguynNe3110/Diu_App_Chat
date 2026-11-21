package com.uzuu.diuchat.feature.calls.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import com.uzuu.diuchat.feature.calls.domain.usecase.AcceptCallUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.EndCallUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.StartCallUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
