package com.uzuu.diuchat.feature.home.presentation

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import java.util.Calendar

private const val MORNING_START_HOUR = 5
private const val MORNING_END_HOUR = 11
private const val AFTERNOON_START_HOUR = 12
private const val AFTERNOON_END_HOUR = 17

sealed interface HomeAction : BaseAction<HomeUiState> {
    data class SetLoading(
        val isLoading: Boolean,
    ) : HomeAction {
        override fun reduce(state: HomeUiState): HomeUiState = state.copy(isLoading = isLoading)
    }

    data class SetData(
        val stars: List<StarPerson>,
        val recentChats: List<RecentChat>,
        val isQuietModeActive: Boolean,
        val userName: String = "",
        val quietBannerText: String = "",
    ) : HomeAction {
        override fun reduce(state: HomeUiState): HomeUiState = state.copy(
            isLoading = false,
            stars = stars,
            recentChats = recentChats,
            isQuietModeActive = isQuietModeActive,
            quietBannerText = quietBannerText.ifBlank { state.quietBannerText },
            userName = userName.ifBlank { state.userName },
            greeting = computeGreeting(),
        )

        private fun computeGreeting(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in MORNING_START_HOUR..MORNING_END_HOUR -> "Chào buổi sáng"
                in AFTERNOON_START_HOUR..AFTERNOON_END_HOUR -> "Chào buổi chiều"
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

    data class DismissQuietBanner(
        val dismiss: Boolean = true,
    ) : HomeAction {
        override fun reduce(state: HomeUiState): HomeUiState = state.copy(isQuietModeActive = !dismiss)
    }
}
