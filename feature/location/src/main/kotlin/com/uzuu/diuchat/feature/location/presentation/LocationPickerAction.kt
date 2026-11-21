package com.uzuu.diuchat.feature.location.presentation

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.location.domain.model.LiveDuration
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot

sealed interface LocationPickerAction : BaseAction<LocationPickerUiState> {

    data class SetLoading(val isLoading: Boolean) : LocationPickerAction {
        override fun reduce(state: LocationPickerUiState): LocationPickerUiState = state.copy(isLoading = isLoading)
    }

    data class SetLocation(val location: LocationSnapshot) : LocationPickerAction {
        override fun reduce(state: LocationPickerUiState): LocationPickerUiState = state.copy(
            currentLocation = location,
            isLoading = false,
            errorMessage = null,
        )
    }

    data class SetError(val errorMessage: String?) : LocationPickerAction {
        override fun reduce(state: LocationPickerUiState): LocationPickerUiState = state.copy(
            errorMessage = errorMessage,
            isLoading = false,
        )
    }

    data class SelectDuration(val duration: LiveDuration) : LocationPickerAction {
        override fun reduce(state: LocationPickerUiState): LocationPickerUiState = state.copy(selectedDuration = duration)
    }

    data class ToggleMode(val isLiveMode: Boolean) : LocationPickerAction {
        override fun reduce(state: LocationPickerUiState): LocationPickerUiState = state.copy(isLiveMode = isLiveMode)
    }

    data class LiveSharingStarted(val session: LiveLocationSession) : LocationPickerAction {
        override fun reduce(state: LocationPickerUiState): LocationPickerUiState = state.copy(
            createdSession = session,
            isLoading = false,
            errorMessage = null,
        )
    }
}
