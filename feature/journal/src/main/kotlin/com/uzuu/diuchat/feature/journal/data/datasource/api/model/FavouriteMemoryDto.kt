package com.uzuu.diuchat.feature.journal.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavouriteMemoryDto(
    @SerialName("id") val id: String,
    @SerialName("sender_name") val senderName: String,
    @SerialName("mood_id") val moodId: String,
    @SerialName("content") val content: String,
    @SerialName("saved_date") val savedDate: String,
    @SerialName("conversation_id") val conversationId: String = "",
)
