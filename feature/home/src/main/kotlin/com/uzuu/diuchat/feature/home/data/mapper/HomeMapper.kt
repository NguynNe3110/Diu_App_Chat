package com.uzuu.diuchat.feature.home.data.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.home.data.datasource.api.model.HomeStarDto
import com.uzuu.diuchat.feature.home.data.datasource.api.model.RecentChatDto
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import java.util.Locale
import kotlin.math.roundToInt

class HomeMapper {
    fun toDomain(dto: HomeStarDto): StarPerson = StarPerson(
        id = dto.id,
        name = dto.name,
        initials = dto.initials,
        moodColor = parseMoodColor(dto.moodColorHex),
        size = dto.sizeDp.dp,
        xPercent = dto.xPercent,
        yPercent = dto.yPercent,
        isQuiet = dto.isQuiet,
        memberCount = dto.memberCount,
    )

    fun toDto(domain: StarPerson): HomeStarDto = HomeStarDto(
        id = domain.id,
        name = domain.name,
        initials = domain.initials,
        moodColorHex = domain.moodColor.toHexString(),
        sizeDp = domain.size.value,
        xPercent = domain.xPercent,
        yPercent = domain.yPercent,
        isQuiet = domain.isQuiet,
        memberCount = domain.memberCount,
    )

    fun toDomain(dto: RecentChatDto): RecentChat = RecentChat(
        id = dto.id,
        contactName = dto.contactName,
        initials = dto.initials,
        moodColor = parseMoodColor(dto.moodColorHex),
        lastMessage = dto.lastMessage,
        timestamp = dto.timestamp,
        unreadCount = dto.unreadCount,
        isPendingLetter = dto.isPendingLetter,
        isQuiet = dto.isQuiet,
    )

    fun toDto(domain: RecentChat): RecentChatDto = RecentChatDto(
        id = domain.id,
        contactName = domain.contactName,
        initials = domain.initials,
        moodColorHex = domain.moodColor.toHexString(),
        lastMessage = domain.lastMessage,
        timestamp = domain.timestamp,
        unreadCount = domain.unreadCount,
        isPendingLetter = domain.isPendingLetter,
        isQuiet = domain.isQuiet,
    )

    private fun parseMoodColor(hex: String): Color {
        val cleaned = hex.trim().removePrefix("#")
        val value = cleaned.toLongOrNull(HEX_RADIX) ?: return AppPalette.MoodThuong
        val argb =
            when (cleaned.length) {
                HEX_RGB_LENGTH -> ALPHA_MASK or value
                HEX_ARGB_LENGTH -> value
                else -> return AppPalette.MoodThuong
            }
        return Color(argb.toInt())
    }

    private fun Color.toHexString(): String {
        val r = (red * RGB_MAX).roundToInt().coerceIn(0, RGB_MAX)
        val g = (green * RGB_MAX).roundToInt().coerceIn(0, RGB_MAX)
        val b = (blue * RGB_MAX).roundToInt().coerceIn(0, RGB_MAX)
        return String.format(Locale.US, "#%02X%02X%02X", r, g, b)
    }

    private companion object {
        const val HEX_RADIX = 16
        const val HEX_RGB_LENGTH = 6
        const val HEX_ARGB_LENGTH = 8
        const val ALPHA_MASK = 0xFF000000L
        const val RGB_MAX = 255
    }
}
