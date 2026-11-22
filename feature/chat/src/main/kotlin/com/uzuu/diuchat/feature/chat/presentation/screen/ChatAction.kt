package com.uzuu.diuchat.feature.chat.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState

sealed interface ChatAction : BaseAction<ChatUiState> {
    data class SetLoadingMessages(
        val isLoading: Boolean,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isLoadingMessages = isLoading)
    }

    data class SetPartnerTyping(
        val isTyping: Boolean,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isPartnerTyping = isTyping)
    }

    data class SetMessages(
        val messages: List<ChatMessage>,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(
            messages = messages,
            isLoadingMessages = false,
        )
    }

    data class SetConversation(
        val conversation: Conversation?,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(
            currentConversation = conversation,
            conversationTitle = conversation?.customNickname ?: conversation?.title ?: state.conversationTitle,
            partnerMood = conversation?.mood ?: state.partnerMood,
            pinnedSnippet = conversation?.pinnedMessageSnippet,
        )
    }

    data class SetConversationId(
        val conversationId: String,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(conversationId = conversationId)
    }

    data class SetAvailableConversations(
        val conversations: List<Conversation>,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(availableConversations = conversations)
    }

    data class UpdateInput(
        val content: String,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(inputContent = content)
    }

    data class SelectMood(
        val mood: MoodType,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(selectedMood = mood)
    }

    data class ToggleMoodTray(
        val isOpen: Boolean,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isMoodTrayOpen = isOpen)
    }

    data class SetReplyingTo(
        val message: ChatMessage?,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(replyingTo = message)
    }

    data class SetSending(
        val isSending: Boolean,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isSending = isSending)
    }

    data class SetVoiceRecordingState(
        val recordingState: VoiceRecordingState,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(voiceRecordingState = recordingState)
    }

    data class SetPendingPhoto(
        val uri: String?,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(pendingPhotoUri = uri)
    }

    data class SetAudioPlayback(
        val messageId: String?,
        val progress: Float = 0f,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(
            playingAudioMessageId = messageId,
            audioPlaybackProgress = progress,
        )
    }

    data class SetActiveReactionDetail(
        val message: ChatMessage?,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(activeReactionDetailMessage = message)
    }

    data class SetActiveForwardMessage(
        val message: ChatMessage?,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(activeForwardMessage = message)
    }

    data class SetChatInfoOpen(
        val isOpen: Boolean,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isChatInfoOpen = isOpen)
    }

    data class SetNewGroupOpen(
        val isOpen: Boolean,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isNewGroupOpen = isOpen)
    }

    data class SetSlowSendSheetOpen(
        val isOpen: Boolean,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isSlowSendSheetOpen = isOpen)
    }

    data class SetAttachmentSheetOpen(
        val isOpen: Boolean,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isAttachmentSheetOpen = isOpen)
    }

    data class UpdateSlowSendDraft(
        val content: String,
        val scheduleLabel: String,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(
            slowSendDraftContent = content,
            slowSendScheduleLabel = scheduleLabel,
        )
    }

    data class AppendLocalMessage(
        val message: ChatMessage,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(
            messages = state.messages + message,
        )
    }

    data class ToggleRevealSealedMessage(
        val messageId: String,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(
            messages = state.messages.map { msg ->
                if (msg.id == messageId) msg.copy(isSealedRevealed = !msg.isSealedRevealed) else msg
            },
        )
    }

    data class CancelSealedMessage(
        val messageId: String,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(
            messages = state.messages.filterNot { it.id == messageId },
        )
    }

    data class SetGroupEditSheetOpen(
        val isOpen: Boolean,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isGroupEditSheetOpen = isOpen)
    }

    data class ToggleNewGroupMember(
        val memberId: String,
        val maxSelectablePeers: Int = 7,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState {
            val current = state.newGroupSelectedMemberIds
            val updated = if (current.contains(memberId)) {
                current - memberId
            } else if (current.size < maxSelectablePeers) {
                current + memberId
            } else {
                current
            }
            return state.copy(newGroupSelectedMemberIds = updated)
        }
    }

    data class UpdateNewGroupDraft(
        val title: String,
        val mood: MoodType,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(
            newGroupTitle = title,
            newGroupMood = mood,
        )
    }

    data object ClearInput : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(
            inputContent = "",
            replyingTo = null,
            isMoodTrayOpen = false,
            pendingPhotoUri = null,
            voiceRecordingState = VoiceRecordingState.Idle,
        )
    }
}
