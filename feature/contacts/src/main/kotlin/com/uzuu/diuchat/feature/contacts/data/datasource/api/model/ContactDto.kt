package com.uzuu.diuchat.feature.contacts.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("initials")
    val initials: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("circle")
    val circle: String = "FRIENDS",
    @SerialName("mood_color_hex")
    val moodColorHex: String = "#F7A3A9",
    @SerialName("nickname")
    val nickname: String? = null,
    @SerialName("anniversary")
    val anniversary: String? = null,
    @SerialName("is_quiet")
    val isQuiet: Boolean = false,
    @SerialName("last_seen")
    val lastSeen: String? = null,
)
