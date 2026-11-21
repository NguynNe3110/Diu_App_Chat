package com.uzuu.diuchat.feature.location.presentation

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession

data class LiveLocationUiState(
    val session: LiveLocationSession? = null,
    val isLoading: Boolean = false,
    val remainingTimeFormatted: String = "",
    val isSharingActive: Boolean = true,
    val errorMessage: String? = null,
) : BaseState
