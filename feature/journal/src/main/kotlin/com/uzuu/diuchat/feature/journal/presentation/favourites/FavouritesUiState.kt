package com.uzuu.diuchat.feature.journal.presentation.favourites

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.journal.domain.model.FavouriteMemory

data class FavouritesUiState(
    val isLoading: Boolean = false,
    val memories: List<FavouriteMemory> = emptyList(),
    val errorMessage: String? = null,
) : BaseState
