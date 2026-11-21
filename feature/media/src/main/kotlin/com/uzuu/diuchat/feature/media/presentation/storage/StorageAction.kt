package com.uzuu.diuchat.feature.media.presentation.storage

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.media.domain.model.StorageStats

sealed interface StorageAction : BaseAction<StorageUiState> {

    data class SetLoading(val isLoading: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState =
            state.copy(isLoading = isLoading)
    }

    data class SetStats(val stats: StorageStats) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState =
            state.copy(isLoading = false, stats = stats)
    }

    data class ToggleAutoDownloadWifi(val enabled: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState =
            state.copy(autoDownloadWifi = enabled)
    }

    data class ToggleAutoDownloadVoice(val enabled: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState =
            state.copy(autoDownloadVoice = enabled)
    }

    data class ToggleKeepOriginalQuality(val enabled: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState =
            state.copy(keepOriginalQuality = enabled)
    }

    data class SetShowClearDialog(val show: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState =
            state.copy(showClearDialog = show)
    }

    data class SetClearing(val isClearing: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState =
            state.copy(isClearing = isClearing)
    }

    data class CacheCleared(val message: String) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState =
            state.copy(
                isClearing = false,
                showClearDialog = false,
                lastClearedMessage = message,
            )
    }
}
