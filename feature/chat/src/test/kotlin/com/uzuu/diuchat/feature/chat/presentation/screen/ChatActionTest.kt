package com.uzuu.diuchat.feature.chat.presentation.screen

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

class ChatActionTest {

    private val initialState = ChatUiState()

    @Test
    fun `SetMessages updates messages in state`() {
        val messages = listOf(
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
        val message = ChatMessage(
            id = "1",
            conversationId = "conv_1",
            senderId = "user_1",
            senderName = "Linh",
            content = "Tin nhắn gốc",
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
    fun `ClearInput resets inputContent, replyingTo, and closes tray`() {
        val message = ChatMessage(
            id = "1",
            conversationId = "conv_1",
            senderId = "user_1",
            senderName = "Linh",
            content = "Tin nhắn gốc",
            mood = MoodType.THUONG,
            status = MessageStatus.SENT,
        )
        val modifiedState = initialState.copy(
            inputContent = "Nội dung cũ",
            replyingTo = message,
            isMoodTrayOpen = true,
        )

        val newState = ChatAction.ClearInput.reduce(modifiedState)

        newState.inputContent shouldBeEqualTo ""
        newState.replyingTo.shouldBeNull()
        newState.isMoodTrayOpen shouldBeEqualTo false
    }
}
