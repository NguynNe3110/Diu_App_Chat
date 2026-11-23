package com.uzuu.diuchat.feature.chat.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.chat.domain.media.VoiceAudioPlayer
import com.uzuu.diuchat.feature.chat.domain.media.VoiceAudioRecorder
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.GroupError
import com.uzuu.diuchat.feature.chat.domain.model.GroupRole
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState
import com.uzuu.diuchat.feature.chat.domain.usecase.CreateGroupUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.DeleteConversationUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.DeleteMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ForwardMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.GetConversationUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.GetConversationsUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.GetMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.LeaveGroupUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.LoadOlderMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ManageGroupMemberUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ObserveTypingStatusUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.RevealSealedMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendPhotoMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendSealedMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendTypingStatusUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendVoiceMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.TogglePinMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ToggleReactionUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.UpdateGroupInfoUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private const val TYPING_IDLE_TIMEOUT_MS = 2_500L
private const val TYPING_REMOTE_CLEAR_MS = 3_500L
private const val DEFAULT_SLOW_SEND_DELAY_MS = 12 * 3600 * 1000L

@Suppress("TooManyFunctions", "LongParameterList")
class ChatViewModel(
    private val createGroupUseCase: CreateGroupUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val forwardMessageUseCase: ForwardMessageUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val loadOlderMessagesUseCase: LoadOlderMessagesUseCase,
    private val manageGroupMemberUseCase: ManageGroupMemberUseCase,
    private val observeTypingStatusUseCase: ObserveTypingStatusUseCase,
    private val revealSealedMessageUseCase: RevealSealedMessageUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendPhotoMessageUseCase: SendPhotoMessageUseCase,
    private val sendSealedMessageUseCase: SendSealedMessageUseCase,
    private val sendTypingStatusUseCase: SendTypingStatusUseCase,
    private val sendVoiceMessageUseCase: SendVoiceMessageUseCase,
    private val togglePinMessageUseCase: TogglePinMessageUseCase,
    private val toggleReactionUseCase: ToggleReactionUseCase,
    private val updateGroupInfoUseCase: UpdateGroupInfoUseCase,
    private val voiceAudioPlayer: VoiceAudioPlayer,
    private val voiceAudioRecorder: VoiceAudioRecorder,
) : BaseViewModel<ChatUiState, ChatAction>(ChatUiState()) {
    private var messagesJob: Job? = null
    private var conversationJob: Job? = null
    private var typingObserveJob: Job? = null
    private var typingClearJob: Job? = null
    private var localTypingStopJob: Job? = null
    private var voiceRecordingTickerJob: Job? = null

    init {
        observeAvailableConversations()
        if (uiStateFlow.value.conversationId.isNotBlank()) {
            loadMessages()
        }
    }

    private fun observeAvailableConversations() {
        viewModelScope.launch {
            getConversationsUseCase()
                .catch { /* handle error */ }
                .collect { conversations ->
                    sendAction(ChatAction.SetAvailableConversations(conversations))
                }
        }
    }

    fun loadMessages(conversationId: String = uiStateFlow.value.conversationId) {
        if (conversationId.isBlank()) return
        if (uiStateFlow.value.conversationId != conversationId) {
            sendAction(ChatAction.SetConversationId(conversationId))
        }
        sendAction(ChatAction.SetLoadingMessages(true))
        sendAction(ChatAction.SetLoadingOlderMessages(false))
        sendAction(ChatAction.SetHasMoreMessages(true))
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            getMessagesUseCase(conversationId)
                .catch { /* handle error */ }
                .collect { messages ->
                    sendAction(ChatAction.SetMessages(messages))
                }
        }
        conversationJob?.cancel()
        conversationJob = viewModelScope.launch {
            getConversationUseCase(conversationId)
                .catch { /* handle error */ }
                .collect { conversation ->
                    if (conversation != null) {
                        sendAction(ChatAction.SetConversation(conversation))
                    }
                }
        }
        typingObserveJob?.cancel()
        typingObserveJob = viewModelScope.launch {
            observeTypingStatusUseCase(conversationId)
                .catch { /* ignore typing errors */ }
                .collect { isTyping ->
                    sendAction(ChatAction.SetPartnerTyping(isTyping))
                    typingClearJob?.cancel()
                    if (isTyping) {
                        typingClearJob = viewModelScope.launch {
                            delay(TYPING_REMOTE_CLEAR_MS)
                            sendAction(ChatAction.SetPartnerTyping(false))
                        }
                    }
                }
        }
    }

    fun onLoadOlderMessages() {
        val currentState = uiStateFlow.value
        if (
            currentState.conversationId.isBlank() ||
            currentState.isLoadingMessages ||
            currentState.isLoadingOlderMessages ||
            !currentState.hasMoreMessages
        ) {
            return
        }
        val oldestEpochMs = currentState.messages.firstOrNull()?.createdAtEpochMs ?: return
        sendAction(ChatAction.SetLoadingOlderMessages(true))
        viewModelScope.launch {
            try {
                val hasMore = loadOlderMessagesUseCase(
                    conversationId = currentState.conversationId,
                    beforeEpochMs = oldestEpochMs,
                )
                sendAction(ChatAction.SetHasMoreMessages(hasMore))
            } finally {
                sendAction(ChatAction.SetLoadingOlderMessages(false))
            }
        }
    }

    fun onInputChanged(content: String) {
        sendAction(ChatAction.UpdateInput(content))
        val convoId = uiStateFlow.value.conversationId
        if (convoId.isBlank()) return
        localTypingStopJob?.cancel()
        if (content.isNotBlank()) {
            viewModelScope.launch {
                runCatching { sendTypingStatusUseCase(convoId, true) }
            }
            localTypingStopJob = viewModelScope.launch {
                delay(TYPING_IDLE_TIMEOUT_MS)
                runCatching { sendTypingStatusUseCase(convoId, false) }
            }
        } else {
            viewModelScope.launch {
                runCatching { sendTypingStatusUseCase(convoId, false) }
            }
        }
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

    fun onReplyToMessageById(messageId: String) {
        val target = uiStateFlow.value.messages.find { it.id == messageId } ?: return
        sendAction(ChatAction.SetReplyingTo(target))
    }

    fun onDeleteMessage(messageId: String) {
        viewModelScope.launch {
            deleteMessageUseCase(messageId)
        }
    }

    fun onDeleteConversation(onSuccess: () -> Unit) {
        val convoId = uiStateFlow.value.conversationId
        if (convoId.isBlank()) return
        viewModelScope.launch {
            deleteConversationUseCase(convoId)
            sendAction(ChatAction.SetChatInfoOpen(false))
            onSuccess()
        }
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

    fun onSelectConversation(conversation: Conversation) {
        sendAction(ChatAction.SetConversation(conversation))
    }

    fun onStartVoiceRecording() {
        voiceAudioPlayer.stop()
        sendAction(ChatAction.SetAudioPlayback(null, 0f))
        val started = voiceAudioRecorder.startRecording()
        val initialAmplitudes = listOf(0.25f, 0.45f, 0.65f, 0.35f, 0.75f, 0.5f, 0.6f, 0.4f, 0.7f, 0.45f, 0.3f, 0.55f)
        sendAction(
            ChatAction.SetVoiceRecordingState(
                VoiceRecordingState.Recording(
                    elapsedMs = 0L,
                    amplitudes = initialAmplitudes,
                ),
            ),
        )
        if (!started) return
        val startedAt = System.currentTimeMillis()
        voiceRecordingTickerJob?.cancel()
        voiceRecordingTickerJob = viewModelScope.launch {
            val rollingBars = ArrayDeque<Float>(22).apply {
                repeat(22) { addLast(0.22f) }
            }
            while (true) {
                delay(120L)
                val amp = voiceAudioRecorder.readNormalizedAmplitude()
                if (rollingBars.size >= 22) rollingBars.removeFirst()
                rollingBars.addLast(amp)
                sendAction(
                    ChatAction.SetVoiceRecordingState(
                        VoiceRecordingState.Recording(
                            elapsedMs = System.currentTimeMillis() - startedAt,
                            amplitudes = rollingBars.toList(),
                        ),
                    ),
                )
            }
        }
    }

    fun onCancelVoiceRecording() {
        voiceRecordingTickerJob?.cancel()
        voiceRecordingTickerJob = null
        voiceAudioRecorder.cancelRecording()
        sendAction(ChatAction.SetVoiceRecordingState(VoiceRecordingState.Idle))
    }

    fun onStopVoiceRecording(
        durationMs: Long = 5000L,
        waveform: List<Float> = listOf(0.3f, 0.6f, 0.8f, 0.5f, 0.9f, 0.4f, 0.7f, 0.6f, 0.8f, 0.4f),
        audioUri: String = "file:///data/user/0/com.uzuu.diuchat/cache/audio_preview.m4a",
    ) {
        voiceRecordingTickerJob?.cancel()
        voiceRecordingTickerJob = null
        val clip = voiceAudioRecorder.stopRecording()
        sendAction(
            ChatAction.SetVoiceRecordingState(
                VoiceRecordingState.Preview(
                    audioUri = clip?.audioUri ?: audioUri,
                    durationMs = clip?.durationMs ?: durationMs,
                    waveform = clip?.waveform ?: waveform,
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
        voiceRecordingTickerJob?.cancel()
        voiceRecordingTickerJob = null

        val (audioUri, durationMs, waveform) = when (recordingState) {
            is VoiceRecordingState.Preview -> Triple(
                recordingState.audioUri,
                recordingState.durationMs,
                recordingState.waveform,
            )

            is VoiceRecordingState.Recording -> {
                val clip = voiceAudioRecorder.stopRecording()
                if (clip != null) {
                    Triple(clip.audioUri, clip.durationMs, clip.waveform)
                } else {
                    Triple(
                        "file:///data/user/0/com.uzuu.diuchat/cache/audio_recorded.m4a",
                        recordingState.elapsedMs.coerceAtLeast(1000L),
                        recordingState.amplitudes,
                    )
                }
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
            voiceAudioPlayer.stop()
            sendAction(ChatAction.SetAudioPlayback(null, 0f))
        } else {
            val uri = message.mediaUri.orEmpty()
            sendAction(ChatAction.SetAudioPlayback(message.id, 0.05f))
            if (uri.isNotBlank()) {
                voiceAudioPlayer.play(
                    messageId = message.id,
                    audioUri = uri,
                    onProgress = { progress ->
                        sendAction(ChatAction.SetAudioPlayback(message.id, progress))
                    },
                    onCompleted = {
                        sendAction(ChatAction.SetAudioPlayback(null, 0f))
                    },
                    onError = {
                        sendAction(ChatAction.SetAudioPlayback(null, 0f))
                    },
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecordingTickerJob?.cancel()
        voiceAudioRecorder.cancelRecording()
        voiceAudioPlayer.release()
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

    // P7 Reactions, Pin, Forward & Groups
    fun onToggleReaction(message: ChatMessage, mood: MoodType) {
        viewModelScope.launch {
            toggleReactionUseCase(message.id, mood)
        }
    }

    fun onTogglePinMessage(message: ChatMessage) {
        val currentState = uiStateFlow.value
        val currentConvoId = currentState.conversationId
        val willPin = currentState.currentConversation?.pinnedMessageId != message.id
        viewModelScope.launch {
            togglePinMessageUseCase(currentConvoId, message.id)
            val updatedSnippet = if (willPin) message.content.take(60) else null
            sendAction(
                ChatAction.SetConversation(
                    uiStateFlow.value.currentConversation?.copy(
                        pinnedMessageId = if (willPin) message.id else null,
                        pinnedMessageSnippet = updatedSnippet,
                    ),
                ),
            )
        }
    }

    fun onUnpinCurrentMessage() {
        val convo = uiStateFlow.value.currentConversation ?: return
        val pinnedId = convo.pinnedMessageId ?: return
        viewModelScope.launch {
            togglePinMessageUseCase(convo.id, pinnedId)
            sendAction(
                ChatAction.SetConversation(
                    convo.copy(pinnedMessageId = null, pinnedMessageSnippet = null),
                ),
            )
        }
    }

    fun onOpenReactionDetail(message: ChatMessage) {
        sendAction(ChatAction.SetActiveReactionDetail(message))
    }

    fun onCloseReactionDetail() {
        sendAction(ChatAction.SetActiveReactionDetail(null))
    }

    fun onOpenForwardDialog(message: ChatMessage) {
        sendAction(ChatAction.SetActiveForwardMessage(message))
    }

    fun onCloseForwardDialog() {
        sendAction(ChatAction.SetActiveForwardMessage(null))
    }

    fun onConfirmForward(recipientIds: List<String>) {
        val message = uiStateFlow.value.activeForwardMessage ?: return
        viewModelScope.launch {
            forwardMessageUseCase(message, recipientIds)
            sendAction(ChatAction.SetActiveForwardMessage(null))
        }
    }

    fun onOpenChatInfo(isOpen: Boolean) {
        sendAction(ChatAction.SetChatInfoOpen(isOpen))
    }

    fun onOpenNewGroup(isOpen: Boolean) {
        sendAction(ChatAction.SetNewGroupOpen(isOpen))
    }

    fun onCreateGroup(
        title: String,
        mood: MoodType,
        memberIds: List<String>,
        onSuccess: (String) -> Unit,
    ) {
        viewModelScope.launch {
            val group = createGroupUseCase(title, mood, memberIds)
            sendAction(ChatAction.SetNewGroupOpen(false))
            onSuccess(group.id)
        }
    }

    fun onUpdateNickname(nickname: String) {
        val convo = uiStateFlow.value.currentConversation ?: return
        viewModelScope.launch {
            updateGroupInfoUseCase(convo.id, nickname = nickname)
            sendAction(ChatAction.SetConversation(convo.copy(customNickname = nickname)))
        }
    }

    fun onToggleMute(isMuted: Boolean) {
        val convo = uiStateFlow.value.currentConversation ?: return
        viewModelScope.launch {
            updateGroupInfoUseCase(convo.id, isMuted = isMuted)
            sendAction(ChatAction.SetConversation(convo.copy(isMuted = isMuted)))
        }
    }

    fun onManageMemberRole(targetUserId: String, newRole: GroupRole) {
        val convo = uiStateFlow.value.currentConversation ?: return
        viewModelScope.launch {
            manageGroupMemberUseCase(convo.id, targetUserId, newRole = newRole)
        }
    }

    fun onRemoveMember(targetUserId: String) {
        val convo = uiStateFlow.value.currentConversation ?: return
        viewModelScope.launch {
            manageGroupMemberUseCase(convo.id, targetUserId, isRemove = true)
        }
    }

    fun onLeaveGroup(onSuccess: () -> Unit, onError: (String) -> Unit = {}) {
        val convo = uiStateFlow.value.currentConversation ?: return
        viewModelScope.launch {
            try {
                leaveGroupUseCase(convo.id)
                sendAction(ChatAction.SetChatInfoOpen(false))
                onSuccess()
            } catch (e: GroupError.LastAdminCannotLeave) {
                onError(e.message ?: "DEC-07: Quản trị viên cuối cùng không thể rời nhóm.")
            }
        }
    }

    // UI Overhaul Prototype Alignment: Slow-Send, Attachment, Location, Group Edit & NewGroup Selection
    fun onOpenSlowSendSheet(isOpen: Boolean) {
        sendAction(ChatAction.SetSlowSendSheetOpen(isOpen))
    }

    fun onOpenAttachmentSheet(isOpen: Boolean) {
        sendAction(ChatAction.SetAttachmentSheetOpen(isOpen))
    }

    fun onUpdateSlowSendDraft(
        content: String = uiStateFlow.value.slowSendDraftContent,
        scheduleLabel: String = uiStateFlow.value.slowSendScheduleLabel,
        unlockEpochMs: Long? = uiStateFlow.value.slowSendUnlockEpochMs,
    ) {
        sendAction(
            ChatAction.UpdateSlowSendDraft(
                content = content,
                scheduleLabel = scheduleLabel,
                unlockEpochMs = unlockEpochMs,
            ),
        )
    }

    fun onSealSlowSendMessage() {
        val currentState = uiStateFlow.value
        val draftText = currentState.slowSendDraftContent.trim().ifEmpty {
            currentState.inputContent.trim().ifEmpty { "Lời nhắn dịu dàng hẹn gửi…" }
        }
        val unlockEpochMs = currentState.slowSendUnlockEpochMs
            ?: (System.currentTimeMillis() + DEFAULT_SLOW_SEND_DELAY_MS)
        val scheduleLabel = currentState.slowSendScheduleLabel
        viewModelScope.launch {
            sendSealedMessageUseCase(
                conversationId = currentState.conversationId,
                content = draftText,
                mood = currentState.selectedMood,
                unlockAtEpochMs = unlockEpochMs,
                scheduledLabel = scheduleLabel,
            )
            sendAction(
                ChatAction.UpdateSlowSendDraft(
                    content = "",
                    scheduleLabel = scheduleLabel,
                    unlockEpochMs = null,
                ),
            )
            sendAction(ChatAction.SetSlowSendSheetOpen(false))
        }
    }

    fun onToggleRevealSealedMessage(messageId: String) {
        val target = uiStateFlow.value.messages.find { it.id == messageId } ?: return
        if (target.isSealedRevealed) {
            sendAction(ChatAction.ToggleRevealSealedMessage(messageId))
            return
        }
        val unlockAt = target.unlockAtEpochMs
        val isTimeReached = unlockAt == null || System.currentTimeMillis() >= unlockAt
        if (!target.isFromMe && !isTimeReached) return

        val needsRemoteContent = target.content.isBlank() || target.content.startsWith("✉️ Thư hẹn")
        if (needsRemoteContent) {
            viewModelScope.launch {
                val realContent = runCatching { revealSealedMessageUseCase(messageId) }.getOrNull()
                if (!realContent.isNullOrBlank()) {
                    val updatedList = uiStateFlow.value.messages.map { msg ->
                        if (msg.id == messageId) msg.copy(content = realContent, isSealedRevealed = true) else msg
                    }
                    sendAction(ChatAction.SetMessages(updatedList))
                } else if (target.isFromMe || isTimeReached) {
                    sendAction(ChatAction.ToggleRevealSealedMessage(messageId))
                }
            }
        } else {
            sendAction(ChatAction.ToggleRevealSealedMessage(messageId))
            if (!target.isFromMe) {
                viewModelScope.launch {
                    runCatching { revealSealedMessageUseCase(messageId) }
                }
            }
        }
    }

    fun onCancelSealedMessage(messageId: String) {
        sendAction(ChatAction.CancelSealedMessage(messageId))
        viewModelScope.launch {
            deleteMessageUseCase(messageId)
        }
    }

    fun onSendLocationBubble(
        distanceLabel: String = "Mình đang ở đây · cách bạn 1,2 km",
    ) {
        val currentState = uiStateFlow.value
        val locationMsg = ChatMessage(
            id = "loc_${System.currentTimeMillis()}",
            conversationId = currentState.conversationId,
            senderId = "me",
            senderName = "Mình",
            content = distanceLabel,
            mood = currentState.selectedMood,
            status = MessageStatus.SENT,
            isFromMe = true,
            mediaType = MessageMediaType.LOCATION,
            locationDistanceLabel = distanceLabel,
        )
        sendAction(ChatAction.AppendLocalMessage(locationMsg))
        sendAction(ChatAction.SetAttachmentSheetOpen(false))
    }

    fun onOpenGroupEditSheet(isOpen: Boolean) {
        sendAction(ChatAction.SetGroupEditSheetOpen(isOpen))
    }

    fun onUpdateGroupTitleAndMood(title: String, mood: MoodType) {
        val convo = uiStateFlow.value.currentConversation
        if (convo != null) {
            viewModelScope.launch {
                updateGroupInfoUseCase(convo.id, title = title, mood = mood)
                sendAction(
                    ChatAction.SetConversation(
                        convo.copy(
                            title = title,
                            mood = mood,
                        ),
                    ),
                )
            }
        }
        sendAction(ChatAction.SetGroupEditSheetOpen(false))
    }

    fun onToggleNewGroupMember(memberId: String) {
        sendAction(ChatAction.ToggleNewGroupMember(memberId))
    }

    fun onUpdateNewGroupDraft(title: String, mood: MoodType) {
        sendAction(ChatAction.UpdateNewGroupDraft(title = title, mood = mood))
    }
}
