package com.uzuu.diuchat.feature.home.data.datasource.api.service

import com.uzuu.diuchat.feature.home.data.datasource.api.model.HomeStarDto
import com.uzuu.diuchat.feature.home.data.datasource.api.model.RecentChatDto
import com.uzuu.diuchat.feature.home.data.datasource.api.response.HomeOverviewResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlin.coroutines.cancellation.CancellationException
import timber.log.Timber

interface HomeSupabaseService {
    suspend fun fetchOverview(): HomeOverviewResponse?

    suspend fun fetchStars(): List<HomeStarDto>?

    suspend fun fetchRecentChats(): List<RecentChatDto>?
}

class HomeSupabaseServiceImpl(
    private val supabaseClient: SupabaseClient?,
) : HomeSupabaseService {
    override suspend fun fetchOverview(): HomeOverviewResponse? {
        val stars = fetchStars() ?: return null
        val recentChats = fetchRecentChats() ?: return null
        return HomeOverviewResponse(stars = stars, recentChats = recentChats)
    }

    override suspend fun fetchStars(): List<HomeStarDto>? {
        val client = supabaseClient ?: return null
        return runCatching {
            client.postgrest
                .from(TABLE_HOME_STARS)
                .select()
                .decodeList<HomeStarDto>()
        }.onFailure { error ->
            if (error is CancellationException) throw error
            Timber.w(error, "Failed to fetch home stars from Supabase")
        }.getOrNull()
    }

    override suspend fun fetchRecentChats(): List<RecentChatDto>? {
        val client = supabaseClient ?: return null
        return runCatching {
            client.postgrest
                .from(TABLE_RECENT_CHATS)
                .select()
                .decodeList<RecentChatDto>()
        }.onFailure { error ->
            if (error is CancellationException) throw error
            Timber.w(error, "Failed to fetch recent chats from Supabase")
        }.getOrNull()
    }

    private companion object {
        const val TABLE_HOME_STARS = "home_stars"
        const val TABLE_RECENT_CHATS = "recent_chats"
    }
}
