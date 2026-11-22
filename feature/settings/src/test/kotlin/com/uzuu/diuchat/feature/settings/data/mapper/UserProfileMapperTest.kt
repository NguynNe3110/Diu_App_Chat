package com.uzuu.diuchat.feature.settings.data.mapper

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class UserProfileMapperTest {
    private val mapper = UserProfileMapper()

    @Test
    fun `toDomain maps all fields from UserProfileDto`() {
        val dto = UserProfileDto(
            id = "u-1",
            displayName = "Linh",
            username = "linh_diu",
            bio = "Bình yên",
            birthday = "01/01/2001",
            phone = "+84901234567",
            primaryMood = "Vui vẻ",
            avatarUrl = "https://example.com/avatar.png",
        )

        val domain = mapper.toDomain(dto)

        domain shouldBeEqualTo UserProfile(
            id = "u-1",
            displayName = "Linh",
            username = "linh_diu",
            bio = "Bình yên",
            birthday = "01/01/2001",
            phone = "+84901234567",
            primaryMood = "Vui vẻ",
            avatarUrl = "https://example.com/avatar.png",
        )
    }

    @Test
    fun `toDto maps all fields from UserProfile`() {
        val domain = UserProfile(
            id = "u-2",
            displayName = "Minh",
            username = "minh_diu",
            bio = "Nhẹ nhàng",
            birthday = "15/08/2000",
            phone = "+84909998887",
            primaryMood = "Bình yên",
            avatarUrl = null,
        )

        val dto = mapper.toDto(domain)

        dto shouldBeEqualTo UserProfileDto(
            id = "u-2",
            displayName = "Minh",
            username = "minh_diu",
            bio = "Nhẹ nhàng",
            birthday = "15/08/2000",
            phone = "+84909998887",
            primaryMood = "binhyen",
            avatarUrl = null,
        )
    }
}
