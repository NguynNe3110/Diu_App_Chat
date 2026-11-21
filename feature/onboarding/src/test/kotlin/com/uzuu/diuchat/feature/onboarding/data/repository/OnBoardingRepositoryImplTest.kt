package com.uzuu.diuchat.feature.onboarding.data.repository

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class OnBoardingRepositoryImplTest {
    private val sharedPreferences: SharedPreferences = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val sut =
        OnBoardingRepositoryImpl(
            context = context,
            preferences = sharedPreferences,
        )

    @Test
    fun `isOnboardingCompleted returns true when sharedPreferences returns true`() {
        every {
            sharedPreferences.getBoolean(OnBoardingRepositoryImpl.KEY_ONBOARDING_COMPLETED, false)
        } returns true

        val actual = sut.isOnboardingCompleted()

        actual shouldBeEqualTo true
    }

    @Test
    fun `setOnboardingCompleted writes boolean to sharedPreferences`() {
        val editor: SharedPreferences.Editor = mockk(relaxed = true)
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor

        sut.setOnboardingCompleted(true)

        verify { editor.putBoolean(OnBoardingRepositoryImpl.KEY_ONBOARDING_COMPLETED, true) }
        verify { editor.apply() }
    }
}
