package com.uzuu.diuchat.feature.calls.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.calls.domain.model.CallLog

sealed class CallHistoryAction : BaseAction<CallHistoryUiState> {
    data class SetCallLogs(val logs: List<CallLog>) : CallHistoryAction() {
        override fun reduce(state: CallHistoryUiState): CallHistoryUiState = state.copy(callLogs = logs, isLoading = false)
    }

    data class SetFilter(val filter: CallHistoryFilter) : CallHistoryAction() {
        override fun reduce(state: CallHistoryUiState): CallHistoryUiState = state.copy(filter = filter)
    }

    data class SetLoading(val loading: Boolean) : CallHistoryAction() {
        override fun reduce(state: CallHistoryUiState): CallHistoryUiState = state.copy(isLoading = loading)
    }
}
