package com.uzuu.diuchat.feature.home.data.repository

import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.home.data.datasource.api.service.HomeSupabaseService
import com.uzuu.diuchat.feature.home.data.mapper.HomeMapper
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeRepositoryImpl(
    private val homeSupabaseService: HomeSupabaseService,
    private val homeMapper: HomeMapper,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : HomeRepository {
    private val starsState = MutableStateFlow(seedStars())
    private val recentChatsState = MutableStateFlow(seedRecentChats())

    init {
        coroutineScope.launch {
            syncFromRemote()
        }
    }

    override fun getStars(isQuiet: Boolean): Flow<List<StarPerson>> = starsState.map { stars ->
        stars.map { star ->
            if (star.id == "minh") star.copy(isQuiet = isQuiet) else star
        }
    }

    override fun getRecentChats(): Flow<List<RecentChat>> = recentChatsState.asStateFlow()

    override suspend fun refreshFromRemote() {
        syncFromRemote()
    }

    suspend fun syncFromRemote() {
        val overview = homeSupabaseService.fetchOverview() ?: return
        if (homeSupabaseService.isAuthenticated()) {
            starsState.value = overview.stars.map(homeMapper::toDomain)
            recentChatsState.value = overview.recentChats.map(homeMapper::toDomain)
        } else {
            if (overview.stars.isNotEmpty()) {
                starsState.value = overview.stars.map(homeMapper::toDomain)
            }
            if (overview.recentChats.isNotEmpty()) {
                recentChatsState.value = overview.recentChats.map(homeMapper::toDomain)
            }
        }
    }

    companion object {
        private fun seedStars(): List<StarPerson> = listOf(
            StarPerson(
                id = "minh",
                name = "Minh",
                initials = "M",
                moodColor = AppPalette.MoodThuong,
                size = 92.dp,
                xPercent = 0.50f,
                yPercent = 0.18f,
                isQuiet = false,
            ),
            StarPerson(
                id = "nha",
                name = "Nhà mình",
                initials = "N",
                moodColor = AppPalette.MoodVui,
                size = 66.dp,
                xPercent = 0.18f,
                yPercent = 0.38f,
                memberCount = 3,
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

        private fun seedRecentChats(): List<RecentChat> = listOf(
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
    }
}
