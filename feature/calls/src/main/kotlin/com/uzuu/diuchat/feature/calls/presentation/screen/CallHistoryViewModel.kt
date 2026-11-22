package com.uzuu.diuchat.feature.calls.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.usecase.GetCallHistoryUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.StartCallUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CallHistoryViewModel(
    private val getCallHistoryUseCase: GetCallHistoryUseCase,
    private val startCallUseCase: StartCallUseCase,
) : BaseViewModel<CallHistoryUiState, CallHistoryAction>(CallHistoryUiState()) {

    init {
        observeCallHistory()
    }

    private fun observeCallHistory() {
        getCallHistoryUseCase()
            .onEach { logs ->
                sendAction(CallHistoryAction.SetCallLogs(logs))
            }
            .launchIn(viewModelScope)
    }

    fun setFilter(filter: CallHistoryFilter) {
        sendAction(CallHistoryAction.SetFilter(filter))
    }

    fun startFavoriteCall(
        caller: FavoriteCaller,
        callType: CallType = CallType.AUDIO,
    ) {
        viewModelScope.launch {
            startCallUseCase(
                calleeId = caller.peerId,
                calleeName = caller.name,
                callType = callType,
            )
        }
    }

    fun startCallToFavorite(
        favorite: FavoriteCaller,
        callType: CallType = CallType.AUDIO,
    ) {
        startFavoriteCall(caller = favorite, callType = callType)
    }

    fun redial(callLog: CallLog) {
        viewModelScope.launch {
            startCallUseCase(
                calleeId = callLog.peerId,
                calleeName = callLog.peerName,
                callType = callLog.type,
            )
        }
    }
}
