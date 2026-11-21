package com.uzuu.diuchat.feature.chat.presentation.screen

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

class ChatActionTest {
    private val initialState = ChatUiState()

    @Test
    fun `SetMessages updates messages in state`() {
        val messages =
            listOf(
                ChatMessage(
                    id = "1",
                    conversationId = "conv_1",
                    senderId = "user_1",
                    senderName = "Linh",
                    content = "Chào buổi sáng",
                    mood = MoodType.BINHYEN,
                    status = MessageStatus.SENT,
                ),
            )
        val action = ChatAction.SetMessages(messages)
        val newState = action.reduce(initialState)

        newState.messages shouldBeEqualTo messages
    }

    @Test
    fun `UpdateInput updates inputContent`() {
        val action = ChatAction.UpdateInput("Em đang uống trà")
        val newState = action.reduce(initialState)

        newState.inputContent shouldBeEqualTo "Em đang uống trà"
    }

    @Test
    fun `SelectMood updates selectedMood`() {
        val action = ChatAction.SelectMood(MoodType.VUI)
        val newState = action.reduce(initialState)

        newState.selectedMood shouldBeEqualTo MoodType.VUI
    }

    @Test
    fun `ToggleMoodTray updates isMoodTrayOpen`() {
        val action = ChatAction.ToggleMoodTray(true)
        val newState = action.reduce(initialState)

        newState.isMoodTrayOpen shouldBeEqualTo true
    }

    @Test
    fun `SetReplyingTo updates replyingTo`() {
        val message =
            ChatMessage(
                id = "1",
                conversationId = "conv_1",
                senderId = "user_1",
                senderName = "Linh",
                content = "Chào em",
                mood = MoodType.THUONG,
                status = MessageStatus.SENT,
            )
        val action = ChatAction.SetReplyingTo(message)
        val newState = action.reduce(initialState)

        newState.replyingTo shouldBeEqualTo message
    }

    @Test
    fun `SetSending updates isSending`() {
        val action = ChatAction.SetSending(true)
        val newState = action.reduce(initialState)

        newState.isSending shouldBeEqualTo true
    }

    @Test
    fun `SetVoiceRecordingState updates voiceRecordingState`() {
        val recordingState = VoiceRecordingState.Recording(
            elapsedMs = 4200L,
            amplitudes = listOf(0.4f, 0.7f),
        )
        val action = ChatAction.SetVoiceRecordingState(recordingState)
        val newState = action.reduce(initialState)

        newState.voiceRecordingState shouldBeEqualTo recordingState
    }

    @Test
    fun `SetPendingPhoto updates pendingPhotoUri`() {
        val uri = "content://media/photo/12"
        val action = ChatAction.SetPendingPhoto(uri)
        val newState = action.reduce(initialState)

        newState.pendingPhotoUri shouldBeEqualTo uri
    }

    @Test
    fun `SetAudioPlayback updates playing audio and progress`() {
        val action = ChatAction.SetAudioPlayback("msg_audio_1", 0.75f)
        val newState = action.reduce(initialState)

        newState.playingAudioMessageId shouldBeEqualTo "msg_audio_1"
        newState.audioPlaybackProgress shouldBeEqualTo 0.75f
    }

    @Test
    fun `ClearInput resets inputContent, replyingTo, photo, and voiceRecordingState`() {
        val modifiedState =
            initialState.copy(
                inputContent = "Hello",
                replyingTo =
                ChatMessage(
                    id = "1",
                    conversationId = "c1",
                    senderId = "s1",
                    senderName = "Name",
                    content = "Hey",
                    mood = MoodType.THUONG,
                    status = MessageStatus.SENT,
                ),
                isMoodTrayOpen = true,
                pendingPhotoUri = "content://photo",
                voiceRecordingState = VoiceRecordingState.Recording(3000L),
            )
        val action = ChatAction.ClearInput
        val newState = action.reduce(modifiedState)

        newState.inputContent shouldBeEqualTo ""
        newState.replyingTo.shouldBeNull()
        newState.isMoodTrayOpen shouldBeEqualTo false
        newState.pendingPhotoUri.shouldBeNull()
        newState.voiceRecordingState shouldBeEqualTo VoiceRecordingState.Idle
    }
}
