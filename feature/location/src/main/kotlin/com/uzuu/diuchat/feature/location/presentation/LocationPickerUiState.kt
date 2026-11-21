package com.uzuu.diuchat.feature.location.presentation

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.location.domain.model.LiveDuration
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot

data class LocationPickerUiState(
    val currentLocation: LocationSnapshot? = null,
    val isLoading: Boolean = false,
    val isLiveMode: Boolean = false,
    val selectedDuration: LiveDuration = LiveDuration.FIFTEEN_MINUTES,
    val createdSession: LiveLocationSession? = null,
    val errorMessage: String? = null,
) : BaseState
