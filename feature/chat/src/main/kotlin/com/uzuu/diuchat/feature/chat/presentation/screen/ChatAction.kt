package com.uzuu.diuchat.feature.chat.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MoodType

sealed interface ChatAction : BaseAction<ChatUiState> {
    data class SetMessages(val messages: List<ChatMessage>) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(messages = messages)
    }

    data class UpdateInput(val content: String) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(inputContent = content)
    }

    data class SelectMood(val mood: MoodType) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(selectedMood = mood)
    }

    data class ToggleMoodTray(val isOpen: Boolean) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isMoodTrayOpen = isOpen)
    }

    data class SetReplyingTo(val message: ChatMessage?) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(replyingTo = message)
    }

    data class SetSending(val isSending: Boolean) : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState = state.copy(isSending = isSending)
    }

    data object ClearInput : ChatAction {
        override fun reduce(state: ChatUiState): ChatUiState =
            state.copy(inputContent = "", replyingTo = null, isMoodTrayOpen = false)
    }
}
