package com.uzuu.diuchat.feature.chat.presentation.screen

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
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
    fun `SetLoadingOlderMessages and SetHasMoreMessages update pagination flags`() {
        val loading = ChatAction.SetLoadingOlderMessages(true).reduce(initialState)
        loading.isLoadingOlderMessages shouldBeEqualTo true

        val done = ChatAction.SetHasMoreMessages(false).reduce(loading)
        done.hasMoreMessages shouldBeEqualTo false
    }

    @Test
    fun `SetConversation updates currentConversation and derived fields`() {
        val convo = Conversation(
            id = "conv_1",
            title = "Linh Dịu",
            mood = MoodType.THUONG,
            lastMessage = "Hey",
            lastMessageTimeEpochMs = 1000L,
            customNickname = "Bé Linh",
            pinnedMessageSnippet = "Tin nhắn quan trọng",
        )
        val action = ChatAction.SetConversation(convo)
        val newState = action.reduce(initialState)

        newState.currentConversation shouldBeEqualTo convo
        newState.conversationTitle shouldBeEqualTo "Bé Linh"
        newState.partnerMood shouldBeEqualTo MoodType.THUONG
        newState.pinnedSnippet shouldBeEqualTo "Tin nhắn quan trọng"
    }

    @Test
    fun `SetAvailableConversations updates availableConversations`() {
        val list = listOf(
            Conversation(id = "c1", title = "T1", mood = MoodType.VUI, lastMessage = "", lastMessageTimeEpochMs = 0L),
        )
        val action = ChatAction.SetAvailableConversations(list)
        val newState = action.reduce(initialState)

        newState.availableConversations shouldBeEqualTo list
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
    fun `SetActiveReactionDetail and SetActiveForwardMessage update state`() {
        val msg = ChatMessage(
            id = "m1",
            conversationId = "c1",
            senderId = "s1",
            senderName = "N",
            content = "C",
            mood = MoodType.BINHYEN,
            status = MessageStatus.SENT,
        )
        val s1 = ChatAction.SetActiveReactionDetail(msg).reduce(initialState)
        s1.activeReactionDetailMessage shouldBeEqualTo msg

        val s2 = ChatAction.SetActiveForwardMessage(msg).reduce(initialState)
        s2.activeForwardMessage shouldBeEqualTo msg
    }

    @Test
    fun `SetChatInfoOpen and SetNewGroupOpen update visibility flags`() {
        val s1 = ChatAction.SetChatInfoOpen(true).reduce(initialState)
        s1.isChatInfoOpen shouldBeEqualTo true

        val s2 = ChatAction.SetNewGroupOpen(true).reduce(initialState)
        s2.isNewGroupOpen shouldBeEqualTo true
    }

    @Test
    fun `SetSlowSendSheetOpen and UpdateSlowSendDraft update slow send state`() {
        val opened = ChatAction.SetSlowSendSheetOpen(true).reduce(initialState)
        opened.isSlowSendSheetOpen shouldBeEqualTo true

        val drafted = ChatAction.UpdateSlowSendDraft("Thương em nhiều", "Khi Minh thức dậy").reduce(opened)
        drafted.slowSendDraftContent shouldBeEqualTo "Thương em nhiều"
        drafted.slowSendScheduleLabel shouldBeEqualTo "Khi Minh thức dậy"
    }

    @Test
    fun `AppendLocalMessage ToggleRevealSealedMessage and CancelSealedMessage manage sealed letter bubble`() {
        val sealedMsg = ChatMessage(
            id = "sealed_1",
            conversationId = "conv_1",
            senderId = "me",
            senderName = "Mình",
            content = "Lời nhắn hẹn giờ",
            mood = MoodType.THUONG,
            status = MessageStatus.SENT,
            mediaType = MessageMediaType.SEALED_LETTER,
            scheduledForLabel = "7:00 sáng mai",
        )
        val withSealed = ChatAction.AppendLocalMessage(sealedMsg).reduce(initialState)
        withSealed.messages.size shouldBeEqualTo 1
        withSealed.messages.first().isSealedRevealed shouldBeEqualTo false

        val revealed = ChatAction.ToggleRevealSealedMessage("sealed_1").reduce(withSealed)
        revealed.messages.first().isSealedRevealed shouldBeEqualTo true

        val cancelled = ChatAction.CancelSealedMessage("sealed_1").reduce(revealed)
        cancelled.messages.isEmpty() shouldBeEqualTo true

        // SetMessages preserves isSealedRevealed across Room re-emissions
        val reemitted = ChatAction.SetMessages(listOf(sealedMsg.copy(isSealedRevealed = false))).reduce(revealed)
        reemitted.messages.first().isSealedRevealed shouldBeEqualTo true
    }

    @Test
    fun `ToggleNewGroupMember caps selection at 7 peers for 8 total members`() {
        var state = initialState
        (1..8).forEach { idx ->
            state = ChatAction.ToggleNewGroupMember("user_$idx").reduce(state)
        }
        state.newGroupSelectedMemberIds.size shouldBeEqualTo 7
        state.newGroupSelectedMemberIds.contains("user_8") shouldBeEqualTo false

        // Deselecting an existing member works when at capacity
        val afterRemove = ChatAction.ToggleNewGroupMember("user_3").reduce(state)
        afterRemove.newGroupSelectedMemberIds.size shouldBeEqualTo 6
    }

    @Test
    fun `SetGroupEditSheetOpen and UpdateNewGroupDraft update group states`() {
        val s1 = ChatAction.SetGroupEditSheetOpen(true).reduce(initialState)
        s1.isGroupEditSheetOpen shouldBeEqualTo true

        val s2 = ChatAction.UpdateNewGroupDraft("Nhà mình", MoodType.VUI).reduce(s1)
        s2.newGroupTitle shouldBeEqualTo "Nhà mình"
        s2.newGroupMood shouldBeEqualTo MoodType.VUI
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
