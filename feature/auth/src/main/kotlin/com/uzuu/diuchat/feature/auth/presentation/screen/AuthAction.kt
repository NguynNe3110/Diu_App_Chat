package com.uzuu.diuchat.feature.auth.presentation.screen

import com.uzuu.diuchat.feature.auth.domain.model.OtpState
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction

sealed interface AuthAction : BaseAction<AuthUiState> {

    data class UpdatePhone(val phone: String) : AuthAction {
        override fun reduce(state: AuthUiState): AuthUiState =
            state.copy(otpState = OtpState.EditingPhone(phoneNumber = phone))
    }

    data class SetSending(val phone: String) : AuthAction {
        override fun reduce(state: AuthUiState): AuthUiState =
            state.copy(otpState = OtpState.Sending(phoneNumber = phone))
    }

    data class SetAwaitingCode(
        val phone: String,
        val deadlineEpochMs: Long,
        val code: String = "",
        val error: String? = null,
    ) : AuthAction {
        override fun reduce(state: AuthUiState): AuthUiState =
            state.copy(
                otpState = OtpState.AwaitingCode(
                    phoneNumber = phone,
                    code = code,
                    resendDeadlineEpochMs = deadlineEpochMs,
                    error = error,
                ),
            )
    }

    data class UpdateCode(val code: String) : AuthAction {
        override fun reduce(state: AuthUiState): AuthUiState {
            val current = state.otpState as? OtpState.AwaitingCode ?: return state
            return state.copy(otpState = current.copy(code = code, error = null))
        }
    }

    data class SetVerifying(val phone: String, val code: String) : AuthAction {
        override fun reduce(state: AuthUiState): AuthUiState =
            state.copy(otpState = OtpState.Verifying(phoneNumber = phone, code = code))
    }

    data class SetSuccess(val phone: String) : AuthAction {
        override fun reduce(state: AuthUiState): AuthUiState =
            state.copy(otpState = OtpState.Success(phoneNumber = phone))
    }

    data class SetError(
        val phone: String,
        val message: String,
        val retryable: Boolean = true,
    ) : AuthAction {
        override fun reduce(state: AuthUiState): AuthUiState =
            state.copy(otpState = OtpState.Error(phoneNumber = phone, message = message, retryable = retryable))
    }

    data class TickCooldown(val secondsRemaining: Int) : AuthAction {
        override fun reduce(state: AuthUiState): AuthUiState =
            state.copy(resendSecondsRemaining = secondsRemaining)
    }

    data class BackToPhone(val phone: String) : AuthAction {
        override fun reduce(state: AuthUiState): AuthUiState =
            state.copy(
                otpState = OtpState.EditingPhone(phoneNumber = phone),
                resendSecondsRemaining = 0,
            )
    }
}
