package com.uzuu.diuchat.feature.location.presentation

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.location.domain.model.LiveDuration
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.usecase.GetLocationSnapshotUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.StartLiveLocationUseCase
import kotlinx.coroutines.launch

class LocationPickerViewModel(
    private val getLocationSnapshotUseCase: GetLocationSnapshotUseCase,
    private val startLiveLocationUseCase: StartLiveLocationUseCase,
) : BaseViewModel<LocationPickerUiState, LocationPickerAction>(LocationPickerUiState()) {

    init {
        loadCurrentLocation()
    }

    fun loadCurrentLocation() {
        sendAction(LocationPickerAction.SetLoading(true))
        viewModelScope.launch {
            getLocationSnapshotUseCase()
                .onSuccess { snapshot ->
                    sendAction(LocationPickerAction.SetLocation(snapshot))
                }
                .onFailure { error ->
                    sendAction(LocationPickerAction.SetError(error.message ?: "Không thể lấy vị trí hiện tại."))
                }
        }
    }

    fun selectDuration(duration: LiveDuration) {
        sendAction(LocationPickerAction.SelectDuration(duration))
    }

    fun toggleMode(isLiveMode: Boolean) {
        sendAction(LocationPickerAction.ToggleMode(isLiveMode))
    }

    fun startLiveSharing(
        conversationId: String,
        onSuccess: ((LiveLocationSession) -> Unit)? = null,
    ) {
        sendAction(LocationPickerAction.SetLoading(true))
        viewModelScope.launch {
            val durationMinutes = uiStateFlow.value.selectedDuration.minutes
            startLiveLocationUseCase(conversationId, durationMinutes)
                .onSuccess { session ->
                    sendAction(LocationPickerAction.LiveSharingStarted(session))
                    onSuccess?.invoke(session)
                }
                .onFailure { error ->
                    sendAction(LocationPickerAction.SetError(error.message ?: "Không thể bắt đầu chia sẻ vị trí."))
                }
        }
    }
}
