package com.uzuu.diuchat.feature.home.presentation

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import java.util.Calendar

sealed interface HomeAction : BaseAction<HomeUiState> {

    data class SetLoading(val isLoading: Boolean) : HomeAction {
        override fun reduce(state: HomeUiState): HomeUiState = state.copy(isLoading = isLoading)
    }

    data class SetData(
        val stars: List<StarPerson>,
        val recentChats: List<RecentChat>,
        val isQuietModeActive: Boolean,
    ) : HomeAction {
        override fun reduce(state: HomeUiState): HomeUiState = state.copy(
            isLoading = false,
            stars = stars,
            recentChats = recentChats,
            isQuietModeActive = isQuietModeActive,
            greeting = computeGreeting(),
        )

        private fun computeGreeting(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 5..11 -> "Chào buổi sáng"
                in 12..17 -> "Chào buổi chiều"
                else -> "Chào buổi tối"
            }
        }
    }

    data object ClearData : HomeAction {
        override fun reduce(state: HomeUiState): HomeUiState = state.copy(
            isLoading = false,
            stars = emptyList(),
            recentChats = emptyList(),
        )
    }

    data class DismissQuietBanner(val dismiss: Boolean = true) : HomeAction {
        override fun reduce(state: HomeUiState): HomeUiState = state.copy(isQuietModeActive = !dismiss)
    }
}
