package com.uzuu.diuchat.feature.contacts.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionRequestDto(
    @SerialName("id")
    val id: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("sender_name")
    val senderName: String,
    @SerialName("sender_initials")
    val senderInitials: String,
    @SerialName("sender_phone")
    val senderPhone: String,
    @SerialName("sender_mood_color_hex")
    val senderMoodColorHex: String = "#A2D7AC",
    @SerialName("proposed_circle")
    val proposedCircle: String = "FRIENDS",
    @SerialName("created_at")
    val createdAt: String = "",
)
