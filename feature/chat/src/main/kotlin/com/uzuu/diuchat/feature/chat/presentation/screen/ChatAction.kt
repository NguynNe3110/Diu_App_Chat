package com.uzuu.diuchat.feature.chat.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState

sealed interface ChatAction : BaseAction<ChatUiState> {
    data class SetMessages(
        val messages: List<ChatMessage>,
    ) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(messages = messages)
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
