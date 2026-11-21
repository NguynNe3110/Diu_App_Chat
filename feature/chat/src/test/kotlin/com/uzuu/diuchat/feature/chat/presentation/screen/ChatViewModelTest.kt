package com.uzuu.diuchat.feature.chat.presentation.screen

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState
import com.uzuu.diuchat.feature.chat.domain.usecase.GetMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendPhotoMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendVoiceMessageUseCase
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
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class ChatViewModelTest {
    private val mockGetMessagesUseCase: GetMessagesUseCase = mockk()
    private val mockSendMessageUseCase: SendMessageUseCase = mockk()
    private val mockSendPhotoMessageUseCase: SendPhotoMessageUseCase = mockk()
    private val mockSendVoiceMessageUseCase: SendVoiceMessageUseCase = mockk()

    private val sampleMessages =
        listOf(
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
        sut = ChatViewModel(
            getMessagesUseCase = mockGetMessagesUseCase,
            sendMessageUseCase = mockSendMessageUseCase,
            sendPhotoMessageUseCase = mockSendPhotoMessageUseCase,
            sendVoiceMessageUseCase = mockSendVoiceMessageUseCase,
        )
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

        val expectedMessage =
            ChatMessage(
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
                conversationId = any(),
                content = any(),
                mood = any(),
                replyToId = any(),
                replyQuote = any(),
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
    }

    @Test
    fun `onPhotoSelected and onClearPhoto update pendingPhotoUri`() {
        sut.onPhotoSelected("content://photos/autumn.jpg")
        sut.uiStateFlow.value.pendingPhotoUri shouldBeEqualTo "content://photos/autumn.jpg"

        sut.onClearPhoto()
        sut.uiStateFlow.value.pendingPhotoUri.shouldBeNull()
    }

    @Test
    fun `onSendMessage with photo sends via sendPhotoMessageUseCase`() = runTest {
        // given
        val photoUri = "content://photos/coffee.jpg"
        sut.onPhotoSelected(photoUri)
        sut.onInputChanged("Tách trà ấm buổi sáng")
        sut.onSelectMood(MoodType.BINHYEN)

        val expectedPhotoMessage =
            ChatMessage(
                id = "msg_photo_1",
                conversationId = "conv_linh_diu",
                senderId = "me",
                senderName = "Tôi",
                content = "Tách trà ấm buổi sáng",
                mood = MoodType.BINHYEN,
                status = MessageStatus.SENT,
                mediaType = MessageMediaType.PHOTO,
                mediaUri = photoUri,
                caption = "Tách trà ấm buổi sáng",
            )

        coEvery {
            mockSendPhotoMessageUseCase(
                conversationId = any(),
                photoUri = any(),
                caption = any(),
                mood = any(),
                replyToId = any(),
                replyQuote = any(),
            )
        } returns expectedPhotoMessage

        // when
        sut.onSendMessage()
        advanceUntilIdle()

        // then
        coVerify(exactly = 1) {
            mockSendPhotoMessageUseCase(
                conversationId = "conv_linh_diu",
                photoUri = photoUri,
                caption = "Tách trà ấm buổi sáng",
                mood = MoodType.BINHYEN,
                replyToId = null,
                replyQuote = null,
            )
        }
        sut.uiStateFlow.value.pendingPhotoUri.shouldBeNull()
        sut.uiStateFlow.value.inputContent shouldBeEqualTo ""
    }

    @Test
    fun `onStartVoiceRecording updates state to Recording`() {
        sut.onStartVoiceRecording()
        sut.uiStateFlow.value.voiceRecordingState shouldBeInstanceOf VoiceRecordingState.Recording::class
    }

    @Test
    fun `onStopVoiceRecording updates state to Preview`() {
        sut.onStopVoiceRecording(durationMs = 6000L)
        val state = sut.uiStateFlow.value.voiceRecordingState
        state shouldBeInstanceOf VoiceRecordingState.Preview::class
        (state as VoiceRecordingState.Preview).durationMs shouldBeEqualTo 6000L
    }

    @Test
    fun `onCancelVoiceRecording resets state to Idle`() {
        sut.onStartVoiceRecording()
        sut.onCancelVoiceRecording()
        sut.uiStateFlow.value.voiceRecordingState shouldBeEqualTo VoiceRecordingState.Idle
    }

    @Test
    fun `onTogglePreviewPlay toggles isPlaying in Preview`() {
        sut.onStopVoiceRecording()
        val before = (sut.uiStateFlow.value.voiceRecordingState as VoiceRecordingState.Preview).isPlaying
        before shouldBeEqualTo false

        sut.onTogglePreviewPlay()
        val after = (sut.uiStateFlow.value.voiceRecordingState as VoiceRecordingState.Preview).isPlaying
        after shouldBeEqualTo true
    }

    @Test
    fun `onSendVoiceMessage sends voice message via usecase and resets state`() = runTest {
        // given
        sut.onStopVoiceRecording(durationMs = 4500L)
        sut.onSelectMood(MoodType.THUONG)

        val expectedVoiceMessage =
            ChatMessage(
                id = "msg_voice_1",
                conversationId = "conv_linh_diu",
                senderId = "me",
                senderName = "Tôi",
                content = "🎤 [Tin nhắn thoại]",
                mood = MoodType.THUONG,
                status = MessageStatus.SENT,
                mediaType = MessageMediaType.VOICE,
                mediaUri = "file:///data/user/0/com.uzuu.diuchat/cache/audio_preview.m4a",
                audioDurationMs = 4500L,
            )

        coEvery {
            mockSendVoiceMessageUseCase(
                conversationId = any(),
                audioUri = any(),
                durationMs = any(),
                waveform = any(),
                mood = any(),
                replyToId = any(),
                replyQuote = any(),
            )
        } returns expectedVoiceMessage

        // when
        sut.onSendVoiceMessage()
        advanceUntilIdle()

        // then
        coVerify(exactly = 1) {
            mockSendVoiceMessageUseCase(
                conversationId = "conv_linh_diu",
                audioUri = any(),
                durationMs = 4500L,
                waveform = any(),
                mood = MoodType.THUONG,
                replyToId = null,
                replyQuote = null,
            )
        }
        sut.uiStateFlow.value.voiceRecordingState shouldBeEqualTo VoiceRecordingState.Idle
    }

    @Test
    fun `onToggleAudioPlayback updates playing audio message id`() {
        val message = sampleMessages.first()
        sut.onToggleAudioPlayback(message)
        sut.uiStateFlow.value.playingAudioMessageId shouldBeEqualTo message.id
        sut.uiStateFlow.value.audioPlaybackProgress shouldBeEqualTo 0.45f

        // toggle again stops playback
        sut.onToggleAudioPlayback(message)
        sut.uiStateFlow.value.playingAudioMessageId.shouldBeNull()
    }
}
