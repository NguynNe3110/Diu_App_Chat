package com.uzuu.diuchat.feature.auth.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.auth.domain.model.OtpState
import com.uzuu.diuchat.feature.auth.domain.usecase.CheckHasUsernameUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.ClaimUsernameUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.SendOtpUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.VerifyOtpUseCase
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val checkHasUsernameUseCase: CheckHasUsernameUseCase,
    private val claimUsernameUseCase: ClaimUsernameUseCase,
) : BaseViewModel<AuthUiState, AuthAction>(AuthUiState()) {
    private var cooldownJob: Job? = null

    fun onKeypadPhonePress(key: String) {
        val currentPhone = uiStateFlow.value.rawPhone
        val newPhone =
            if (key == "del") {
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
            // Luôn mở màn hình nhập OTP để người dùng trải nghiệm đầy đủ UI ngay cả khi chưa tích hợp nhà cung cấp SMS
            val deadlineEpochMs = System.currentTimeMillis() + RESEND_COOLDOWN_MS
            sendAction(
                AuthAction.SetAwaitingCode(
                    phone = phone,
                    deadlineEpochMs = deadlineEpochMs,
                ),
            )
            startCooldownTimer(deadlineEpochMs)
        }
    }

    fun onKeypadOtpPress(
        key: String,
        onAuthSuccess: () -> Unit = {},
    ) {
        val currentState = uiStateFlow.value.otpState as? OtpState.AwaitingCode ?: return
        val currentCode = currentState.code

        val newCode =
            if (key == "del") {
                currentCode.dropLast(1)
            } else {
                if (key.all { it.isDigit() } && currentCode.length < OTP_DIGITS) currentCode + key else currentCode
            }

        sendAction(AuthAction.UpdateCode(newCode))

        if (newCode.length == OTP_DIGITS) {
            verifyOtp(currentState.phoneNumber, newCode, onAuthSuccess)
        }
    }

    private fun verifyOtp(
        phone: String,
        code: String,
        onAuthSuccess: () -> Unit,
    ) {
        sendAction(AuthAction.SetVerifying(phone, code))
        viewModelScope.launch {
            verifyOtpUseCase(phone, code)
                .onSuccess {
                    cooldownJob?.cancel()
                    checkUsernameAndNavigate(onAuthSuccess)
                }.onFailure {
                    val awaiting = uiStateFlow.value.otpState as? OtpState.AwaitingCode
                    val deadline = awaiting?.resendDeadlineEpochMs ?: (System.currentTimeMillis() + RESEND_COOLDOWN_MS)
                    sendAction(
                        AuthAction.SetAwaitingCode(
                            phone = phone,
                            code = "",
                            deadlineEpochMs = deadline,
                            error = "Mã xác thực chưa đúng hoặc chưa nhận được SMS. Bạn có thể quay lại để đăng nhập bằng Google.",
                        ),
                    )
                }
        }
    }

    fun onStartGoogleSignIn() {
        sendAction(AuthAction.SetGoogleLoading(isLoading = true))
    }

    fun onGoogleIdTokenReceived(
        idToken: String,
        onAuthSuccess: () -> Unit,
    ) {
        sendAction(AuthAction.SetGoogleLoading(isLoading = true))
        viewModelScope.launch {
            signInWithGoogleUseCase(idToken)
                .onSuccess {
                    checkUsernameAndNavigate(onAuthSuccess)
                }.onFailure { error ->
                    sendAction(
                        AuthAction.SetGoogleLoading(
                            isLoading = false,
                            error = error.message ?: "Đăng nhập Google thất bại. Vui lòng thử lại.",
                        ),
                    )
                }
        }
    }

    fun onGoogleSignInError(message: String) {
        sendAction(AuthAction.SetGoogleLoading(isLoading = false, error = message))
    }

    fun onDismissGoogleError() {
        sendAction(AuthAction.ClearGoogleError)
    }

    fun requireUsernameSetup() {
        if (!uiStateFlow.value.isChoosingUsername) {
            sendAction(AuthAction.ShowChooseUsername())
        }
    }

    private suspend fun checkUsernameAndNavigate(onAuthSuccess: () -> Unit) {
        checkHasUsernameUseCase()
            .onSuccess { hasUsername ->
                sendAction(AuthAction.SetGoogleLoading(isLoading = false))
                if (hasUsername) {
                    sendAction(AuthAction.SetSuccess(uiStateFlow.value.rawPhone))
                    onAuthSuccess()
                } else {
                    sendAction(AuthAction.ShowChooseUsername())
                }
            }.onFailure { error ->
                sendAction(
                    AuthAction.SetGoogleLoading(
                        isLoading = false,
                        error = error.message ?: "Không thể kiểm tra thông tin tài khoản. Vui lòng thử lại.",
                    ),
                )
            }
    }

    fun onUsernameInputChanged(input: String) {
        sendAction(AuthAction.UpdateUsernameInput(input))
    }

    fun onConfirmUsernameClicked(onAuthSuccess: () -> Unit) {
        val state = uiStateFlow.value
        val canonical = state.canonicalUsername
        if (!state.isUsernameValid) {
            sendAction(
                AuthAction.SetClaimingUsername(
                    isClaiming = false,
                    error = "Username phải từ 3 đến 30 ký tự, chỉ gồm chữ thường (a-z), số (0-9) và gạch dưới (_)",
                ),
            )
            return
        }

        sendAction(AuthAction.SetClaimingUsername(isClaiming = true))
        viewModelScope.launch {
            claimUsernameUseCase(canonical)
                .onSuccess {
                    sendAction(AuthAction.SetClaimingUsername(isClaiming = false))
                    onAuthSuccess()
                }.onFailure { error ->
                    val errorMsg = if (error.message?.contains("duplicate", ignoreCase = true) == true ||
                        error.message?.contains("unique", ignoreCase = true) == true ||
                        error.message?.contains("username", ignoreCase = true) == true
                    ) {
                        "Tên định danh @$canonical đã được sử dụng. Vui lòng chọn tên khác."
                    } else {
                        error.message ?: "Không thể lưu username. Vui lòng thử lại."
                    }
                    sendAction(
                        AuthAction.SetClaimingUsername(
                            isClaiming = false,
                            error = errorMsg,
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
    }

    fun onBackToPhone() {
        cooldownJob?.cancel()
        val phone = uiStateFlow.value.rawPhone
        sendAction(AuthAction.BackToPhone(phone))
    }

    fun startCooldownTimer(deadlineEpochMs: Long) {
        cooldownJob?.cancel()
        cooldownJob =
            viewModelScope.launch {
                var remainingSec = ((deadlineEpochMs - System.currentTimeMillis()) / 1000).coerceAtLeast(0).toInt()
                while (remainingSec > 0) {
                    sendAction(AuthAction.TickCooldown(remainingSec))
                    delay(1000)
                    remainingSec--
                }
                sendAction(AuthAction.TickCooldown(0))
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
