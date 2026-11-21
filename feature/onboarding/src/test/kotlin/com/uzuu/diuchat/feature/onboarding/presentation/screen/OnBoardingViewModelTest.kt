package com.uzuu.diuchat.feature.onboarding.presentation.screen

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class OnBoardingViewModelTest {
    private val sut = OnBoardingViewModel()

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
}
