package com.uzuu.diuchat.feature.media.data.mapper

import androidx.compose.ui.graphics.Color
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.media.data.datasource.api.model.MediaVaultItemDto
import com.uzuu.diuchat.feature.media.domain.model.MediaType
import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem

class MediaMapper {
    fun dtoToDomain(dto: MediaVaultItemDto): MediaVaultItem = MediaVaultItem(
        id = dto.id,
        conversationId = dto.conversationId,
        type = runCatching { MediaType.valueOf(dto.type.uppercase()) }.getOrDefault(MediaType.PHOTO),
        url = dto.url,
        caption = dto.caption,
        moodName = dto.moodName,
        moodColor = hexToMoodColor(dto.moodColorHex, dto.moodName),
        durationSeconds = dto.durationSeconds,
        timestamp = dto.timestamp,
        monthGroup = dto.monthGroup,
        senderName = dto.senderName,
    )

    fun domainToDto(domain: MediaVaultItem): MediaVaultItemDto = MediaVaultItemDto(
        id = domain.id,
        conversationId = domain.conversationId,
        type = domain.type.name,
        url = domain.url,
        caption = domain.caption,
        moodName = domain.moodName,
        moodColorHex = moodColorToHex(domain.moodColor),
        durationSeconds = domain.durationSeconds,
        timestamp = domain.timestamp,
        monthGroup = domain.monthGroup,
        senderName = domain.senderName,
    )

    private fun hexToMoodColor(
        hex: String,
        moodName: String,
    ): Color = when (hex.uppercase()) {
        HEX_THUONG -> AppPalette.MoodThuong
        HEX_VUI -> AppPalette.MoodVui
        HEX_BINH_YEN -> AppPalette.MoodBinhYen
        HEX_NHO -> AppPalette.MoodNho
        else -> moodNameToColor(moodName)
    }

    private fun moodNameToColor(moodName: String): Color = when (moodName) {
        "Thương" -> AppPalette.MoodThuong
        "Vui" -> AppPalette.MoodVui
        "Nhớ" -> AppPalette.MoodNho
        else -> AppPalette.MoodBinhYen
    }

    private fun moodColorToHex(color: Color): String = when (color) {
        AppPalette.MoodThuong -> HEX_THUONG
        AppPalette.MoodVui -> HEX_VUI
        AppPalette.MoodNho -> HEX_NHO
        else -> HEX_BINH_YEN
    }

    private companion object {
        const val HEX_THUONG = "#F7A3A9"
        const val HEX_VUI = "#F4CA84"
        const val HEX_BINH_YEN = "#A2D7AC"
        const val HEX_NHO = "#C5B3EA"
    }
}
