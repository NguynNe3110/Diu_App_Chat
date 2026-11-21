package com.uzuu.diuchat.feature.auth.domain.model

sealed interface OtpState {
    data class EditingPhone(
        val phoneNumber: String = "",
        val validationError: String? = null,
    ) : OtpState

    data class Sending(
        val phoneNumber: String,
    ) : OtpState

    data class AwaitingCode(
        val phoneNumber: String,
        val code: String = "",
        val resendDeadlineEpochMs: Long,
        val error: String? = null,
    ) : OtpState

    data class Verifying(
        val phoneNumber: String,
        val code: String,
    ) : OtpState

    data class Success(
        val phoneNumber: String,
    ) : OtpState

    data class Error(
        val phoneNumber: String,
        val message: String,
        val retryable: Boolean = true,
    ) : OtpState
}
