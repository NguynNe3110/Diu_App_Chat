package com.uzuu.diuchat.feature.media.presentation.storage

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.model.ConversationStorage
import com.uzuu.diuchat.feature.media.domain.model.StorageStats

sealed interface StorageAction : BaseAction<StorageUiState> {

    data class SetLoading(val isLoading: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(isLoading = isLoading)
    }

    data class SetStats(val stats: StorageStats) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(isLoading = false, stats = stats)
    }

    data class SetAutoDownloadPolicy(val policy: AutoDownloadPolicy) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(
            autoDownloadPolicy = policy,
            autoDownloadWifi = policy != AutoDownloadPolicy.MANUAL,
        )
    }

    data class ToggleAutoDownloadWifi(val enabled: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(
            autoDownloadWifi = enabled,
            autoDownloadPolicy = if (enabled) AutoDownloadPolicy.WIFI_ONLY else AutoDownloadPolicy.MANUAL,
        )
    }

    data class ToggleAutoDownloadVoice(val enabled: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(autoDownloadVoice = enabled)
    }

    data class ToggleKeepOriginalQuality(val enabled: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(keepOriginalQuality = enabled)
    }

    data class SetShowClearDialog(val show: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(showClearDialog = show)
    }

    data class SetClearing(val isClearing: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(isClearing = isClearing)
    }

    data class SetSelectedConversationToClear(val conversation: ConversationStorage?) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(
            selectedConversationToClear = conversation,
            showConversationClearDialog = conversation != null,
        )
    }

    data class SetClearingConversation(val isClearing: Boolean) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(isClearingConversation = isClearing)
    }

    data class CacheCleared(val message: String) : StorageAction {
        override fun reduce(state: StorageUiState): StorageUiState = state.copy(
            isClearing = false,
            showClearDialog = false,
            selectedConversationToClear = null,
            showConversationClearDialog = false,
            isClearingConversation = false,
            lastClearedMessage = message,
        )
    }
}
