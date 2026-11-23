package com.uzuu.diuchat.feature.chat.presentation.screen

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuChatMessagesSkeleton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMediaSourceSheet
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodDot
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.util.rememberMediaPickerController
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import com.uzuu.diuchat.feature.chat.presentation.composable.ChatComposer
import com.uzuu.diuchat.feature.chat.presentation.composable.ForwardDialog
import com.uzuu.diuchat.feature.chat.presentation.composable.MessageBubble
import com.uzuu.diuchat.feature.chat.presentation.composable.PinnedMessageBar
import com.uzuu.diuchat.feature.chat.presentation.composable.ReactDetailDialog
import com.uzuu.diuchat.feature.chat.presentation.composable.ReplyQuoteBar
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Suppress("compose:vm-forwarding-check")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    conversationId: String = "",
    pendingReplyMessageId: String? = null,
    onConsumePendingReply: () -> Unit = {},
    onStartVoiceCall: (
        calleeId: String,
        conversationId: String,
        peerName: String,
        peerMoodHex: String,
    ) -> Unit = { _, _, _, _ -> },
    onStartVideoCall: (
        calleeId: String,
        conversationId: String,
        peerName: String,
        peerMoodHex: String,
    ) -> Unit = { _, _, _, _ -> },
    onOpenPhotoClick: (ChatMessage) -> Unit = {},
    onOpenPhotoViewer: (
        imageUrl: String,
        messageId: String,
        conversationId: String,
        caption: String,
        moodName: String,
        senderName: String,
        timestamp: String,
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onAttachLocationClick: () -> Unit = {},
    @Suppress("UnusedParameter") onSendDelayedLetterClick: () -> Unit = {},
    onSearchInChatClick: () -> Unit = {},
    onOpenSharedMediaClick: () -> Unit = {},
    onOpenPendingLettersClick: () -> Unit = {},
    viewModel: ChatViewModel = koinViewModel(),
) {
    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val currentOnConsumePendingReply by rememberUpdatedState(onConsumePendingReply)

    LaunchedEffect(pendingReplyMessageId, state.messages) {
        if (!pendingReplyMessageId.isNullOrBlank() && state.messages.any { it.id == pendingReplyMessageId }) {
            viewModel.onReplyToMessageById(pendingReplyMessageId)
            currentOnConsumePendingReply()
        }
    }
    var showMediaSourceSheet by remember { mutableStateOf(false) }

    val mediaPickerController = rememberMediaPickerController(
        onSelectImage = { uri: Uri ->
            viewModel.onPhotoSelected(uri.toString())
        },
    )

    val peerMoodHex = remember(state.partnerMood) {
        String.format(Locale.US, "#%06X", (state.partnerMood.colorHex and 0xFFFFFF))
    }

    val resolvedCalleeId = remember(state.currentConversation, state.messages) {
        state.currentConversation?.directPeerId
            ?: state.messages.firstOrNull { !it.isFromMe && it.senderId.isNotBlank() }?.senderId
            ?: ""
    }

    val handleVoiceCall = {
        onStartVoiceCall(resolvedCalleeId, state.conversationId, state.conversationTitle, peerMoodHex)
    }
    val handleVideoCall = {
        onStartVideoCall(resolvedCalleeId, state.conversationId, state.conversationTitle, peerMoodHex)
    }
    val handlePhotoOpen: (ChatMessage) -> Unit = { message ->
        onOpenPhotoClick(message)
        val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.createdAtEpochMs))
        onOpenPhotoViewer(
            message.mediaUri.orEmpty(),
            message.id,
            message.conversationId,
            message.caption ?: message.content,
            message.mood.displayName,
            message.senderName,
            timeText,
        )
    }

    // Sub-screen: ChatInfoScreen
    if (state.isChatInfoOpen) {
        val currentConvo = state.currentConversation ?: Conversation(
            id = state.conversationId,
            title = state.conversationTitle,
            mood = state.partnerMood,
            lastMessage = "",
            lastMessageTimeEpochMs = 0L,
            daysTogether = 1,
        )
        ChatInfoScreen(
            conversation = currentConvo,
            messages = state.messages,
            onBackClick = { viewModel.onOpenChatInfo(false) },
            onLeaveGroup = {
                viewModel.onLeaveGroup(
                    onSuccess = {
                        viewModel.onOpenChatInfo(false)
                        onBackClick()
                    },
                )
            },
            onDeleteConversation = {
                viewModel.onDeleteConversation(
                    onSuccess = onBackClick,
                )
            },
            onUpdateNickname = viewModel::onUpdateNickname,
            onToggleMute = viewModel::onToggleMute,
            onManageMemberRole = viewModel::onManageMemberRole,
            onRemoveMember = viewModel::onRemoveMember,
            onVoiceCallClick = handleVoiceCall,
            onVideoCallClick = handleVideoCall,
            onSearchInChatClick = onSearchInChatClick,
            onOpenSharedMediaClick = onOpenSharedMediaClick,
            onOpenPendingLettersClick = onOpenPendingLettersClick,
            onOpenPhotoViewer = onOpenPhotoViewer,
            isGroupEditSheetOpen = state.isGroupEditSheetOpen,
            onOpenGroupEditSheet = viewModel::onOpenGroupEditSheet,
            onUpdateGroupTitleAndMood = viewModel::onUpdateGroupTitleAndMood,
            onOpenNewGroup = {
                viewModel.onOpenChatInfo(false)
                viewModel.onOpenNewGroup(true)
            },
        )
        return
    }

    // Sub-screen: NewGroupScreen
    if (state.isNewGroupOpen) {
        NewGroupScreen(
            onBackClick = { viewModel.onOpenNewGroup(false) },
            onCreateGroupSuccess = { _ ->
                viewModel.onOpenNewGroup(false)
            },
            selectedMemberIds = state.newGroupSelectedMemberIds,
            groupTitle = state.newGroupTitle,
            selectedMood = state.newGroupMood,
            onToggleMember = viewModel::onToggleNewGroupMember,
            onUpdateDraft = viewModel::onUpdateNewGroupDraft,
            onCreateGroup = { title, mood, members, onSuccess ->
                viewModel.onCreateGroup(
                    title = title,
                    mood = mood,
                    memberIds = members,
                    onSuccess = onSuccess,
                )
            },
        )
        return
    }

    ChatContent(
        state = state,
        onBackClick = onBackClick,
        onStartVoiceCall = handleVoiceCall,
        onStartVideoCall = handleVideoCall,
        onOpenSlowSendSheet = { viewModel.onOpenSlowSendSheet(true) },
        onOpenChatInfo = { viewModel.onOpenChatInfo(true) },
        onUnpinCurrentMessage = viewModel::onUnpinCurrentMessage,
        onCancelReply = viewModel::onCancelReply,
        onCancelVoiceRecording = viewModel::onCancelVoiceRecording,
        onSendVoiceMessage = viewModel::onSendVoiceMessage,
        onInputChange = viewModel::onInputChanged,
        onSelectMood = viewModel::onSelectMood,
        onToggleMoodTray = viewModel::onToggleMoodTray,
        onAttachPhotoClick = {
            viewModel.onOpenAttachmentSheet(!state.isAttachmentSheetOpen)
        },
        onClearPhoto = viewModel::onClearPhoto,
        onStartVoiceRecording = viewModel::onStartVoiceRecording,
        onSendMessage = viewModel::onSendMessage,
        onReplyToMessage = viewModel::onReplyToMessage,
        onToggleAudioPlayback = viewModel::onToggleAudioPlayback,
        onToggleReaction = viewModel::onToggleReaction,
        onOpenReactionDetail = viewModel::onOpenReactionDetail,
        onCloseReactionDetail = viewModel::onCloseReactionDetail,
        onTogglePinMessage = viewModel::onTogglePinMessage,
        onOpenForwardDialog = viewModel::onOpenForwardDialog,
        onCloseForwardDialog = viewModel::onCloseForwardDialog,
        onConfirmForward = viewModel::onConfirmForward,
        onToggleSealedReveal = viewModel::onToggleRevealSealedMessage,
        onCancelScheduledLetter = viewModel::onCancelSealedMessage,
        onPhotoClick = handlePhotoOpen,
        onLoadOlderMessages = viewModel::onLoadOlderMessages,
        modifier = modifier,
    )

    if (state.isAttachmentSheetOpen) {
        ChatAttachmentOptionsSheet(
            onTakePhotoClick = {
                viewModel.onOpenAttachmentSheet(false)
                mediaPickerController.launchCamera()
            },
            onPickRecentPhotoClick = { demoUri ->
                viewModel.onOpenAttachmentSheet(false)
                viewModel.onPhotoSelected(demoUri)
            },
            onPickGalleryClick = {
                viewModel.onOpenAttachmentSheet(false)
                showMediaSourceSheet = true
            },
            onAttachLocationClick = {
                viewModel.onSendLocationBubble()
                onAttachLocationClick()
            },
            onStartVoiceClick = {
                viewModel.onOpenAttachmentSheet(false)
                viewModel.onStartVoiceRecording()
            },
            onOpenSlowSendClick = {
                viewModel.onOpenAttachmentSheet(false)
                viewModel.onOpenSlowSendSheet(true)
            },
            onDismiss = { viewModel.onOpenAttachmentSheet(false) },
        )
    }

    if (state.isSlowSendSheetOpen) {
        ChatSlowSendSheet(
            partnerName = state.conversationTitle,
            draftContent = state.slowSendDraftContent,
            selectedSchedule = state.slowSendScheduleLabel,
            moodColor = Color(state.selectedMood.colorHex),
            onDraftChange = { newText ->
                viewModel.onUpdateSlowSendDraft(
                    content = newText,
                    scheduleLabel = state.slowSendScheduleLabel,
                    unlockEpochMs = state.slowSendUnlockEpochMs,
                )
            },
            onScheduleSelect = { schedule, unlockEpochMs ->
                viewModel.onUpdateSlowSendDraft(
                    content = state.slowSendDraftContent,
                    scheduleLabel = schedule,
                    unlockEpochMs = unlockEpochMs,
                )
            },
            onSealAndSchedule = {
                viewModel.onSealSlowSendMessage()
            },
            onDismiss = { viewModel.onOpenSlowSendSheet(false) },
        )
    }

    if (showMediaSourceSheet) {
        DiuMediaSourceSheet(
            title = "Gửi ảnh trong trò chuyện",
            onPickFromGallery = mediaPickerController::launchPhotoPicker,
            onTakePhoto = mediaPickerController::launchCamera,
            onDismiss = { showMediaSourceSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    state: ChatUiState,
    onBackClick: () -> Unit,
    onStartVoiceCall: () -> Unit = {},
    onStartVideoCall: () -> Unit = {},
    onOpenSlowSendSheet: () -> Unit = {},
    onOpenChatInfo: () -> Unit = {},
    onUnpinCurrentMessage: () -> Unit = {},
    onCancelReply: () -> Unit = {},
    onCancelVoiceRecording: () -> Unit = {},
    onSendVoiceMessage: () -> Unit = {},
    onInputChange: (String) -> Unit = {},
    onSelectMood: (MoodType) -> Unit = {},
    onToggleMoodTray: () -> Unit = {},
    onAttachPhotoClick: () -> Unit = {},
    onClearPhoto: () -> Unit = {},
    onStartVoiceRecording: () -> Unit = {},
    onSendMessage: () -> Unit = {},
    onReplyToMessage: (ChatMessage) -> Unit = {},
    onToggleAudioPlayback: (ChatMessage) -> Unit = {},
    onToggleReaction: (ChatMessage, MoodType) -> Unit = { _, _ -> },
    onOpenReactionDetail: (ChatMessage) -> Unit = {},
    onCloseReactionDetail: () -> Unit = {},
    onTogglePinMessage: (ChatMessage) -> Unit = {},
    onOpenForwardDialog: (ChatMessage) -> Unit = {},
    onCloseForwardDialog: () -> Unit = {},
    onConfirmForward: (List<String>) -> Unit = {},
    onToggleSealedReveal: (String) -> Unit = {},
    onCancelScheduledLetter: (String) -> Unit = {},
    onPhotoClick: (ChatMessage) -> Unit = {},
    onLoadOlderMessages: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val colors = AppTheme.colors
    val partnerMoodColor = Color(state.partnerMood.colorHex)
    val isGroup = state.currentConversation?.isGroup == true
    val currentOnLoadOlder by rememberUpdatedState(onLoadOlderMessages)

    var previousFirstMessageId by remember(state.conversationId) { mutableStateOf<String?>(null) }
    var previousLastMessageId by remember(state.conversationId) { mutableStateOf<String?>(null) }
    var previousMessageCount by remember(state.conversationId) { mutableStateOf(0) }

    LaunchedEffect(state.conversationId, state.messages.size, state.messages.firstOrNull()?.id, state.messages.lastOrNull()?.id) {
        val messages = state.messages
        if (messages.isEmpty()) {
            previousFirstMessageId = null
            previousLastMessageId = null
            previousMessageCount = 0
            return@LaunchedEffect
        }
        val firstId = messages.first().id
        val lastId = messages.last().id
        val headerOffset = if (state.isLoadingOlderMessages) 1 else 0

        when {
            previousMessageCount == 0 -> {
                listState.scrollToItem(messages.lastIndex + headerOffset)
            }

            firstId != previousFirstMessageId && messages.size > previousMessageCount -> {
                val anchorIdx = previousFirstMessageId?.let { oldId ->
                    messages.indexOfFirst { it.id == oldId }
                } ?: -1
                if (anchorIdx > 0) {
                    val savedOffset = listState.firstVisibleItemScrollOffset
                    val currentVisibleInList = (listState.firstVisibleItemIndex - headerOffset).coerceAtLeast(0)
                    listState.scrollToItem(
                        index = (anchorIdx + currentVisibleInList + headerOffset).coerceAtMost(messages.lastIndex + headerOffset),
                        scrollOffset = savedOffset,
                    )
                }
            }

            lastId != previousLastMessageId -> {
                val lastVisibleIdx = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val isNearBottom = lastVisibleIdx >= (previousMessageCount - 3).coerceAtLeast(0)
                if (isNearBottom || messages.last().isFromMe) {
                    listState.animateScrollToItem(messages.lastIndex + headerOffset)
                }
            }
        }

        previousFirstMessageId = firstId
        previousLastMessageId = lastId
        previousMessageCount = messages.size
    }

    LaunchedEffect(
        listState,
        state.conversationId,
        state.hasMoreMessages,
        state.isLoadingOlderMessages,
        state.isLoadingMessages,
        state.messages.size,
    ) {
        if (
            !state.hasMoreMessages ||
            state.isLoadingOlderMessages ||
            state.isLoadingMessages ||
            state.messages.size < ChatRepository.DEFAULT_PAGE_SIZE
        ) {
            return@LaunchedEffect
        }
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstVisibleIndex ->
                if (firstVisibleIndex <= 2) {
                    currentOnLoadOlder()
                }
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Column {
                // Prototype .chat-h.glass header with ONLY Phone & Video call actions
                Surface(
                    color = colors.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DiuIconButton(
                                onClick = onBackClick,
                                contentDescription = "Quay lại",
                            ) {
                                Icon(
                                    painter = painterResource(AppIcons.Back),
                                    contentDescription = null,
                                    tint = colors.ink,
                                    modifier = Modifier.size(22.dp),
                                )
                            }

                            // Clickable .chat-hit area (opens ChatInfoScreen)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(onClick = onOpenChatInfo)
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                DiuOrbAvatar(
                                    initials = state.conversationTitle.take(1).ifEmpty { "D" },
                                    moodColor = partnerMoodColor,
                                    size = 40.dp,
                                    badgeText = if (isGroup) "${state.currentConversation?.members?.size ?: 4}" else null,
                                )

                                Column {
                                    Text(
                                        text = state.conversationTitle,
                                        style = AppTheme.typography.titleMedium.copy(
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        color = colors.ink,
                                    )
                                    if (state.isPartnerTyping) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        ) {
                                            DiuMoodDot(
                                                color = partnerMoodColor,
                                                size = 7.dp,
                                            )
                                            Text(
                                                text = "đang nhập…",
                                                style = AppTheme.typography.bodySmall.copy(
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                ),
                                                color = colors.accent,
                                            )
                                        }
                                    } else if (state.isPartnerInQuietHours) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Icon(
                                                painter = painterResource(AppIcons.Moon),
                                                contentDescription = "Giờ yên lặng",
                                                tint = colors.quietPurple,
                                                modifier = Modifier.size(12.dp),
                                            )
                                            Text(
                                                text = "đang nghỉ ngơi",
                                                style = AppTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = colors.quietPurple,
                                            )
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        ) {
                                            DiuMoodDot(
                                                color = partnerMoodColor,
                                                size = 7.dp,
                                            )
                                            Text(
                                                text = "đang thấy ${state.partnerMood.displayName.lowercase()}",
                                                style = AppTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = colors.inkSecondary,
                                            )
                                        }
                                    }
                                }
                            }

                            // ONLY 2 actions: Voice Call & Video Call
                            DiuIconButton(
                                onClick = onStartVoiceCall,
                                contentDescription = "Gọi thoại",
                            ) {
                                Icon(
                                    painter = painterResource(AppIcons.Phone),
                                    contentDescription = null,
                                    tint = colors.ink,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            DiuIconButton(
                                onClick = onStartVideoCall,
                                contentDescription = "Gọi video",
                            ) {
                                Icon(
                                    painter = painterResource(AppIcons.Video),
                                    contentDescription = null,
                                    tint = colors.ink,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        HorizontalDivider(thickness = 1.dp, color = colors.line)
                    }
                }

                // Sticky Pinned Message Banner
                state.pinnedSnippet?.let { snippet ->
                    PinnedMessageBar(
                        pinnedSnippet = snippet,
                        moodColor = partnerMoodColor,
                        onBarClick = {
                            val pinnedId = state.currentConversation?.pinnedMessageId
                            val targetIndex = (
                                if (pinnedId != null) {
                                    state.messages.indexOfFirst { it.id == pinnedId }
                                } else {
                                    -1
                                }
                                ).takeIf { it >= 0 }
                                ?: state.messages.indexOfFirst { it.isPinned }.takeIf { it >= 0 }
                                ?: state.messages.indexOfFirst { it.content == snippet }
                            if (targetIndex >= 0) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                        },
                        onUnpinClick = onUnpinCurrentMessage,
                    )
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.imePadding()) {
                state.replyingTo?.let { replyingMessage ->
                    ReplyQuoteBar(
                        replyingTo = replyingMessage,
                        onCancelReply = onCancelReply,
                    )
                }

                ChatComposer(
                    inputContent = state.inputContent,
                    selectedMood = state.selectedMood,
                    isMoodTrayOpen = state.isMoodTrayOpen,
                    isSending = state.isSending,
                    pendingPhotoUri = state.pendingPhotoUri,
                    partnerName = state.conversationTitle,
                    isAttachmentOpen = state.isAttachmentSheetOpen,
                    voiceRecordingState = state.voiceRecordingState,
                    onInputChange = onInputChange,
                    onSelectMood = onSelectMood,
                    onToggleMoodTray = onToggleMoodTray,
                    onAttachPhotoClick = onAttachPhotoClick,
                    onClearPhotoClick = onClearPhoto,
                    onSlowSendClick = onOpenSlowSendSheet,
                    onStartVoiceRecording = onStartVoiceRecording,
                    onCancelVoiceRecording = onCancelVoiceRecording,
                    onSendVoiceMessage = onSendVoiceMessage,
                    onSendClick = onSendMessage,
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (state.isLoadingMessages && state.messages.isEmpty()) {
                DiuChatMessagesSkeleton(modifier = Modifier.fillMaxSize())
            } else if (state.messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        DiuOrbAvatar(
                            initials = state.conversationTitle.take(1).ifEmpty { "D" },
                            moodColor = partnerMoodColor,
                            size = 56.dp,
                            isPulsing = true,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Bắt đầu cuộc trò chuyện",
                            style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = colors.ink,
                        )
                        Text(
                            text = "Gửi lời nhắn mang sắc thái cảm xúc dịu êm.",
                            style = AppTheme.typography.bodySmall,
                            color = colors.inkSecondary,
                        )
                        if (state.isPartnerInQuietHours) {
                            Spacer(modifier = Modifier.height(16.dp))
                            QuietNoteInlinePill(
                                partnerName = state.conversationTitle,
                                onSlowSendClick = onOpenSlowSendSheet,
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                ) {
                    if (state.isLoadingOlderMessages) {
                        item(key = "load_older_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = partnerMoodColor,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đang tải tin nhắn cũ hơn…",
                                    style = AppTheme.typography.bodySmall,
                                    color = colors.inkSecondary,
                                )
                            }
                        }
                    }
                    items(
                        items = state.messages,
                        key = { it.id },
                    ) { message ->
                        MessageBubble(
                            message = message,
                            onReplyClick = onReplyToMessage,
                            isPlayingAudio = state.playingAudioMessageId == message.id,
                            audioProgress = state.audioPlaybackProgress,
                            isGroupConversation = isGroup,
                            onToggleAudioPlayback = onToggleAudioPlayback,
                            onReactionClick = onToggleReaction,
                            onViewReactions = onOpenReactionDetail,
                            onPinClick = onTogglePinMessage,
                            onForwardClick = onOpenForwardDialog,
                            onToggleSealedReveal = onToggleSealedReveal,
                            onCancelScheduledLetter = onCancelScheduledLetter,
                            onPhotoClick = onPhotoClick,
                        )
                    }

                    if (state.isPartnerInQuietHours) {
                        item(key = "quiet_hours_inline_note") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                QuietNoteInlinePill(
                                    partnerName = state.conversationTitle,
                                    onSlowSendClick = onOpenSlowSendSheet,
                                )
                            }
                        }
                    }
                }
            }

            state.activeReactionDetailMessage?.let { detailMsg ->
                ReactDetailDialog(
                    reactions = detailMsg.reactions,
                    onDismiss = onCloseReactionDetail,
                )
            }

            state.activeForwardMessage?.let { forwardMsg ->
                ForwardDialog(
                    messageToForward = forwardMsg,
                    availableConversations = state.availableConversations,
                    onDismiss = onCloseForwardDialog,
                    onConfirmForward = onConfirmForward,
                )
            }
        }
    }
}

