package com.uzuu.diuchat.feature.album.presentation.screen.albumdetail

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.album.domain.usecase.GetAlbumUseCase
import com.uzuu.diuchat.feature.base.domain.result.Result.Failure
import com.uzuu.diuchat.feature.base.domain.result.Result.Success
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

internal class AlbumDetailViewModel(
    private val getAlbumUseCase: GetAlbumUseCase,
) : BaseViewModel<AlbumDetailUiState, AlbumDetailAction>(AlbumDetailUiState.Loading) {
    fun onInit(
        albumName: String,
        artistName: String,
        albumMbId: String?,
    ) {
        getAlbum(albumName, artistName, albumMbId)
    }

    private fun getAlbum(
        albumName: String,
        artistName: String,
        albumMbId: String?,
    ) {
        sendAction(AlbumDetailAction.AlbumLoadStart)

        viewModelScope.launch {
            getAlbumUseCase(artistName, albumName, albumMbId).also {
                when (it) {
                    is Success -> {
                        sendAction(AlbumDetailAction.AlbumLoadSuccess(it.value))
                    }

                    is Failure -> {
                        sendAction(AlbumDetailAction.AlbumLoadFailure)
                    }
                }
            }
        }
    }
}
