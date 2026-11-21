package com.uzuu.diuchat.feature.auth.data.mapper

import com.uzuu.diuchat.feature.auth.data.datasource.api.model.UserProfileDto

class AuthMapper {
    fun hasUsername(dto: UserProfileDto?): Boolean = !dto?.username.isNullOrBlank()

    fun toDisplayName(dto: UserProfileDto?): String = dto?.displayName?.takeIf { it.isNotBlank() } ?: DEFAULT_DISPLAY_NAME

    fun toDto(
        id: String,
        username: String?,
        displayName: String?,
        avatarUrl: String?,
    ): UserProfileDto = UserProfileDto(
        id = id,
        username = username?.trim()?.removePrefix("@")?.lowercase(),
        displayName = displayName?.takeIf { it.isNotBlank() },
        avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
    )

    private companion object {
        private const val DEFAULT_DISPLAY_NAME = "Người thương"
    }
}
