package com.uzuu.diuchat.feature.media.presentation.vault

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.media.domain.model.MediaType
import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem

sealed interface MediaVaultAction : BaseAction<MediaVaultUiState> {

    data class SetLoading(val isLoading: Boolean) : MediaVaultAction {
        override fun reduce(state: MediaVaultUiState): MediaVaultUiState = state.copy(isLoading = isLoading)
    }

    data class SetItems(val items: List<MediaVaultItem>) : MediaVaultAction {
        override fun reduce(state: MediaVaultUiState): MediaVaultUiState {
            val filtered = filterItems(items, state.selectedType)
            return state.copy(
                isLoading = false,
                allItems = items,
                filteredItems = filtered,
            )
        }
    }

    data class SelectType(val type: MediaType) : MediaVaultAction {
        override fun reduce(state: MediaVaultUiState): MediaVaultUiState {
            val filtered = filterItems(state.allItems, type)
            return state.copy(
                selectedType = type,
                filteredItems = filtered,
            )
        }
    }

    data class TogglePlayVoice(val voiceId: String) : MediaVaultAction {
        override fun reduce(state: MediaVaultUiState): MediaVaultUiState = state.copy(
            currentlyPlayingVoiceId = if (state.currentlyPlayingVoiceId == voiceId) null else voiceId,
        )
    }

    companion object {
        fun filterItems(items: List<MediaVaultItem>, type: MediaType): List<MediaVaultItem> = if (type == MediaType.ALL) {
            items
        } else {
            items.filter { it.type == type }
        }
    }
}
