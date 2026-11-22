package com.uzuu.diuchat.feature.chat.data.datasource.api.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MessageDto(
    @SerialName("id")
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("reply_to_id")
    val replyToId: String? = null,
    @SerialName("forwarded_from_id")
    val forwardedFromId: String? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("type")
    val type: String = "text",
    @SerialName("content")
    val content: String? = null,
    @SerialName("media_url")
    val mediaUrl: String? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("mood_id")
    val moodId: String = "thuong",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("metadata")
    val metadata: JsonObject = buildJsonObject {},
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("is_pinned")
    val isPinned: Boolean = false,
    @SerialName("deleted_at")
    val deletedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)
