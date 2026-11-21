package com.uzuu.diuchat.feature.media.data.datasource.api.response

import com.uzuu.diuchat.feature.media.data.datasource.api.model.MediaVaultItemDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaVaultResponse(
    @SerialName("conversation_id")
    val conversationId: String = "",
    @SerialName("items")
    val items: List<MediaVaultItemDto> = emptyList(),
)
