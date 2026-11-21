package com.uzuu.diuchat.feature.chat.presentation.screen

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.usecase.GetMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendMessageUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class ChatViewModelTest {

    private val mockGetMessagesUseCase: GetMessagesUseCase = mockk()
    private val mockSendMessageUseCase: SendMessageUseCase = mockk()

    private val sampleMessages = listOf(
        ChatMessage(
            id = "msg_1",
            conversationId = "conv_linh_diu",
            senderId = "user_linh",
            senderName = "Linh Dịu",
            content = "Hôm nay trời thu Hà Nội rất dịu mát.",
            mood = MoodType.BINHYEN,
            status = MessageStatus.SENT,
        ),
    )

    private lateinit var sut: ChatViewModel

    @BeforeEach
    fun setUp() {
        every { mockGetMessagesUseCase(any()) } returns flowOf(sampleMessages)
        sut = ChatViewModel(mockGetMessagesUseCase, mockSendMessageUseCase)
    }

    @Test
    fun `initial state loads messages from conversation`() = runTest {
        advanceUntilIdle()
        sut.uiStateFlow.value.messages shouldBeEqualTo sampleMessages
        sut.uiStateFlow.value.conversationTitle shouldBeEqualTo "Linh Dịu"
    }

    @Test
    fun `onInputChanged updates inputContent`() {
        sut.onInputChanged("Em vừa đi dạo về")
        sut.uiStateFlow.value.inputContent shouldBeEqualTo "Em vừa đi dạo về"
    }

    @Test
    fun `onSelectMood updates selectedMood`() {
        sut.onSelectMood(MoodType.VUI)
        sut.uiStateFlow.value.selectedMood shouldBeEqualTo MoodType.VUI
    }

    @Test
    fun `onToggleMoodTray toggles tray state`() {
        sut.onToggleMoodTray(true)
        sut.uiStateFlow.value.isMoodTrayOpen shouldBeEqualTo true

        sut.onToggleMoodTray(false)
        sut.uiStateFlow.value.isMoodTrayOpen shouldBeEqualTo false
    }

    @Test
    fun `onReplyToMessage and onCancelReply update replyingTo`() {
        val targetMessage = sampleMessages.first()
        sut.onReplyToMessage(targetMessage)
        sut.uiStateFlow.value.replyingTo shouldBeEqualTo targetMessage

        sut.onCancelReply()
        sut.uiStateFlow.value.replyingTo.shouldBeNull()
    }

    @Test
    fun `onSendMessage sends message via usecase and clears input`() = runTest {
        // given
        sut.onInputChanged("Hẹn gặp em nhé!")
        sut.onSelectMood(MoodType.THUONG)

        val expectedMessage = ChatMessage(
            id = "msg_2",
            conversationId = "conv_linh_diu",
            senderId = "me",
            senderName = "Tôi",
            content = "Hẹn gặp em nhé!",
            mood = MoodType.THUONG,
            status = MessageStatus.SENT,
        )

        coEvery {
            mockSendMessageUseCase(
                conversationId = "conv_linh_diu",
                content = "Hẹn gặp em nhé!",
                mood = MoodType.THUONG,
                replyToId = null,
                replyQuote = null,
            )
        } returns expectedMessage

        // when
        sut.onSendMessage()
        advanceUntilIdle()

        // then
        coVerify(exactly = 1) {
            mockSendMessageUseCase(
                conversationId = "conv_linh_diu",
                content = "Hẹn gặp em nhé!",
                mood = MoodType.THUONG,
                replyToId = null,
                replyQuote = null,
            )
        }
        sut.uiStateFlow.value.inputContent shouldBeEqualTo ""
        sut.uiStateFlow.value.isSending shouldBeEqualTo false
    }
}
