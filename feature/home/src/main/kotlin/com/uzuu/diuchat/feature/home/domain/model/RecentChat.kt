package com.uzuu.diuchat.feature.home.domain.model

import androidx.compose.ui.graphics.Color

data class RecentChat(
    val id: String,
    val contactName: String,
    val initials: String,
    val moodColor: Color,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isPendingLetter: Boolean = false,
    val isQuiet: Boolean = false,
    val avatarUrl: String? = null,
)
