package com.uzuu.diuchat.feature.settings.domain.model

data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val birthday: String = "",
    val phone: String = "",
    val primaryMood: String = "Bình yên",
    val avatarUrl: String? = null,
)
