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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeRepositoryImpl(
    private val homeSupabaseService: HomeSupabaseService,
    private val homeMapper: HomeMapper,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : HomeRepository {
    private val starsFlow = MutableSharedFlow<List<StarPerson>>(replay = 1)
    private val recentChatsFlow = MutableSharedFlow<List<RecentChat>>(replay = 1)
    private val userDisplayNameState = MutableStateFlow("")
    private val ioScope = coroutineScope

    init {
        if (!homeSupabaseService.isConfigured()) {
            starsFlow.tryEmit(seedStars())
            recentChatsFlow.tryEmit(seedRecentChats())
        }
        ioScope.launch {
            syncFromRemote()
        }
        if (homeSupabaseService.isConfigured()) {
            ioScope.launch {
                homeSupabaseService.observeRealtimeUpdates().collect {
                    syncFromRemote()
                }
            }
        }
    }

    override fun getStars(isQuiet: Boolean): Flow<List<StarPerson>> {
        if (starsFlow.replayCache.isEmpty() && homeSupabaseService.isAuthenticated()) {
            ioScope.launch { syncFromRemote() }
        }
        return starsFlow.asSharedFlow().map { stars ->
            stars.map { star ->
                if (star.id == "minh") star.copy(isQuiet = isQuiet) else star
            }
        }
    }

    override fun getRecentChats(): Flow<List<RecentChat>> = recentChatsFlow.asSharedFlow()

    override fun getUserDisplayName(): Flow<String> = userDisplayNameState.asStateFlow()

    override suspend fun refreshFromRemote() {
        syncFromRemote()
    }

    override suspend fun clearLocalData() {
        if (homeSupabaseService.isConfigured()) {
            starsFlow.tryEmit(emptyList())
            recentChatsFlow.tryEmit(emptyList())
            userDisplayNameState.value = ""
        }
    }

    override suspend fun deleteConversation(conversationId: String) {
        starsFlow.replayCache.firstOrNull()?.let { current ->
            starsFlow.tryEmit(current.filterNot { it.id == conversationId })
        }
        recentChatsFlow.replayCache.firstOrNull()?.let { current ->
            recentChatsFlow.tryEmit(current.filterNot { it.id == conversationId })
        }
        homeSupabaseService.deleteConversation(conversationId)
        syncFromRemote()
    }

    suspend fun syncFromRemote() {
        val overview = homeSupabaseService.fetchOverview()
        if (overview == null) {
            if (homeSupabaseService.isConfigured() && homeSupabaseService.isAuthenticated() && starsFlow.replayCache.isEmpty()) {
                starsFlow.tryEmit(emptyList())
                recentChatsFlow.tryEmit(emptyList())
            }
            return
        }
        overview.userDisplayName?.let { userDisplayNameState.value = it }
        if (homeSupabaseService.isAuthenticated()) {
            starsFlow.tryEmit(overview.stars.map(homeMapper::toDomain))
            recentChatsFlow.tryEmit(overview.recentChats.map(homeMapper::toDomain))
        } else {
            if (overview.stars.isNotEmpty()) {
                starsFlow.tryEmit(overview.stars.map(homeMapper::toDomain))
            }
            if (overview.recentChats.isNotEmpty()) {
                recentChatsFlow.tryEmit(overview.recentChats.map(homeMapper::toDomain))
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
