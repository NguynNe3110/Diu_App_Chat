package com.uzuu.diuchat.feature.auth.presentation.screen

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.auth.domain.model.OtpState
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState

private const val PHONE_DIGITS = 9
private const val PHONE_GROUP_SIZE = 3
private val USERNAME_REGEX = Regex("^[a-z0-9_]{3,30}$")

@Immutable
data class AuthUiState(
    val otpState: OtpState = OtpState.EditingPhone(),
    val resendSecondsRemaining: Int = 0,
    val isGoogleLoading: Boolean = false,
    val googleErrorMessage: String? = null,
    val isChoosingUsername: Boolean = false,
    val usernameInput: String = "",
    val isClaimingUsername: Boolean = false,
    val usernameErrorMessage: String? = null,
) : BaseState {
    val rawPhone: String
        get() =
            when (val s = otpState) {
                is OtpState.EditingPhone -> s.phoneNumber
                is OtpState.Sending -> s.phoneNumber
                is OtpState.AwaitingCode -> s.phoneNumber
                is OtpState.Verifying -> s.phoneNumber
                is OtpState.Success -> s.phoneNumber
                is OtpState.Error -> s.phoneNumber
            }

    val formattedPhone: String
        get() {
            val digits = rawPhone
            return digits.chunked(PHONE_GROUP_SIZE).joinToString(" ")
        }

    val isPhoneValid: Boolean
        get() = rawPhone.length == PHONE_DIGITS

    val canResend: Boolean
        get() = resendSecondsRemaining <= 0

    val canonicalUsername: String
        get() = usernameInput.trim().removePrefix("@").lowercase()

    val isUsernameValid: Boolean
        get() = canonicalUsername.matches(USERNAME_REGEX)
}
