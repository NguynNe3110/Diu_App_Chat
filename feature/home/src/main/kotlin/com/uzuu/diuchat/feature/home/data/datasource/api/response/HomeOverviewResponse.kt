package com.uzuu.diuchat.feature.home.data.datasource.api.response

import com.uzuu.diuchat.feature.home.data.datasource.api.model.HomeStarDto
import com.uzuu.diuchat.feature.home.data.datasource.api.model.RecentChatDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeOverviewResponse(
    @SerialName("stars")
    val stars: List<HomeStarDto> = emptyList(),
    @SerialName("recent_chats")
    val recentChats: List<RecentChatDto> = emptyList(),
)
