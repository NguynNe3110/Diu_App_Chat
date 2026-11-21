package com.uzuu.diuchat.feature.auth.presentation.screen

import com.uzuu.diuchat.feature.auth.domain.model.OtpState
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class AuthActionTest {
    @Test
    fun `UpdatePhone updates phone in EditingPhone state`() {
        val initial = AuthUiState()
        val next = AuthAction.UpdatePhone("912345678").reduce(initial)

        next.rawPhone shouldBeEqualTo "912345678"
        next.otpState shouldBeInstanceOf OtpState.EditingPhone::class
    }

    @Test
    fun `SetSending transitions to Sending`() {
        val initial = AuthUiState()
        val next = AuthAction.SetSending("912345678").reduce(initial)

        next.otpState shouldBeInstanceOf OtpState.Sending::class
    }

    @Test
    fun `SetAwaitingCode transitions to AwaitingCode with deadline`() {
        val initial = AuthUiState()
        val deadline = 100_000L
        val next = AuthAction.SetAwaitingCode("912345678", deadline).reduce(initial)

        next.otpState shouldBeInstanceOf OtpState.AwaitingCode::class
        val state = next.otpState as OtpState.AwaitingCode
        state.resendDeadlineEpochMs shouldBeEqualTo deadline
        state.code shouldBeEqualTo ""
    }

    @Test
    fun `UpdateCode updates code in AwaitingCode`() {
        val initial =
            AuthUiState(
                otpState = OtpState.AwaitingCode("912345678", code = "12", resendDeadlineEpochMs = 100L),
            )
        val next = AuthAction.UpdateCode("123").reduce(initial)

        (next.otpState as OtpState.AwaitingCode).code shouldBeEqualTo "123"
    }

    @Test
    fun `SetVerifying transitions to Verifying`() {
        val initial = AuthUiState()
        val next = AuthAction.SetVerifying("912345678", "123456").reduce(initial)

        next.otpState shouldBeInstanceOf OtpState.Verifying::class
    }

    @Test
    fun `SetSuccess transitions to Success`() {
        val initial = AuthUiState()
        val next = AuthAction.SetSuccess("912345678").reduce(initial)

        next.otpState shouldBeInstanceOf OtpState.Success::class
    }

    @Test
    fun `SetError transitions to Error`() {
        val initial = AuthUiState()
        val next = AuthAction.SetError("912345678", "Error message").reduce(initial)

        next.otpState shouldBeInstanceOf OtpState.Error::class
        val err = next.otpState as OtpState.Error
        err.message shouldBeEqualTo "Error message"
    }

    @Test
    fun `TickCooldown updates resendSecondsRemaining`() {
        val initial = AuthUiState(resendSecondsRemaining = 60)
        val next = AuthAction.TickCooldown(45).reduce(initial)

        next.resendSecondsRemaining shouldBeEqualTo 45
    }

    @Test
    fun `BackToPhone resets to EditingPhone`() {
        val initial =
            AuthUiState(
                otpState = OtpState.AwaitingCode("912345678", "123", 100L),
                resendSecondsRemaining = 30,
                isChoosingUsername = true,
            )
        val next = AuthAction.BackToPhone("912345678").reduce(initial)

        next.otpState shouldBeInstanceOf OtpState.EditingPhone::class
        next.resendSecondsRemaining shouldBeEqualTo 0
        next.isChoosingUsername shouldBeEqualTo false
    }

    @Test
    fun `SetGoogleLoading updates loading and error state`() {
        val initial = AuthUiState()
        val loading = AuthAction.SetGoogleLoading(isLoading = true).reduce(initial)
        loading.isGoogleLoading shouldBeEqualTo true
        loading.googleErrorMessage shouldBeEqualTo null

        val error = AuthAction.SetGoogleLoading(isLoading = false, error = "Google failed").reduce(loading)
        error.isGoogleLoading shouldBeEqualTo false
        error.googleErrorMessage shouldBeEqualTo "Google failed"
    }

    @Test
    fun `ShowChooseUsername transitions to choosing username state`() {
        val initial = AuthUiState(isGoogleLoading = true)
        val next = AuthAction.ShowChooseUsername("minh_an").reduce(initial)

        next.isChoosingUsername shouldBeEqualTo true
        next.usernameInput shouldBeEqualTo "minh_an"
        next.isGoogleLoading shouldBeEqualTo false
    }

    @Test
    fun `UpdateUsernameInput updates username input and resets error`() {
        val initial = AuthUiState(usernameErrorMessage = "Username duplicate")
        val next = AuthAction.UpdateUsernameInput("new_name").reduce(initial)

        next.usernameInput shouldBeEqualTo "new_name"
        next.usernameErrorMessage shouldBeEqualTo null
    }

    @Test
    fun `SetClaimingUsername updates claiming and error state`() {
        val initial = AuthUiState()
        val claiming = AuthAction.SetClaimingUsername(isClaiming = true).reduce(initial)
        claiming.isClaimingUsername shouldBeEqualTo true

        val failed = AuthAction.SetClaimingUsername(isClaiming = false, error = "Tên đã tồn tại").reduce(claiming)
        failed.isClaimingUsername shouldBeEqualTo false
        failed.usernameErrorMessage shouldBeEqualTo "Tên đã tồn tại"
    }

    @Test
    fun `ClearGoogleError clears error`() {
        val initial = AuthUiState(googleErrorMessage = "Lỗi kết nối")
        val next = AuthAction.ClearGoogleError.reduce(initial)

        next.googleErrorMessage shouldBeEqualTo null
    }
}
