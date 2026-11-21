package com.uzuu.diuchat.feature.auth.presentation.screen

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.auth.domain.model.OtpState
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState

@Immutable
data class AuthUiState(
    val otpState: OtpState = OtpState.EditingPhone(),
    val resendSecondsRemaining: Int = 0,
) : BaseState {

    val rawPhone: String
        get() = when (val s = otpState) {
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
            return when {
                digits.length <= 3 -> digits
                digits.length <= 6 -> "${digits.substring(0, 3)} ${digits.substring(3)}"
                else -> "${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6)}"
            }
        }

    val isPhoneValid: Boolean
        get() = rawPhone.length == 9

    val canResend: Boolean
        get() = resendSecondsRemaining <= 0
}
