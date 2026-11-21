package com.uzuu.diuchat.feature.chat.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState
import com.uzuu.diuchat.feature.chat.domain.usecase.GetMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendPhotoMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendVoiceMessageUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendPhotoMessageUseCase: SendPhotoMessageUseCase,
    private val sendVoiceMessageUseCase: SendVoiceMessageUseCase,
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

    fun onPhotoSelected(uri: String) {
        sendAction(ChatAction.SetPendingPhoto(uri))
    }

    fun onClearPhoto() {
        sendAction(ChatAction.SetPendingPhoto(null))
    }

    fun onStartVoiceRecording() {
        val defaultAmplitudes = listOf(0.2f, 0.5f, 0.8f, 0.4f, 0.9f, 0.6f, 0.7f, 0.4f, 0.8f, 0.5f, 0.3f, 0.6f)
        sendAction(
            ChatAction.SetVoiceRecordingState(
                VoiceRecordingState.Recording(
                    elapsedMs = 3000L,
                    amplitudes = defaultAmplitudes,
                ),
            ),
        )
    }

    fun onCancelVoiceRecording() {
        sendAction(ChatAction.SetVoiceRecordingState(VoiceRecordingState.Idle))
    }

    fun onStopVoiceRecording(
        durationMs: Long = 5000L,
        waveform: List<Float> = listOf(0.3f, 0.6f, 0.8f, 0.5f, 0.9f, 0.4f, 0.7f, 0.6f, 0.8f, 0.4f),
        audioUri: String = "file:///data/user/0/com.uzuu.diuchat/cache/audio_preview.m4a",
    ) {
        sendAction(
            ChatAction.SetVoiceRecordingState(
                VoiceRecordingState.Preview(
                    audioUri = audioUri,
                    durationMs = durationMs,
                    waveform = waveform,
                    isPlaying = false,
                    currentPositionMs = 0L,
                ),
            ),
        )
    }

    fun onTogglePreviewPlay() {
        val current = uiStateFlow.value.voiceRecordingState
        if (current is VoiceRecordingState.Preview) {
            sendAction(
                ChatAction.SetVoiceRecordingState(
                    current.copy(isPlaying = !current.isPlaying),
                ),
            )
        }
    }

    fun onSendVoiceMessage() {
        val currentState = uiStateFlow.value
        val recordingState = currentState.voiceRecordingState
        val (audioUri, durationMs, waveform) = when (recordingState) {
            is VoiceRecordingState.Preview -> {
                Triple(recordingState.audioUri, recordingState.durationMs, recordingState.waveform)
            }
            is VoiceRecordingState.Recording -> {
                Triple("file:///data/user/0/com.uzuu.diuchat/cache/audio_recorded.m4a", recordingState.elapsedMs, recordingState.amplitudes)
            }
            else -> return
        }

        sendAction(ChatAction.SetSending(true))
        viewModelScope.launch {
            try {
                sendVoiceMessageUseCase(
                    conversationId = currentState.conversationId,
                    audioUri = audioUri,
                    durationMs = durationMs,
                    waveform = waveform,
                    mood = currentState.selectedMood,
                    replyToId = currentState.replyingTo?.id,
                    replyQuote = currentState.replyingTo?.content,
                )
                sendAction(ChatAction.SetVoiceRecordingState(VoiceRecordingState.Idle))
                sendAction(ChatAction.ClearInput)
            } finally {
                sendAction(ChatAction.SetSending(false))
            }
        }
    }

    fun onToggleAudioPlayback(message: ChatMessage) {
        val currentState = uiStateFlow.value
        if (currentState.playingAudioMessageId == message.id) {
            sendAction(ChatAction.SetAudioPlayback(null, 0f))
        } else {
            sendAction(ChatAction.SetAudioPlayback(message.id, 0.45f))
        }
    }

    fun onSendMessage() {
        val currentState = uiStateFlow.value
        val text = currentState.inputContent.trim()
        val photoUri = currentState.pendingPhotoUri

        if (text.isEmpty() && photoUri == null) return

        sendAction(ChatAction.SetSending(true))
        viewModelScope.launch {
            try {
                if (photoUri != null) {
                    sendPhotoMessageUseCase(
                        conversationId = currentState.conversationId,
                        photoUri = photoUri,
                        caption = text.takeIf { it.isNotEmpty() },
                        mood = currentState.selectedMood,
                        replyToId = currentState.replyingTo?.id,
                        replyQuote = currentState.replyingTo?.content,
                    )
                } else {
                    sendMessageUseCase(
                        conversationId = currentState.conversationId,
                        content = text,
                        mood = currentState.selectedMood,
                        replyToId = currentState.replyingTo?.id,
                        replyQuote = currentState.replyingTo?.content,
                    )
                }
                sendAction(ChatAction.ClearInput)
            } finally {
                sendAction(ChatAction.SetSending(false))
            }
        }
    }
}
