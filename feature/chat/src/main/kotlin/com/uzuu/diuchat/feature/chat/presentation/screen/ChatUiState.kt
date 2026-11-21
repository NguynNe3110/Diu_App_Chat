package com.uzuu.diuchat.feature.chat.presentation.screen

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MoodType

@Immutable
data class ChatUiState(
    val conversationId: String = "conv_linh_diu",
    val conversationTitle: String = "Linh Dịu",
    val partnerMood: MoodType = MoodType.BINHYEN,
    val isPartnerInQuietHours: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val inputContent: String = "",
    val selectedMood: MoodType = MoodType.THUONG,
    val isMoodTrayOpen: Boolean = false,
    val replyingTo: ChatMessage? = null,
    val isSending: Boolean = false,
) : BaseState
