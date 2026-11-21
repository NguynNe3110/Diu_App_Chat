package com.uzuu.diuchat.feature.chat.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id")
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("sender_name")
    val senderName: String = "Tôi",
    @SerialName("content")
    val content: String,
    @SerialName("mood")
    val mood: String = "binh_yen",
    @SerialName("status")
    val status: String = "SENT",
    @SerialName("reply_to_id")
    val replyToId: String? = null,
    @SerialName("reply_quote")
    val replyQuote: String? = null,
    @SerialName("created_at_epoch_ms")
    val createdAtEpochMs: Long = 0L,
    @SerialName("is_from_me")
    val isFromMe: Boolean = false,
    @SerialName("media_type")
    val mediaType: String = "TEXT",
    @SerialName("media_uri")
    val mediaUri: String? = null,
    @SerialName("audio_duration_ms")
    val audioDurationMs: Long? = null,
    @SerialName("audio_waveform_csv")
    val audioWaveformCsv: String? = null,
    @SerialName("caption")
    val caption: String? = null,
    @SerialName("is_pinned")
    val isPinned: Boolean = false,
    @SerialName("reactions_csv")
    val reactionsCsv: String? = null,
)
