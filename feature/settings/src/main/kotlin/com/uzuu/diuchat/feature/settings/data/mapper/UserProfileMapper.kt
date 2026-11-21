package com.uzuu.diuchat.feature.settings.data.mapper

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile

internal class UserProfileMapper {
    fun toDomain(dto: UserProfileDto): UserProfile = UserProfile(
        id = dto.id,
        displayName = dto.displayName,
        username = dto.username,
        bio = dto.bio,
        birthday = dto.birthday,
        phone = dto.phone,
        primaryMood = dto.primaryMood,
        avatarUrl = dto.avatarUrl,
    )

    fun toDto(domain: UserProfile): UserProfileDto = UserProfileDto(
        id = domain.id,
        displayName = domain.displayName,
        username = domain.username,
        bio = domain.bio,
        birthday = domain.birthday,
        phone = domain.phone,
        primaryMood = domain.primaryMood,
        avatarUrl = domain.avatarUrl,
    )
}
