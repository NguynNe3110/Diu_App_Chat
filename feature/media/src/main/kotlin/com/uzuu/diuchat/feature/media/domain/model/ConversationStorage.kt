package com.uzuu.diuchat.feature.media.domain.model

import androidx.compose.ui.graphics.Color
import com.uzuu.diuchat.feature.base.common.res.AppPalette

data class ConversationStorage(
    val conversationId: String,
    val contactName: String,
    val initials: String,
    val moodColor: Color = AppPalette.MoodBinhYen,
    val totalBytes: Long,
    val photoCount: Int,
    val voiceCount: Int,
) {
    val formattedSize: String
        get() = formatBytes(totalBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            val mb = bytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format(java.util.Locale.US, "%.1f GB", mb / 1024.0)
            } else {
                String.format(java.util.Locale.US, "%.1f MB", mb)
            }
        }
    }
}
