package com.uzuu.diuchat.feature.media.domain.model

import androidx.compose.ui.graphics.Color
import com.uzuu.diuchat.feature.base.common.res.AppPalette

data class MediaVaultItem(
    val id: String,
    val conversationId: String,
    val type: MediaType,
    val url: String = "",
    val caption: String = "",
    val moodName: String = "Bình yên",
    val moodColor: Color = AppPalette.MoodBinhYen,
    val durationSeconds: Int = 0,
    val timestamp: String = "",
    val monthGroup: String = "Tháng 11, 2026",
    val senderName: String = "Bạn",
)
