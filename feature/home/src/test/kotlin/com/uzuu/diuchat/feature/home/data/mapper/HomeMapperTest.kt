package com.uzuu.diuchat.feature.home.data.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.home.data.datasource.api.model.HomeStarDto
import com.uzuu.diuchat.feature.home.data.datasource.api.model.RecentChatDto
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeMapperTest {
    private val mapper = HomeMapper()

    @Test
    fun `toDomain maps HomeStarDto to StarPerson with parsed color and dp`() {
        val dto = HomeStarDto(
            id = "star-1",
            name = "Minh",
            initials = "M",
            moodColorHex = "#F7A3A9",
            sizeDp = 92f,
            xPercent = 0.5f,
            yPercent = 0.2f,
            isQuiet = true,
            memberCount = 3,
        )

        val domain = mapper.toDomain(dto)

        assertEquals("star-1", domain.id)
        assertEquals("Minh", domain.name)
        assertEquals("M", domain.initials)
        assertEquals(Color(0xFFF7A3A9), domain.moodColor)
        assertEquals(92.dp, domain.size)
        assertEquals(0.5f, domain.xPercent)
        assertEquals(0.2f, domain.yPercent)
        assertTrue(domain.isQuiet)
        assertEquals(3, domain.memberCount)
    }

    @Test
    fun `toDomain falls back to MoodThuong when hex is invalid`() {
        val dto = HomeStarDto(
            id = "star-2",
            name = "Thảo",
            initials = "T",
            moodColorHex = "not-a-hex",
        )

        val domain = mapper.toDomain(dto)

        assertEquals(AppPalette.MoodThuong, domain.moodColor)
    }

    @Test
    fun `toDto maps StarPerson back to HomeStarDto`() {
        val domain = StarPerson(
            id = "star-3",
            name = "Bảo Vy",
            initials = "BV",
            moodColor = Color(0xFFA2D7AC),
            size = 64.dp,
            xPercent = 0.7f,
            yPercent = 0.8f,
            isQuiet = false,
            memberCount = 1,
        )

        val dto = mapper.toDto(domain)

        assertEquals("star-3", dto.id)
        assertEquals("#A2D7AC", dto.moodColorHex)
        assertEquals(64f, dto.sizeDp)
        assertFalse(dto.isQuiet)
    }

    @Test
    fun `toDomain and toDto roundtrip RecentChat`() {
        val dto = RecentChatDto(
            id = "chat-1",
            contactName = "Nhà mình",
            initials = "N",
            moodColorHex = "#F4CA84",
            lastMessage = "Cuối tuần về ăn cơm",
            timestamp = "19:30",
            unreadCount = 2,
            isPendingLetter = true,
            isQuiet = false,
        )

        val domain = mapper.toDomain(dto)
        val roundTrip = mapper.toDto(domain)

        assertEquals(dto.id, domain.id)
        assertEquals(dto.contactName, domain.contactName)
        assertEquals(Color(0xFFF4CA84), domain.moodColor)
        assertEquals(2, domain.unreadCount)
        assertTrue(domain.isPendingLetter)
        assertEquals(dto, roundTrip)
    }

    @Test
    fun `toDto maps RecentChat domain to RecentChatDto`() {
        val domain = RecentChat(
            id = "chat-2",
            contactName = "Minh",
            initials = "M",
            moodColor = Color(0xFFC5B3EA),
            lastMessage = "Ngủ ngon nhé",
            timestamp = "22:00",
            unreadCount = 0,
            isPendingLetter = false,
            isQuiet = true,
        )

        val dto = mapper.toDto(domain)

        assertEquals("#C5B3EA", dto.moodColorHex)
        assertTrue(dto.isQuiet)
    }
}
