package com.uzuu.diuchat.feature.album.presentation.screen.albumdetail

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.album.domain.model.Tag
import com.uzuu.diuchat.feature.album.domain.model.Track
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState

@Immutable
internal sealed interface AlbumDetailUiState : BaseState {
    @Immutable
    data class Content(
        val albumName: String = "",
        val artistName: String = "",
        val coverImageUrl: String = "",
        val tracks: List<Track>? = emptyList(),
        val tags: List<Tag>? = emptyList(),
    ) : AlbumDetailUiState

    @Immutable
    data object Loading : AlbumDetailUiState

    @Immutable
    data object Error : AlbumDetailUiState
}
