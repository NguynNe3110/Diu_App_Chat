package com.uzuu.diuchat.feature.journal.presentation.favourites

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.journal.domain.model.FavouriteMemory

sealed interface FavouritesAction : BaseAction<FavouritesUiState> {
    data class SetLoading(val isLoading: Boolean) : FavouritesAction {
        override fun reduce(state: FavouritesUiState): FavouritesUiState = state.copy(isLoading = isLoading)
    }

    data class SetMemories(val memories: List<FavouriteMemory>) : FavouritesAction {
        override fun reduce(state: FavouritesUiState): FavouritesUiState = state.copy(
            memories = memories,
            isLoading = false,
            errorMessage = null,
        )
    }

    data class SetError(val message: String?) : FavouritesAction {
        override fun reduce(state: FavouritesUiState): FavouritesUiState = state.copy(
            errorMessage = message,
            isLoading = false,
        )
    }
}
