package com.uzuu.diuchat.feature.journal.presentation.favourites

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.journal.domain.usecase.GetFavouritesUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.ToggleFavouriteUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class FavouritesViewModel(
    private val getFavouritesUseCase: GetFavouritesUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
) : BaseViewModel<FavouritesUiState, FavouritesAction>(FavouritesUiState()) {

    init {
        loadFavourites()
    }

    private fun loadFavourites() {
        sendAction(FavouritesAction.SetLoading(true))
        viewModelScope.launch {
            getFavouritesUseCase()
                .catch { sendAction(FavouritesAction.SetError(it.message)) }
                .collect { memories ->
                    sendAction(FavouritesAction.SetMemories(memories))
                }
        }
    }

    fun onToggleFavourite(memoryId: String) {
        viewModelScope.launch {
            toggleFavouriteUseCase(memoryId)
                .onFailure { sendAction(FavouritesAction.SetError(it.message)) }
        }
    }
}
