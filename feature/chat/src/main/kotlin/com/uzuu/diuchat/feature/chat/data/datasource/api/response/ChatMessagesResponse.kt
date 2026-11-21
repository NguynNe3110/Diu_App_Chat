package com.uzuu.diuchat.feature.chat.data.datasource.api.response

import com.uzuu.diuchat.feature.chat.data.datasource.api.model.ConversationDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.model.MessageDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessagesResponse(
    @SerialName("conversation_id")
    val conversationId: String = "",
    @SerialName("messages")
    val messages: List<MessageDto> = emptyList(),
    @SerialName("conversation")
    val conversation: ConversationDto? = null,
)
