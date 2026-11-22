package com.uzuu.diuchat.feature.settings.data.mapper

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile

internal class UserProfileMapper {
    fun toDomain(dto: UserProfileDto): UserProfile = UserProfile(
        id = dto.id,
        displayName = dto.displayName,
        username = dto.username.orEmpty(),
        bio = dto.bio.orEmpty(),
        birthday = dto.birthday.orEmpty(),
        phone = dto.phone.orEmpty(),
        primaryMood = dbMoodToLabel(dto.primaryMood),
        avatarUrl = dto.avatarUrl,
    )

    fun toDto(domain: UserProfile): UserProfileDto = UserProfileDto(
        id = domain.id,
        displayName = domain.displayName,
        username = domain.username.ifBlank { null },
        bio = domain.bio.ifBlank { null },
        birthday = domain.birthday.ifBlank { null },
        phone = domain.phone.ifBlank { null },
        primaryMood = labelToDbMood(domain.primaryMood),
        avatarUrl = domain.avatarUrl,
    )

    private fun dbMoodToLabel(raw: String?): String = when (raw?.lowercase()) {
        "thuong" -> "Thương"
        "vui" -> "Vui"
        "binhyen" -> "Bình yên"
        "nho" -> "Nhớ"
        "tram" -> "Trầm"
        null, "" -> "Bình yên"
        else -> raw
    }

    private fun labelToDbMood(label: String): String = when (label.trim().lowercase()) {
        "thương", "thuong" -> "thuong"
        "vui" -> "vui"
        "bình yên", "binhyen" -> "binhyen"
        "nhớ", "nho" -> "nho"
        "trầm", "tram" -> "tram"
        else -> label
    }
}
