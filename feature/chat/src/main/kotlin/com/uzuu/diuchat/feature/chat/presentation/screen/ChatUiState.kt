package com.uzuu.diuchat.feature.chat.presentation.screen

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState

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
    val voiceRecordingState: VoiceRecordingState = VoiceRecordingState.Idle,
    val pendingPhotoUri: String? = null,
    val playingAudioMessageId: String? = null,
    val audioPlaybackProgress: Float = 0f,
    val currentConversation: Conversation? = null,
    val pinnedSnippet: String? = null,
    val activeReactionDetailMessage: ChatMessage? = null,
    val activeForwardMessage: ChatMessage? = null,
    val availableConversations: List<Conversation> = emptyList(),
    val isChatInfoOpen: Boolean = false,
    val isNewGroupOpen: Boolean = false,
    val isSlowSendSheetOpen: Boolean = false,
    val isAttachmentSheetOpen: Boolean = false,
    val slowSendDraftContent: String = "",
    val slowSendScheduleLabel: String = "Sáng mai 7:00",
    val isGroupEditSheetOpen: Boolean = false,
    val newGroupSelectedMemberIds: List<String> = emptyList(),
    val newGroupTitle: String = "",
    val newGroupMood: MoodType = MoodType.THUONG,
) : BaseState
