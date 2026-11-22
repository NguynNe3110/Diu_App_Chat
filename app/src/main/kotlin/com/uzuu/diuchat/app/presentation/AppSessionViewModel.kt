package com.uzuu.diuchat.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.app.data.supabase.SupabaseSessionChecker
import com.uzuu.diuchat.feature.auth.domain.usecase.CheckHasUsernameUseCase
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import com.uzuu.diuchat.feature.settings.domain.usecase.GetOnboardingCompletedUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.SetOnboardingCompletedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface StartupDestination {
    data object Loading : StartupDestination
    data object OnBoarding : StartupDestination
    data class Auth(val requireUsernameSetup: Boolean = false) : StartupDestination
    data object Home : StartupDestination
}

class AppSessionViewModel(
    private val supabaseSessionChecker: SupabaseSessionChecker,
    private val getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase,
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
    private val checkHasUsernameUseCase: CheckHasUsernameUseCase,
    private val settingsRepository: SettingsRepository,
    private val homeRepository: HomeRepository,
    private val contactsRepository: ContactsRepository,
) : ViewModel() {
    private val _startupDestination = MutableStateFlow<StartupDestination>(StartupDestination.Loading)
    val startupDestination: StateFlow<StartupDestination> = _startupDestination.asStateFlow()

    init {
        resolveStartupDestination()
    }

    private fun resolveStartupDestination() {
        viewModelScope.launch {
            val onboardingCompleted = runCatching {
                getOnboardingCompletedUseCase().first()
            }.getOrDefault(false)

            val isAuthenticated = supabaseSessionChecker.isAuthenticated()

            if (isAuthenticated) {
                if (!onboardingCompleted) {
                    runCatching { setOnboardingCompletedUseCase(true) }
                }
                val hasUsername = checkHasUsernameUseCase().getOrDefault(true)
                if (hasUsername) {
                    runCatching { settingsRepository.refreshFromRemote() }
                    runCatching { homeRepository.refreshFromRemote() }
                    runCatching { contactsRepository.refreshFromRemote() }
                    _startupDestination.value = StartupDestination.Home
                } else {
                    _startupDestination.value = StartupDestination.Auth(requireUsernameSetup = true)
                }
            } else {
                _startupDestination.value = if (onboardingCompleted) {
                    StartupDestination.Auth(requireUsernameSetup = false)
                } else {
                    StartupDestination.OnBoarding
                }
            }
        }
    }

    fun markOnboardingCompleted() {
        viewModelScope.launch {
            setOnboardingCompletedUseCase(true)
        }
    }

    fun onAuthenticated() {
        viewModelScope.launch {
            setOnboardingCompletedUseCase(true)
            runCatching { settingsRepository.refreshFromRemote() }
            runCatching { homeRepository.refreshFromRemote() }
            runCatching { contactsRepository.refreshFromRemote() }
        }
    }

    fun onLoggedOut() {
        viewModelScope.launch {
            runCatching { homeRepository.clearLocalData() }
            runCatching { contactsRepository.clearLocalData() }
        }
    }
}
