package com.uzuu.diuchat.feature.calls.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog

enum class CallHistoryFilter {
    ALL,
    MISSED,
}

data class CallHistoryUiState(
    val callLogs: List<CallLog> = emptyList(),
    val filter: CallHistoryFilter = CallHistoryFilter.ALL,
    val isLoading: Boolean = false,
) : BaseState {
    val filteredLogs: List<CallLog>
        get() = when (filter) {
            CallHistoryFilter.ALL -> callLogs
            CallHistoryFilter.MISSED -> callLogs.filter { it.endReason == CallEndReason.MISSED }
        }
}
