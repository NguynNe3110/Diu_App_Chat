package com.uzuu.diuchat.feature.home.presentation

import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import java.util.Calendar

private const val QUIET_START_HOUR = 22
private const val QUIET_END_HOUR = 6

class HomeViewModel : BaseViewModel<HomeUiState, HomeAction>(HomeUiState()) {
    init {
        loadHomeData()
    }

    fun loadHomeData() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isQuiet = hour >= QUIET_START_HOUR || hour < QUIET_END_HOUR
        sendAction(HomeAction.SetData(sampleStars(isQuiet), sampleRecentChats(), isQuiet))
    }

    private fun sampleStars(isQuiet: Boolean) = listOf(
        StarPerson(
            id = "minh",
            name = "Minh",
            initials = "M",
            moodColor = AppPalette.MoodThuong,
            size = 92.dp,
            xPercent = 0.50f,
            yPercent = 0.18f,
            isQuiet = isQuiet,
        ),
        StarPerson(
            id = "nha",
            name = "Nhà mình",
            initials = "N",
            moodColor = AppPalette.MoodVui,
            size = 66.dp,
            xPercent = 0.18f,
            yPercent = 0.38f,
        ),
        StarPerson(
            id = "thao",
            name = "Thảo",
            initials = "T",
            moodColor = AppPalette.MoodNho,
            size = 60.dp,
            xPercent = 0.82f,
            yPercent = 0.40f,
        ),
        StarPerson(
            id = "ba",
            name = "Ba",
            initials = "B",
            moodColor = AppPalette.MoodBinhYen,
            size = 46.dp,
            xPercent = 0.32f,
            yPercent = 0.72f,
        ),
        StarPerson(
            id = "vy",
            name = "Bảo Vy",
            initials = "V",
            moodColor = AppPalette.MoodTram,
            size = 42.dp,
            xPercent = 0.72f,
            yPercent = 0.75f,
        ),
    )

    private fun sampleRecentChats() = listOf(
        RecentChat(
            id = "minh",
            contactName = "Minh",
            initials = "M",
            moodColor = AppPalette.MoodThuong,
            lastMessage = "Thật hả!! Em vui quá",
            timestamp = "21:14",
            unreadCount = 0,
            isPendingLetter = true,
        ),
        RecentChat(
            id = "nha",
            contactName = "Nhà mình",
            initials = "N",
            moodColor = AppPalette.MoodVui,
            lastMessage = "Mẹ: Cuối tuần về ăn cơm nha con",
            timestamp = "19:30",
            unreadCount = 2,
        ),
        RecentChat(
            id = "thao",
            contactName = "Thảo",
            initials = "T",
            moodColor = AppPalette.MoodNho,
            lastMessage = "Mai mình đi cà phê nha?",
            timestamp = "Hôm qua",
            unreadCount = 0,
        ),
    )

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
