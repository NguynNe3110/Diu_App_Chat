package com.uzuu.diuchat.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "Chào buổi tối",
    val userName: String = "",
    val isQuietModeActive: Boolean = false,
    val quietBannerText: String = "Giờ yên lặng đang bật · đến 6:00",
    val stars: List<StarPerson> = emptyList(),
    val recentChats: List<RecentChat> = emptyList(),
) : BaseState {
    val isEmpty: Boolean get() = stars.isEmpty() && recentChats.isEmpty()
}
