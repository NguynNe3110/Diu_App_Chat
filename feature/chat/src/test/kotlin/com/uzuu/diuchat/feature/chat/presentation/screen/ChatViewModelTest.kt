package com.uzuu.diuchat.feature.chat.presentation.screen

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.GroupError
import com.uzuu.diuchat.feature.chat.domain.model.GroupRole
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState
import com.uzuu.diuchat.feature.chat.domain.usecase.CreateGroupUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ForwardMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.GetMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.LeaveGroupUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ManageGroupMemberUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendPhotoMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendVoiceMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.TogglePinMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ToggleReactionUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.UpdateGroupInfoUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
    private val mockCreateGroupUseCase: CreateGroupUseCase = mockk()
    private val mockForwardMessageUseCase: ForwardMessageUseCase = mockk()
    private val mockGetMessagesUseCase: GetMessagesUseCase = mockk()
    private val mockLeaveGroupUseCase: LeaveGroupUseCase = mockk()
    private val mockManageGroupMemberUseCase: ManageGroupMemberUseCase = mockk()
    private val mockSendMessageUseCase: SendMessageUseCase = mockk()
    private val mockSendPhotoMessageUseCase: SendPhotoMessageUseCase = mockk()
    private val mockSendVoiceMessageUseCase: SendVoiceMessageUseCase = mockk()
    private val mockTogglePinMessageUseCase: TogglePinMessageUseCase = mockk()
    private val mockToggleReactionUseCase: ToggleReactionUseCase = mockk()
    private val mockUpdateGroupInfoUseCase: UpdateGroupInfoUseCase = mockk()

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
            createGroupUseCase = mockCreateGroupUseCase,
            forwardMessageUseCase = mockForwardMessageUseCase,
            getMessagesUseCase = mockGetMessagesUseCase,
            leaveGroupUseCase = mockLeaveGroupUseCase,
            manageGroupMemberUseCase = mockManageGroupMemberUseCase,
            sendMessageUseCase = mockSendMessageUseCase,
            sendPhotoMessageUseCase = mockSendPhotoMessageUseCase,
            sendVoiceMessageUseCase = mockSendVoiceMessageUseCase,
            togglePinMessageUseCase = mockTogglePinMessageUseCase,
            toggleReactionUseCase = mockToggleReactionUseCase,
            updateGroupInfoUseCase = mockUpdateGroupInfoUseCase,
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

    // P7 Reactions, Pin, Forward & Groups tests
    @Test
    fun `onToggleReaction calls toggleReactionUseCase`() = runTest {
        val msg = sampleMessages.first()
        coEvery { mockToggleReactionUseCase(msg.id, MoodType.THUONG) } just runs

        sut.onToggleReaction(msg, MoodType.THUONG)
        advanceUntilIdle()

        coVerify(exactly = 1) { mockToggleReactionUseCase(msg.id, MoodType.THUONG) }
    }

    @Test
    fun `onTogglePinMessage calls togglePinMessageUseCase`() = runTest {
        val msg = sampleMessages.first()
        coEvery { mockTogglePinMessageUseCase("conv_linh_diu", msg.id) } just runs

        sut.onTogglePinMessage(msg)
        advanceUntilIdle()

        coVerify(exactly = 1) { mockTogglePinMessageUseCase("conv_linh_diu", msg.id) }
    }

    @Test
    fun `onOpenForwardDialog and onConfirmForward delegates to forwardMessageUseCase`() = runTest {
        val msg = sampleMessages.first()
        sut.onOpenForwardDialog(msg)
        sut.uiStateFlow.value.activeForwardMessage shouldBeEqualTo msg

        coEvery { mockForwardMessageUseCase(msg, listOf("conv_target_1")) } returns listOf(msg)

        sut.onConfirmForward(listOf("conv_target_1"))
        advanceUntilIdle()

        coVerify(exactly = 1) { mockForwardMessageUseCase(msg, listOf("conv_target_1")) }
        sut.uiStateFlow.value.activeForwardMessage.shouldBeNull()
    }

    @Test
    fun `onCreateGroup creates group and closes dialog`() = runTest {
        val createdConvo = Conversation(
            id = "group_new",
            title = "Gia đình",
            mood = MoodType.THUONG,
            lastMessage = "Nhóm vừa tạo",
            lastMessageTimeEpochMs = 1000L,
            isGroup = true,
        )
        coEvery { mockCreateGroupUseCase("Gia đình", MoodType.THUONG, listOf("u1", "u2")) } returns createdConvo

        var resultId: String? = null
        sut.onCreateGroup("Gia đình", MoodType.THUONG, listOf("u1", "u2")) { id ->
            resultId = id
        }
        advanceUntilIdle()

        resultId shouldBeEqualTo "group_new"
        sut.uiStateFlow.value.isNewGroupOpen shouldBeEqualTo false
    }

    @Test
    fun `onUpdateNickname and onToggleMute update conversation info`() = runTest {
        val convo = Conversation(
            id = "conv_1",
            title = "Title",
            mood = MoodType.THUONG,
            lastMessage = "",
            lastMessageTimeEpochMs = 0L,
        )
        sut.onSelectConversation(convo)

        coEvery { mockUpdateGroupInfoUseCase(convo.id, nickname = "Em Dịu") } just runs
        coEvery { mockUpdateGroupInfoUseCase(convo.id, isMuted = true) } just runs

        sut.onUpdateNickname("Em Dịu")
        advanceUntilIdle()
        coVerify(exactly = 1) { mockUpdateGroupInfoUseCase(convo.id, nickname = "Em Dịu") }

        sut.onToggleMute(true)
        advanceUntilIdle()
        coVerify(exactly = 1) { mockUpdateGroupInfoUseCase(convo.id, isMuted = true) }
    }

    @Test
    fun `onManageMemberRole and onRemoveMember delegate to manageGroupMemberUseCase`() = runTest {
        val convo = Conversation(id = "group_1", title = "G", mood = MoodType.THUONG, lastMessage = "", lastMessageTimeEpochMs = 0L)
        sut.onSelectConversation(convo)

        coEvery { mockManageGroupMemberUseCase("group_1", "user_2", newRole = GroupRole.ADMIN) } just runs
        coEvery { mockManageGroupMemberUseCase("group_1", "user_2", isRemove = true) } just runs

        sut.onManageMemberRole("user_2", GroupRole.ADMIN)
        advanceUntilIdle()
        coVerify(exactly = 1) { mockManageGroupMemberUseCase("group_1", "user_2", newRole = GroupRole.ADMIN) }

        sut.onRemoveMember("user_2")
        advanceUntilIdle()
        coVerify(exactly = 1) { mockManageGroupMemberUseCase("group_1", "user_2", isRemove = true) }
    }

    @Test
    fun `onLeaveGroup success calls leaveGroupUseCase and onSuccess`() = runTest {
        val convo = Conversation(id = "group_1", title = "G", mood = MoodType.THUONG, lastMessage = "", lastMessageTimeEpochMs = 0L)
        sut.onSelectConversation(convo)

        coEvery { mockLeaveGroupUseCase("group_1") } just runs

        var successCalled = false
        sut.onLeaveGroup(onSuccess = { successCalled = true })
        advanceUntilIdle()

        successCalled shouldBeEqualTo true
        coVerify(exactly = 1) { mockLeaveGroupUseCase("group_1") }
    }

    @Test
    fun `onLeaveGroup failure with DEC-07 calls onError callback`() = runTest {
        val convo = Conversation(id = "group_1", title = "G", mood = MoodType.THUONG, lastMessage = "", lastMessageTimeEpochMs = 0L)
        sut.onSelectConversation(convo)

        coEvery { mockLeaveGroupUseCase("group_1") } throws GroupError.LastAdminCannotLeave

        var errorMsg: String? = null
        sut.onLeaveGroup(onSuccess = {}, onError = { msg -> errorMsg = msg })
        advanceUntilIdle()

        errorMsg shouldBeEqualTo GroupError.LastAdminCannotLeave.message
    }
}
