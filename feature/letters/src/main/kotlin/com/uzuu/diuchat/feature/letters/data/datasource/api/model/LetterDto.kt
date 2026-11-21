package com.uzuu.diuchat.feature.letters.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LetterDto(
    @SerialName("id")
    val id: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("sender_name")
    val senderName: String,
    @SerialName("recipient_id")
    val recipientId: String,
    @SerialName("recipient_name")
    val recipientName: String,
    @SerialName("title")
    val title: String,
    @SerialName("scheduled_unlock_epoch_ms")
    val scheduledUnlockEpochMs: Long,
    @SerialName("scheduled_unlock_formatted")
    val scheduledUnlockFormatted: String,
    @SerialName("status")
    val status: String = "SEALED_PENDING",
    @SerialName("is_sender")
    val isSender: Boolean = false,
    @SerialName("mood_name")
    val moodName: String = "Thương",
    @SerialName("content_payload")
    val contentPayload: String = "",
    @SerialName("opened_at_epoch_ms")
    val openedAtEpochMs: Long? = null,
    @SerialName("opened_at_formatted")
    val openedAtFormatted: String? = null,
)
