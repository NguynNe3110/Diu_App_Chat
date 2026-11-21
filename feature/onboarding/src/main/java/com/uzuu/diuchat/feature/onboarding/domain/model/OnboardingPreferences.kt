package com.uzuu.diuchat.feature.onboarding.domain.model

data class OnboardingPreferences(
    val isCompleted: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val microphoneEnabled: Boolean = false,
    val quietHoursEnabled: Boolean = true,
)
