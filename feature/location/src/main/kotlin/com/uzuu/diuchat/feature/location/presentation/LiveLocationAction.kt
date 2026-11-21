package com.uzuu.diuchat.feature.location.presentation

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession

sealed interface LiveLocationAction : BaseAction<LiveLocationUiState> {

    data class SetLoading(val isLoading: Boolean) : LiveLocationAction {
        override fun reduce(state: LiveLocationUiState): LiveLocationUiState = state.copy(isLoading = isLoading)
    }

    data class UpdateSession(val session: LiveLocationSession?) : LiveLocationAction {
        override fun reduce(state: LiveLocationUiState): LiveLocationUiState = state.copy(
            session = session,
            isLoading = false,
            isSharingActive = session?.isActive == true && !(session.isExpired),
        )
    }

    data class UpdateRemainingTime(val formattedTime: String) : LiveLocationAction {
        override fun reduce(state: LiveLocationUiState): LiveLocationUiState = state.copy(remainingTimeFormatted = formattedTime)
    }

    data object SharingStopped : LiveLocationAction {
        override fun reduce(state: LiveLocationUiState): LiveLocationUiState = state.copy(
            isSharingActive = false,
            session = state.session?.copy(isActive = false),
        )
    }

    data class SetError(val errorMessage: String?) : LiveLocationAction {
        override fun reduce(state: LiveLocationUiState): LiveLocationUiState = state.copy(errorMessage = errorMessage, isLoading = false)
    }
}
