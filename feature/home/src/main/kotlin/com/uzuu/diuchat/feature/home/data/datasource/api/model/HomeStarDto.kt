package com.uzuu.diuchat.feature.home.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeStarDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("initials")
    val initials: String,
    @SerialName("mood_color_hex")
    val moodColorHex: String = "#F7A3A9",
    @SerialName("size_dp")
    val sizeDp: Float = 48f,
    @SerialName("x_percent")
    val xPercent: Float = 0.5f,
    @SerialName("y_percent")
    val yPercent: Float = 0.5f,
    @SerialName("is_quiet")
    val isQuiet: Boolean = false,
    @SerialName("member_count")
    val memberCount: Int = 1,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)
