package com.uzuu.diuchat.feature.onboarding.presentation.screen

import com.uzuu.diuchat.feature.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class OnBoardingViewModelTest {
    private val mockCompleteOnboardingUseCase: CompleteOnboardingUseCase = mockk()

    private val sut = OnBoardingViewModel(completeOnboardingUseCase = mockCompleteOnboardingUseCase)

    @Test
    fun `initial state starts on the first page`() {
        sut.uiStateFlow.value shouldBeEqualTo OnBoardingUiState.Initial
    }

    @Test
    fun `page changes update the selected page`() {
        sut.onPageChanged(2)

        sut.uiStateFlow.value.currentPage shouldBeEqualTo 2
    }

    @Test
    fun `returning to the first page updates the current state`() {
        sut.onPageChanged(2)
        sut.onPageChanged(0)

        sut.uiStateFlow.value shouldBeEqualTo OnBoardingUiState.Initial
    }

    @Test
    fun `opening and backing out of permissions step updates state`() {
        sut.onOpenPermissionsStep()
        sut.uiStateFlow.value.showPermissionsStep shouldBeEqualTo true

        sut.onBackFromPermissions()
        sut.uiStateFlow.value.showPermissionsStep shouldBeEqualTo false
    }

    @Test
    fun `toggles update permission and quiet hours flags`() {
        sut.onNotificationsToggled(true)
        sut.onMicrophoneToggled(true)
        sut.onQuietHoursToggled(false)

        val state = sut.uiStateFlow.value
        state.notificationsEnabled shouldBeEqualTo true
        state.microphoneEnabled shouldBeEqualTo true
        state.quietHoursEnabled shouldBeEqualTo false
    }

    @Test
    fun `onCompleteOnboarding persists preferences and invokes onFinish`() = runTest {
        coEvery {
            mockCompleteOnboardingUseCase(
                notificationsEnabled = true,
                microphoneEnabled = false,
                quietHoursEnabled = true,
            )
        } just runs

        sut.onNotificationsToggled(true)
        sut.onMicrophoneToggled(false)
        sut.onQuietHoursToggled(true)

        var finished = false
        sut.onCompleteOnboarding { finished = true }
        advanceUntilIdle()

        coVerify(exactly = 1) {
            mockCompleteOnboardingUseCase(
                notificationsEnabled = true,
                microphoneEnabled = false,
                quietHoursEnabled = true,
            )
        }
        finished shouldBeEqualTo true
        sut.uiStateFlow.value.isCompleting shouldBeEqualTo false
    }
}
