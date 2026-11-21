package com.uzuu.diuchat.feature.settings.domain.model

sealed interface AccountDeletionState {
    data object Idle : AccountDeletionState
    data object AwaitingConfirmation : AccountDeletionState
    data object Processing : AccountDeletionState
    data object Completed : AccountDeletionState
    data class Failed(val error: String) : AccountDeletionState
}
