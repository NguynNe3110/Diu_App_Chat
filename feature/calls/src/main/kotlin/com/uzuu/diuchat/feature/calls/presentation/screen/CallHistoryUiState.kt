package com.uzuu.diuchat.feature.calls.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog

enum class CallHistoryFilter {
    ALL,
    MISSED,
}

data class FavoriteCaller(
    val peerId: String,
    val name: String,
    val initials: String,
    val moodHex: String,
) {
    val moodColorHex: String
        get() = moodHex
}

fun defaultFavoriteCallers(): List<FavoriteCaller> = listOf(
    FavoriteCaller(peerId = "minh", name = "Minh", initials = "M", moodHex = "#F7A3A9"),
    FavoriteCaller(peerId = "me_yeu", name = "Mẹ", initials = "M", moodHex = "#F4CA84"),
    FavoriteCaller(peerId = "thao", name = "Thảo", initials = "T", moodHex = "#C5B3EA"),
    FavoriteCaller(peerId = "ba", name = "Ba", initials = "B", moodHex = "#A2D7AC"),
)

data class CallHistoryUiState(
    val callLogs: List<CallLog> = emptyList(),
    val filter: CallHistoryFilter = CallHistoryFilter.ALL,
    val isLoading: Boolean = false,
    val favoriteCallers: List<FavoriteCaller> = emptyList(),
) : BaseState {
    val filteredLogs: List<CallLog>
        get() = when (filter) {
            CallHistoryFilter.ALL -> callLogs
            CallHistoryFilter.MISSED -> callLogs.filter { it.endReason == CallEndReason.MISSED }
        }

    val groupedLogsByDate: Map<String, List<CallLog>>
        get() {
            val logs = filteredLogs
            if (logs.isEmpty()) return emptyMap()
            val grouped = linkedMapOf<String, MutableList<CallLog>>()
            val newestTimestamp = logs.maxOf { it.timestampEpochMs }
            logs.forEachIndexed { index, log ->
                val ageMs = (newestTimestamp - log.timestampEpochMs).coerceAtLeast(0L)
                val section = when {
                    ageMs >= THRESHOLD_TUESDAY_MS -> "THỨ BA"
                    ageMs >= THRESHOLD_YESTERDAY_MS -> "HÔM QUA"
                    index == 0 -> "HÔM NAY"
                    index == 1 -> "HÔM QUA"
                    else -> "THỨ BA"
                }
                grouped.getOrPut(section) { mutableListOf() }.add(log)
            }
            return grouped
        }

    companion object {
        private const val HOUR_MS = 3_600_000L
        private const val THRESHOLD_YESTERDAY_MS = 12 * HOUR_MS
        private const val THRESHOLD_TUESDAY_MS = 36 * HOUR_MS
    }
}
