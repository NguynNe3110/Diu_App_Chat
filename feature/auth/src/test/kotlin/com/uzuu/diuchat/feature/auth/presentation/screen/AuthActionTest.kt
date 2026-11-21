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
        val initial = AuthUiState(
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
        val initial = AuthUiState(
            otpState = OtpState.AwaitingCode("912345678", "123", 100L),
            resendSecondsRemaining = 30,
        )
        val next = AuthAction.BackToPhone("912345678").reduce(initial)

        next.otpState shouldBeInstanceOf OtpState.EditingPhone::class
        next.resendSecondsRemaining shouldBeEqualTo 0
    }
}