@Composable
private fun QuietNoteInlinePill(
    partnerName: String,
    onSlowSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onSlowSendClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.quietPurple.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, colors.quietPurple.copy(alpha = 0.45f)),
    ) {
        val annotated = buildAnnotatedString {
            append("🌙 $partnerName đang nghỉ ngơi đến 6:00. Tin nhắn sẽ nằm yên chờ thức dậy — hoặc bạn có thể ")
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append("gửi chậm")
            }
            append(".")
        }
        Text(
            text = annotated,
            style = AppTheme.typography.bodySmall,
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

private data class RecentPhotoCell(
    val label: String,
    val tint: Color,
    val uri: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatAttachmentOptionsSheet(
    onTakePhotoClick: () -> Unit,
    onPickRecentPhotoClick: (String) -> Unit,
    onPickGalleryClick: () -> Unit,
    onAttachLocationClick: () -> Unit,
    onStartVoiceClick: () -> Unit,
    onOpenSlowSendClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors
    val recentPhotos = remember {
        listOf(
            RecentPhotoCell("Hoàng hôn", AppPalette.MoodThuong, "content://diu/recent/sunset.jpg"),
            RecentPhotoCell("Tách trà", AppPalette.MoodVui, "content://diu/recent/tea.jpg"),
            RecentPhotoCell("Ban công", AppPalette.MoodBinhYen, "content://diu/recent/balcony.jpg"),
            RecentPhotoCell("Phố chiều", AppPalette.MoodNho, "content://diu/recent/street.jpg"),
            RecentPhotoCell("Mưa đêm", AppPalette.MoodTram, "content://diu/recent/rain.jpg"),
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = colors.surfaceElevated,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // 3-column grid (.att-grid): 1 Camera cell + 5 recent photo thumbnails
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Camera + 2 photos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Camera cell ("Chụp" with colors.ink background + white camera icon)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.ink)
                            .clickable(onClick = onTakePhotoClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                painter = painterResource(AppIcons.Camera),
                                contentDescription = "Chụp",
                                tint = colors.background,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = "Chụp",
                                style = AppTheme.typography.labelMedium,
                                color = colors.background,
                            )
                        }
                    }

                    recentPhotos.take(2).forEach { cell ->
                        RecentPhotoGridItem(
                            cell = cell,
                            onClick = { onPickRecentPhotoClick(cell.uri) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Row 2: 3 recent photos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    recentPhotos.drop(2).forEach { cell ->
                        RecentPhotoGridItem(
                            cell = cell,
                            onClick = { onPickRecentPhotoClick(cell.uri) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Bottom row of 4 square 56.dp buttons (.att-row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                AttachmentSquareButton(
                    iconRes = AppIcons.Image,
                    label = "Thư viện",
                    onClick = onPickGalleryClick,
                )
                AttachmentSquareButton(
                    iconRes = AppIcons.Location,
                    label = "Vị trí",
                    onClick = onAttachLocationClick,
                )
                AttachmentSquareButton(
                    iconRes = AppIcons.Mic,
                    label = "Thoại",
                    onClick = onStartVoiceClick,
                )
                AttachmentSquareButton(
                    iconRes = AppIcons.Clock,
                    label = "Gửi chậm",
                    onClick = onOpenSlowSendClick,
                )
            }
        }
    }
}

@Composable
private fun RecentPhotoGridItem(
    cell: RecentPhotoCell,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(cell.tint.copy(alpha = 0.45f))
            .border(1.dp, colors.line, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colors.photoLabelBg,
        ) {
            Text(
                text = cell.label,
                style = AppTheme.typography.labelSmall,
                color = colors.photoLabelText,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun AttachmentSquareButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AppPalette.Peach.copy(alpha = 0.45f))
                .border(1.dp, colors.line, RoundedCornerShape(18.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = colors.ink,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = label,
            style = AppTheme.typography.labelSmall,
            color = colors.inkSecondary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatSlowSendSheet(
    partnerName: String,
    draftContent: String,
    selectedSchedule: String,
    moodColor: Color,
    onDraftChange: (String) -> Unit,
    onScheduleSelect: (label: String, unlockEpochMs: Long) -> Unit,
    onSealAndSchedule: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickedDateMillis by remember { mutableStateOf<Long?>(null) }

    val customOptionLabel = "Tuỳ chọn ngày giờ…"
    val presetOptions = remember {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        val in1Hour = now.plusHours(1).atZone(zone).toInstant().toEpochMilli()
        val tonight22 = LocalDateTime.of(LocalDate.now(zone), LocalTime.of(22, 0))
            .let { if (it.isAfter(now)) it else it.plusDays(1) }
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        val tomorrow7 = LocalDateTime.of(LocalDate.now(zone).plusDays(1), LocalTime.of(7, 0))
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        listOf(
            "Sau 1 giờ" to in1Hour,
            "Tối nay 22:00" to tonight22,
            "Sáng mai 07:00" to tomorrow7,
            customOptionLabel to 0L,
        )
    }
    val presetLabels = remember(presetOptions) { presetOptions.map { it.first }.toSet() }
    val isCustomSelected = selectedSchedule.isNotBlank() && selectedSchedule !in presetLabels

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = colors.surfaceElevated,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Gửi chậm",
                style = AppTheme.headlineSerif.copy(fontSize = 28.sp),
                color = colors.ink,
            )
            Text(
                text = "Lời nhắn sẽ được niêm phong và đến bên $partnerName đúng lúc...",
                style = AppTheme.typography.bodySmall,
                color = colors.inkSecondary,
            )

            // Lined paper text box (.letter)
            val lineColor = colors.line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, moodColor.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                    .drawBehind {
                        val lineStep = 28.dp.toPx()
                        var y = lineStep
                        while (y < size.height) {
                            drawLine(
                                color = lineColor,
                                start = Offset(14.dp.toPx(), y),
                                end = Offset(size.width - 14.dp.toPx(), y),
                                strokeWidth = 1.dp.toPx(),
                            )
                            y += lineStep
                        }
                    }
                    .padding(16.dp),
            ) {
                if (draftContent.isEmpty()) {
                    Text(
                        text = "Viết đôi dòng dịu dàng gửi $partnerName…",
                        style = AppTheme.typography.bodyMedium.copy(
                            fontFamily = DiuSerif,
                            fontStyle = FontStyle.Italic,
                        ),
                        color = colors.inkTertiary,
                    )
                }
                BasicTextField(
                    value = draftContent,
                    onValueChange = onDraftChange,
                    textStyle = AppTheme.typography.bodyMedium.copy(
                        fontFamily = DiuSerif,
                        color = colors.ink,
                    ),
                    cursorBrush = SolidColor(colors.ink),
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // 2x2 schedule options grid (3 quick presets + 1 custom date/time picker)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presetOptions.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowItems.forEach { (option, epochMs) ->
                            val isCustomTile = option == customOptionLabel
                            val isSelected = if (isCustomTile) isCustomSelected else option == selectedSchedule
                            val displayLabel = if (isCustomTile && isCustomSelected) selectedSchedule else option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (isCustomTile) {
                                            showDatePicker = true
                                        } else {
                                            onScheduleSelect(option, epochMs)
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) moodColor.copy(alpha = 0.28f) else colors.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) moodColor else colors.line,
                                ),
                            ) {
                                Text(
                                    text = displayLabel,
                                    style = AppTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    ),
                                    color = colors.ink,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                )
                            }
                        }
                    }
                }
            }

            DiuButton(
                onClick = onSealAndSchedule,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Niêm phong & hẹn gửi")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true
                    },
                ) {
                    Text("Chọn giờ", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy", color = colors.inkSecondary)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val nextHour = LocalTime.now().plusHours(1)
        val timePickerState = rememberTimePickerState(
            initialHour = nextHour.hour,
            initialMinute = nextHour.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = "Chọn giờ mở lời nhắn",
                    style = AppTheme.typography.titleMedium,
                    color = colors.ink,
                )
            },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val baseDate = Instant.ofEpochMilli(
                            pickedDateMillis ?: System.currentTimeMillis(),
                        ).atZone(ZoneId.of("UTC")).toLocalDate()
                        val selectedDateTime = LocalDateTime.of(
                            baseDate,
                            LocalTime.of(timePickerState.hour, timePickerState.minute),
                        )
                        val epochMs = selectedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val formatted = selectedDateTime.format(
                            DateTimeFormatter.ofPattern("HH:mm · dd/MM/yyyy", Locale.forLanguageTag("vi-VN")),
                        )
                        showTimePicker = false
                        onScheduleSelect(formatted, epochMs)
                    },
                ) {
                    Text("Xác nhận", color = colors.accent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Hủy", color = colors.inkSecondary)
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    AppTheme {
        ChatContent(
            state = ChatUiState(
                conversationTitle = "Minh",
                partnerMood = MoodType.THUONG,
                isPartnerInQuietHours = true,
                messages = listOf(
                    ChatMessage(
                        id = "msg_1",
                        conversationId = "conv_1",
                        senderId = "partner",
                        senderName = "Minh",
                        content = "Hôm nay trời xanh và dịu êm quá!",
                        mood = MoodType.BINHYEN,
                        status = MessageStatus.SENT,
                        isFromMe = false,
                    ),
                    ChatMessage(
                        id = "msg_2",
                        conversationId = "conv_1",
                        senderId = "me",
                        senderName = "Mình",
                        content = "Thương gửi đến em những tia nắng ấm áp.",
                        mood = MoodType.THUONG,
                        status = MessageStatus.SENT,
                        isFromMe = true,
                    ),
                ),
            ),
            onBackClick = {},
        )
    }
}
