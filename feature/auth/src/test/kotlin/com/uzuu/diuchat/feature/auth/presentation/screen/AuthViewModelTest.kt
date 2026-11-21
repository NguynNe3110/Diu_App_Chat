package com.uzuu.diuchat.feature.auth.presentation.screen

import com.uzuu.diuchat.feature.auth.domain.model.OtpState
import com.uzuu.diuchat.feature.auth.domain.usecase.SendOtpUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.VerifyOtpUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class AuthViewModelTest {
    private val mockSendOtpUseCase: SendOtpUseCase = mockk()
    private val mockVerifyOtpUseCase: VerifyOtpUseCase = mockk()

    private val sut =
        AuthViewModel(
            mockSendOtpUseCase,
            mockVerifyOtpUseCase,
        )

    @Test
    fun `initial state is EditingPhone with empty number`() {
        val state = sut.uiStateFlow.value
        state.otpState shouldBeInstanceOf OtpState.EditingPhone::class
        state.rawPhone shouldBeEqualTo ""
        state.isPhoneValid shouldBeEqualTo false
    }

    @Test
    fun `onKeypadPhonePress appends digits up to 9 and del removes`() {
        sut.onKeypadPhonePress("9")
        sut.onKeypadPhonePress("1")
        sut.onKeypadPhonePress("2")
        sut.uiStateFlow.value.rawPhone shouldBeEqualTo "912"
        sut.uiStateFlow.value.formattedPhone shouldBeEqualTo "912"

        sut.onKeypadPhonePress("3")
        sut.onKeypadPhonePress("4")
        sut.onKeypadPhonePress("5")
        sut.uiStateFlow.value.rawPhone shouldBeEqualTo "912345"
        sut.uiStateFlow.value.formattedPhone shouldBeEqualTo "912 345"

        sut.onKeypadPhonePress("6")
        sut.onKeypadPhonePress("7")
        sut.onKeypadPhonePress("8")
        sut.uiStateFlow.value.rawPhone shouldBeEqualTo "912345678"
        sut.uiStateFlow.value.formattedPhone shouldBeEqualTo "912 345 678"
        sut.uiStateFlow.value.isPhoneValid shouldBeEqualTo true

        // Cannot add 10th digit
        sut.onKeypadPhonePress("9")
        sut.uiStateFlow.value.rawPhone shouldBeEqualTo "912345678"

        // Delete removes last digit
        sut.onKeypadPhonePress("del")
        sut.uiStateFlow.value.rawPhone shouldBeEqualTo "91234567"
        sut.uiStateFlow.value.isPhoneValid shouldBeEqualTo false
    }

    @Test
    fun `onSendOtpClicked transitions to Sending and then AwaitingCode on success`() = runTest {
        coEvery { mockSendOtpUseCase(any()) } returns Result.success(Unit)

        "912345678".forEach { sut.onKeypadPhonePress(it.toString()) }
        sut.onSendOtpClicked()

        advanceUntilIdle()

        val state = sut.uiStateFlow.value
        state.otpState shouldBeInstanceOf OtpState.AwaitingCode::class
        val awaiting = state.otpState as OtpState.AwaitingCode
        awaiting.phoneNumber shouldBeEqualTo "912345678"
        awaiting.code shouldBeEqualTo ""
    }

    @Test
    fun `onKeypadOtpPress auto verifies when 6 digits are entered`() = runTest {
        coEvery { mockSendOtpUseCase(any()) } returns Result.success(Unit)
        coEvery { mockVerifyOtpUseCase(any(), any()) } returns Result.success(Unit)

        "912345678".forEach { sut.onKeypadPhonePress(it.toString()) }
        sut.onSendOtpClicked()
        advanceUntilIdle()

        var authSuccessCalled = false
        "123456".forEach { digit ->
            sut.onKeypadOtpPress(digit.toString(), onAuthSuccess = { authSuccessCalled = true })
        }
        advanceUntilIdle()

        val state = sut.uiStateFlow.value
        state.otpState shouldBeInstanceOf OtpState.Success::class
        authSuccessCalled shouldBeEqualTo true
    }

    @Test
    fun `onBackToPhone resets state to EditingPhone`() = runTest {
        coEvery { mockSendOtpUseCase(any()) } returns Result.success(Unit)

        "912345678".forEach { sut.onKeypadPhonePress(it.toString()) }
        sut.onSendOtpClicked()
        advanceUntilIdle()

        sut.onBackToPhone()

        val state = sut.uiStateFlow.value
        state.otpState shouldBeInstanceOf OtpState.EditingPhone::class
        state.rawPhone shouldBeEqualTo "912345678"
    }
}
