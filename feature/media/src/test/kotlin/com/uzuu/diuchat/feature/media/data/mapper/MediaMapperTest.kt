package com.uzuu.diuchat.feature.media.data.mapper

import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.media.data.datasource.api.model.MediaVaultItemDto
import com.uzuu.diuchat.feature.media.domain.model.MediaType
import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class MediaMapperTest {
    private val sut = MediaMapper()

    @Test
    fun `dtoToDomain maps all fields and mood color accurately`() {
        val dto = MediaVaultItemDto(
            id = "p1",
            conversationId = "minh",
            type = "PHOTO",
            url = "https://example.com/photo.jpg",
            caption = "Hoàng hôn",
            moodName = "Thương",
            moodColorHex = "#F7A3A9",
            durationSeconds = 0,
            timestamp = "17:15 · 18/11/2026",
            monthGroup = "Tháng 11, 2026",
            senderName = "Minh",
        )

        val domain = sut.dtoToDomain(dto)

        domain shouldBeEqualTo MediaVaultItem(
            id = "p1",
            conversationId = "minh",
            type = MediaType.PHOTO,
            url = "https://example.com/photo.jpg",
            caption = "Hoàng hôn",
            moodName = "Thương",
            moodColor = AppPalette.MoodThuong,
            durationSeconds = 0,
            timestamp = "17:15 · 18/11/2026",
            monthGroup = "Tháng 11, 2026",
            senderName = "Minh",
        )
    }

    @Test
    fun `domainToDto and dtoToDomain round trip preserves voice and mood`() {
        val domain = MediaVaultItem(
            id = "v1",
            conversationId = "nha",
            type = MediaType.VOICE,
            url = "https://example.com/voice.m4a",
            caption = "Lời nhắn buổi tối",
            moodName = "Nhớ",
            moodColor = AppPalette.MoodNho,
            durationSeconds = 42,
            timestamp = "21:00 · 20/11/2026",
            monthGroup = "Tháng 11, 2026",
            senderName = "Thảo",
        )

        val dto = sut.domainToDto(domain)
        val roundTripped = sut.dtoToDomain(dto)

        roundTripped shouldBeEqualTo domain
    }

    @Test
    fun `dtoToDomain falls back safely on unknown media type and hex`() {
        val dto = MediaVaultItemDto(
            id = "x1",
            conversationId = "c1",
            type = "UNKNOWN",
            moodName = "Vui",
            moodColorHex = "INVALID",
        )

        val domain = sut.dtoToDomain(dto)

        domain.type shouldBeEqualTo MediaType.PHOTO
        domain.moodColor shouldBeEqualTo AppPalette.MoodVui
    }
}
