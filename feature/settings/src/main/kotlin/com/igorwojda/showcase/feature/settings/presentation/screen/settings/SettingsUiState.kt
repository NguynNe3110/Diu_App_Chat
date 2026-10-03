package com.uzuu.diuchat.feature.settings.presentation.screen.settings

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState

@Immutable
internal sealed interface SettingsUiState : BaseState {
    @Immutable
    data object Content : SettingsUiState
}
