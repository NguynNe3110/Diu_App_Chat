package com.uzuu.diuchat.feature.auth.data.mapper

import com.uzuu.diuchat.feature.auth.data.datasource.api.model.UserProfileDto
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class AuthMapperTest {
    private val mapper = AuthMapper()

    @Test
    fun `hasUsername returns true when username is non-blank`() {
        val dto = UserProfileDto(id = "user-1", username = "minh_anh")
        mapper.hasUsername(dto) shouldBeEqualTo true
    }

    @Test
    fun `hasUsername returns false when dto is null or username blank`() {
        mapper.hasUsername(null) shouldBeEqualTo false
        mapper.hasUsername(UserProfileDto(id = "user-1", username = null)) shouldBeEqualTo false
        mapper.hasUsername(UserProfileDto(id = "user-1", username = "   ")) shouldBeEqualTo false
    }

    @Test
    fun `toDisplayName falls back to default when blank`() {
        mapper.toDisplayName(UserProfileDto(displayName = "Minh")) shouldBeEqualTo "Minh"
        mapper.toDisplayName(UserProfileDto(displayName = "  ")) shouldBeEqualTo "Người thương"
        mapper.toDisplayName(null) shouldBeEqualTo "Người thương"
    }

    @Test
    fun `toDto normalizes username and trims blank optional fields`() {
        val dto = mapper.toDto(
            id = "u-1",
            username = " @Minh_Anh ",
            displayName = "  ",
            avatarUrl = "https://example.com/a.png",
        )
        dto.id shouldBeEqualTo "u-1"
        dto.username shouldBeEqualTo "minh_anh"
        dto.displayName shouldBeEqualTo null
        dto.avatarUrl shouldBeEqualTo "https://example.com/a.png"
    }
}
