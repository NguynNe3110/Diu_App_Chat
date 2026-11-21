package com.uzuu.diuchat.feature.chat.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.usecase.GetMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
) : BaseViewModel<ChatUiState, ChatAction>(ChatUiState()) {
    init {
        loadMessages()
    }

    fun loadMessages(conversationId: String = uiStateFlow.value.conversationId) {
        viewModelScope.launch {
            getMessagesUseCase(conversationId)
                .catch { /* handle error */ }
                .collect { messages ->
                    sendAction(ChatAction.SetMessages(messages))
                }
        }
    }

    fun onInputChanged(content: String) {
        sendAction(ChatAction.UpdateInput(content))
    }

    fun onSelectMood(mood: MoodType) {
        sendAction(ChatAction.SelectMood(mood))
    }

    fun onToggleMoodTray(isOpen: Boolean? = null) {
        val newState = isOpen ?: !uiStateFlow.value.isMoodTrayOpen
        sendAction(ChatAction.ToggleMoodTray(newState))
    }

    fun onReplyToMessage(message: ChatMessage) {
        sendAction(ChatAction.SetReplyingTo(message))
    }

    fun onCancelReply() {
        sendAction(ChatAction.SetReplyingTo(null))
    }

    fun onSendMessage() {
        val currentState = uiStateFlow.value
        val text = currentState.inputContent.trim()
        if (text.isEmpty()) return

        sendAction(ChatAction.SetSending(true))
        viewModelScope.launch {
            try {
                sendMessageUseCase(
                    conversationId = currentState.conversationId,
                    content = text,
                    mood = currentState.selectedMood,
                    replyToId = currentState.replyingTo?.id,
                    replyQuote = currentState.replyingTo?.content,
                )
                sendAction(ChatAction.ClearInput)
            } finally {
                sendAction(ChatAction.SetSending(false))
            }
        }
    }
}
