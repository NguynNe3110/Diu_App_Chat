package com.uzuu.diuchat.feature.onboarding.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.uzuu.diuchat.feature.onboarding.domain.repository.OnBoardingRepository

class OnBoardingRepositoryImpl(
    private val context: Context,
    private val preferences: SharedPreferences? = null,
) : OnBoardingRepository {

    private val sharedPreferences: SharedPreferences by lazy {
        preferences ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun isOnboardingCompleted(): Boolean = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    companion object {
        const val PREFS_NAME = "diu_app_prefs"
        const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
    }
}
