package com.uzuu.diuchat.feature.settings.domain.model

data class UserProfile(
    val id: String = "user_me",
    val displayName: String = "Minh",
    val username: String = "minh_diu",
    val bio: String = "Tìm kiếm những điều dịu dàng và bình yên mỗi ngày.",
    val birthday: String = "15/08/2000",
    val phone: String = "+84901234567",
    val primaryMood: String = "Bình yên",
    val avatarUrl: String? = null,
)
