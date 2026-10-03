package com.uzuu.diuchat.feature.settings.presentation.screen.aboutlibraries

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState

@Immutable
internal sealed interface AboutLibrariesUiState : BaseState {
    @Immutable
    data object Content : AboutLibrariesUiState
}
