package com.uzuu.diuchat.feature.onboarding.domain.repository

interface OnBoardingRepository {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
}
