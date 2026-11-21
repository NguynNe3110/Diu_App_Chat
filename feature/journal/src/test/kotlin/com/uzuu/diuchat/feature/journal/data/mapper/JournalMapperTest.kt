package com.uzuu.diuchat.feature.journal.data.mapper

import com.uzuu.diuchat.feature.journal.data.datasource.api.model.MoodCheckinDto
import com.uzuu.diuchat.feature.journal.domain.model.FavouriteMemory
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class JournalMapperTest {
    private val mapper = JournalMapper()

    @Test
    fun `toDomain and toDto roundtrip MoodCheckinDto`() {
        val dto = MoodCheckinDto(
            dateIso = "2026-11-24",
            dayNumber = 24,
            moodId = "thuong",
            privateNote = "Đi dạo bờ sông",
            sharedWithContactIds = listOf("minh", "thao"),
            displayDate = "24 tháng 11",
        )

        val domain = mapper.toDomain(dto)
        domain.dateIso shouldBeEqualTo "2026-11-24"
        domain.dayNumber shouldBeEqualTo 24
        domain.mood shouldBeEqualTo JournalMoodType.THUONG
        domain.privateNote shouldBeEqualTo "Đi dạo bờ sông"
        domain.sharedWithContactIds shouldBeEqualTo listOf("minh", "thao")
        domain.displayDate shouldBeEqualTo "24 tháng 11"

        val roundtrip = mapper.toDto(domain)
        roundtrip shouldBeEqualTo dto
    }

    @Test
    fun `toDomain falls back to BINH_YEN for unknown mood id`() {
        val dto = MoodCheckinDto(
            dateIso = "2026-11-01",
            dayNumber = 1,
            moodId = "unknown_mood",
        )

        val domain = mapper.toDomain(dto)
        domain.mood shouldBeEqualTo JournalMoodType.BINH_YEN
    }

    @Test
    fun `toDomain and toDto roundtrip FavouriteMemoryDto`() {
        val domain = FavouriteMemory(
            id = "fav_1",
            senderName = "Minh",
            mood = JournalMoodType.NHO,
            content = "Nhớ Đà Lạt",
            savedDate = "14/02/2024",
            conversationId = "conv_minh",
        )

        val dto = mapper.toDto(domain)
        dto.id shouldBeEqualTo "fav_1"
        dto.moodId shouldBeEqualTo "nho"

        val roundtrip = mapper.toDomain(dto)
        roundtrip shouldBeEqualTo domain
    }
}
