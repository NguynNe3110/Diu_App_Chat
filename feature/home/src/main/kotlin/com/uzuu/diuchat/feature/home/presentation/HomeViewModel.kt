package com.uzuu.diuchat.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import com.uzuu.diuchat.feature.home.domain.usecase.DeleteHomeConversationUseCase
import com.uzuu.diuchat.feature.home.domain.usecase.GetHomeQuietHoursUseCase
import com.uzuu.diuchat.feature.home.domain.usecase.GetHomeStarsUseCase
import com.uzuu.diuchat.feature.home.domain.usecase.GetRecentChatsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val deleteHomeConversationUseCase: DeleteHomeConversationUseCase,
    private val getHomeQuietHoursUseCase: GetHomeQuietHoursUseCase,
    private val getHomeStarsUseCase: GetHomeStarsUseCase,
    private val getRecentChatsUseCase: GetRecentChatsUseCase,
) : BaseViewModel<HomeUiState, HomeAction>(HomeUiState()) {
    private var observeJob: Job? = null

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        sendAction(HomeAction.SetLoading(true))
        observeJob?.cancel()
        observeJob =
            getHomeQuietHoursUseCase()
                .flatMapLatest { quietStatus ->
                    combine(
                        getHomeStarsUseCase(quietStatus.isActive),
                        getRecentChatsUseCase(),
                    ) { stars, recentChats ->
                        HomeAction.SetData(
                            stars = stars,
                            recentChats = recentChats,
                            isQuietModeActive = quietStatus.isActive,
                            quietBannerText = quietStatus.bannerText,
                        )
                    }
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

    fun onDeleteConversation(conversationId: String) {
        viewModelScope.launch {
            deleteHomeConversationUseCase(conversationId)
        }
    }
}
