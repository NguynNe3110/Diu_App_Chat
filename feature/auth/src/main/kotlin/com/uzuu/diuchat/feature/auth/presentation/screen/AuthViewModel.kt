package com.uzuu.diuchat.feature.auth.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.auth.domain.model.OtpState
import com.uzuu.diuchat.feature.auth.domain.usecase.SendOtpUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.VerifyOtpUseCase
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
) : BaseViewModel<AuthUiState, AuthAction>(AuthUiState()) {

    private var cooldownJob: Job? = null

    fun onKeypadPhonePress(key: String) {
        val currentPhone = uiStateFlow.value.rawPhone
        val newPhone = if (key == "del") {
            currentPhone.dropLast(1)
        } else {
            if (key.all { it.isDigit() } && currentPhone.length < PHONE_DIGITS) currentPhone + key else currentPhone
        }
        sendAction(AuthAction.UpdatePhone(newPhone))
    }

    fun onSendOtpClicked() {
        val phone = uiStateFlow.value.rawPhone
        if (phone.length != PHONE_DIGITS) return

        sendAction(AuthAction.SetSending(phone))
        viewModelScope.launch {
            sendOtpUseCase(phone)
                .onSuccess {
                    val deadlineEpochMs = System.currentTimeMillis() + RESEND_COOLDOWN_MS
                    sendAction(
                        AuthAction.SetAwaitingCode(
                            phone = phone,
                            deadlineEpochMs = deadlineEpochMs,
                        ),
                    )
                    startCooldownTimer(deadlineEpochMs)
                }
                .onFailure { error ->
                    sendAction(
                        AuthAction.SetError(
                            phone = phone,
                            message = error.message ?: "Gửi mã OTP thất bại. Vui lòng thử lại.",
                            retryable = true,
                        ),
                    )
                }
        }
    }

    fun onKeypadOtpPress(key: String, onAuthSuccess: () -> Unit = {}) {
        val currentState = uiStateFlow.value.otpState as? OtpState.AwaitingCode ?: return
        val currentCode = currentState.code

        val newCode = if (key == "del") {
            currentCode.dropLast(1)
        } else {
            if (key.all { it.isDigit() } && currentCode.length < OTP_DIGITS) currentCode + key else currentCode
        }

        sendAction(AuthAction.UpdateCode(newCode))

        if (newCode.length == OTP_DIGITS) {
            verifyOtp(currentState.phoneNumber, newCode, onAuthSuccess)
        }
    }

    private fun verifyOtp(phone: String, code: String, onAuthSuccess: () -> Unit) {
        sendAction(AuthAction.SetVerifying(phone, code))
        viewModelScope.launch {
            verifyOtpUseCase(phone, code)
                .onSuccess {
                    cooldownJob?.cancel()
                    sendAction(AuthAction.SetSuccess(phone))
                    onAuthSuccess()
                }
                .onFailure { error ->
                    val awaiting = uiStateFlow.value.otpState as? OtpState.AwaitingCode
                    val deadline = awaiting?.resendDeadlineEpochMs ?: (System.currentTimeMillis() + RESEND_COOLDOWN_MS)
                    sendAction(
                        AuthAction.SetAwaitingCode(
                            phone = phone,
                            code = "",
                            deadlineEpochMs = deadline,
                            error = error.message ?: "Mã xác thực không đúng. Vui lòng thử lại.",
                        ),
                    )
                }
        }
    }

    fun onResendOtpClicked() {
        if (!uiStateFlow.value.canResend) return
        val phone = uiStateFlow.value.rawPhone

        viewModelScope.launch {
            sendOtpUseCase(phone)
                .onSuccess {
                    val deadlineEpochMs = System.currentTimeMillis() + RESEND_COOLDOWN_MS
                    sendAction(
                        AuthAction.SetAwaitingCode(
                            phone = phone,
                            code = "",
                            deadlineEpochMs = deadlineEpochMs,
                        ),
                    )
                    startCooldownTimer(deadlineEpochMs)
                }
                .onFailure { error ->
                    sendAction(
                        AuthAction.SetError(
                            phone = phone,
                            message = error.message ?: "Không thể gửi lại mã OTP.",
                            retryable = true,
                        ),
                    )
                }
        }
    }

    fun onBackToPhone() {
        cooldownJob?.cancel()
        val phone = uiStateFlow.value.rawPhone
        sendAction(AuthAction.BackToPhone(phone))
    }

    fun startCooldownTimer(deadlineEpochMs: Long) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            while (true) {
                val remainingMs = deadlineEpochMs - System.currentTimeMillis()
                val remainingSec = (remainingMs / 1000).coerceAtLeast(0).toInt()
                sendAction(AuthAction.TickCooldown(remainingSec))
                if (remainingSec <= 0) break
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }

    companion object {
        private const val RESEND_COOLDOWN_MS = 60_000L
        private const val PHONE_DIGITS = 9
        private const val OTP_DIGITS = 6
    }
}
