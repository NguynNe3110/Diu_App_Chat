package com.uzuu.diuchat.feature.media.presentation.vault

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.media.domain.model.MediaType
import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem

@Immutable
data class MediaVaultUiState(
    val isLoading: Boolean = false,
    val selectedType: MediaType = MediaType.ALL,
    val allItems: List<MediaVaultItem> = emptyList(),
    val filteredItems: List<MediaVaultItem> = emptyList(),
    val currentlyPlayingVoiceId: String? = null,
) : BaseState {
    val isEmpty: Boolean get() = filteredItems.isEmpty()

    val groupedByMonth: Map<String, List<MediaVaultItem>>
        get() = filteredItems.groupBy { it.monthGroup }
}
