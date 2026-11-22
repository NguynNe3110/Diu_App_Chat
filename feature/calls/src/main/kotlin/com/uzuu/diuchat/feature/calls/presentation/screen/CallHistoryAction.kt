package com.uzuu.diuchat.feature.calls.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.calls.domain.model.CallLog

private const val MAX_FAVORITE_CALLERS = 5
private val FAVORITE_MOOD_PALETTE = listOf("#F7A3A9", "#FCD36D", "#7ED9B4", "#C4B5FD", "#94A3B8")

sealed class CallHistoryAction : BaseAction<CallHistoryUiState> {
    data class SetCallLogs(val logs: List<CallLog>) : CallHistoryAction() {
        override fun reduce(state: CallHistoryUiState): CallHistoryUiState {
            val derivedFavorites = logs
                .distinctBy { it.peerId }
                .take(MAX_FAVORITE_CALLERS)
                .mapIndexed { index, log ->
                    FavoriteCaller(
                        peerId = log.peerId,
                        name = log.peerName,
                        initials = log.peerName.trim().take(1).uppercase().ifEmpty { "?" },
                        moodHex = log.peerMoodHex.ifBlank {
                            FAVORITE_MOOD_PALETTE[index % FAVORITE_MOOD_PALETTE.size]
                        },
                        avatarUrl = log.peerAvatarUrl,
                    )
                }
            return state.copy(
                callLogs = logs,
                favoriteCallers = derivedFavorites,
                isLoading = false,
            )
        }
    }

    data class SetFilter(val filter: CallHistoryFilter) : CallHistoryAction() {
        override fun reduce(state: CallHistoryUiState): CallHistoryUiState = state.copy(filter = filter)
    }

    data class SetLoading(val loading: Boolean) : CallHistoryAction() {
        override fun reduce(state: CallHistoryUiState): CallHistoryUiState = state.copy(isLoading = loading)
    }
}
