package com.uzuu.diuchat.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import com.uzuu.diuchat.feature.home.domain.usecase.GetHomeStarsUseCase
import com.uzuu.diuchat.feature.home.domain.usecase.GetRecentChatsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar

private const val QUIET_START_HOUR = 22
private const val QUIET_END_HOUR = 6

class HomeViewModel(
    private val getHomeStarsUseCase: GetHomeStarsUseCase,
    private val getRecentChatsUseCase: GetRecentChatsUseCase,
) : BaseViewModel<HomeUiState, HomeAction>(HomeUiState()) {
    private var observeJob: Job? = null

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isQuiet = hour >= QUIET_START_HOUR || hour < QUIET_END_HOUR
        observeJob?.cancel()
        observeJob =
            combine(
                getHomeStarsUseCase(isQuiet),
                getRecentChatsUseCase(),
            ) { stars, recentChats ->
                HomeAction.SetData(stars, recentChats, isQuiet)
            }.onEach { action ->
                sendAction(action)
            }.launchIn(viewModelScope)
    }

    fun onClearData() {
        sendAction(HomeAction.ClearData)
    }

    fun onDismissQuietBanner(dismiss: Boolean = true) {
        sendAction(HomeAction.DismissQuietBanner(dismiss))
    }

    fun onSetData(
        stars: List<StarPerson>,
        recentChats: List<RecentChat>,
        isQuietModeActive: Boolean,
    ) {
        sendAction(HomeAction.SetData(stars, recentChats, isQuietModeActive))
    }

    fun onSetLoading(isLoading: Boolean) {
        sendAction(HomeAction.SetLoading(isLoading))
    }
}
