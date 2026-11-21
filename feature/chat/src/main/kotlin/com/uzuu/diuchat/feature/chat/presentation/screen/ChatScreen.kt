package com.uzuu.diuchat.feature.chat.presentation.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMediaSourceSheet
import com.uzuu.diuchat.feature.base.presentation.util.rememberMediaPickerController
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState
import com.uzuu.diuchat.feature.chat.presentation.composable.ChatComposer
import com.uzuu.diuchat.feature.chat.presentation.composable.ForwardDialog
import com.uzuu.diuchat.feature.chat.presentation.composable.MessageBubble
import com.uzuu.diuchat.feature.chat.presentation.composable.PinnedMessageBar
import com.uzuu.diuchat.feature.chat.presentation.composable.ReactDetailDialog
import com.uzuu.diuchat.feature.chat.presentation.composable.ReplyQuoteBar
import com.uzuu.diuchat.feature.chat.presentation.composable.VoiceRecorderBar
import org.koin.androidx.compose.koinViewModel

@Suppress("compose:vm-forwarding-check")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    conversationId: String = "conv_linh_diu",
    onStartVoiceCall: () -> Unit = {},
    onStartVideoCall: () -> Unit = {},
    onAttachLocationClick: () -> Unit = {},
    onSendDelayedLetterClick: () -> Unit = {},
    onSearchInChatClick: () -> Unit = {},
    onOpenSharedMediaClick: () -> Unit = {},
    onOpenPendingLettersClick: () -> Unit = {},
    viewModel: ChatViewModel = koinViewModel(),
) {
    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    var showMediaSourceSheet by remember { mutableStateOf(false) }
    var showAttachmentOptionsSheet by remember { mutableStateOf(false) }

    val mediaPickerController = rememberMediaPickerController(
        onSelectImage = { uri: Uri ->
            viewModel.onPhotoSelected(uri.toString())
        },
    )

    val partnerMoodColor = Color(state.partnerMood.colorHex)

    // Sub-screen: ChatInfoScreen
    if (state.isChatInfoOpen) {
        val currentConvo = state.currentConversation ?: Conversation(
            id = state.conversationId,
            title = state.conversationTitle,
            mood = state.partnerMood,
            lastMessage = "",
            lastMessageTimeEpochMs = 0L,
            daysTogether = 120,
            anniversaryDate = "14/02/2024",
        )
        ChatInfoScreen(
            conversation = currentConvo,
            onBackClick = { viewModel.onOpenChatInfo(false) },
            onLeaveGroup = {
                viewModel.onLeaveGroup(
                    onSuccess = {
                        viewModel.onOpenChatInfo(false)
                        onBackClick()
                    },
                )
            },
            onUpdateNickname = viewModel::onUpdateNickname,
            onToggleMute = viewModel::onToggleMute,
            onManageMemberRole = viewModel::onManageMemberRole,
            onRemoveMember = viewModel::onRemoveMember,
            onVoiceCallClick = onStartVoiceCall,
            onVideoCallClick = onStartVideoCall,
            onSearchInChatClick = onSearchInChatClick,
            onOpenSharedMediaClick = onOpenSharedMediaClick,
            onOpenPendingLettersClick = onOpenPendingLettersClick,
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
        onStartVoiceCall = onStartVoiceCall,
        onStartVideoCall = onStartVideoCall,
        onSendDelayedLetterClick = onSendDelayedLetterClick,
        onOpenChatInfo = { viewModel.onOpenChatInfo(true) },
        onOpenNewGroup = { viewModel.onOpenNewGroup(true) },
        onUnpinCurrentMessage = viewModel::onUnpinCurrentMessage,
        onCancelReply = viewModel::onCancelReply,
        onCancelVoiceRecording = viewModel::onCancelVoiceRecording,
        onStopVoiceRecording = { viewModel.onStopVoiceRecording() },
        onSendVoiceMessage = viewModel::onSendVoiceMessage,
        onTogglePreviewPlay = viewModel::onTogglePreviewPlay,
        onInputChange = viewModel::onInputChanged,
        onSelectMood = viewModel::onSelectMood,
        onToggleMoodTray = viewModel::onToggleMoodTray,
        onAttachPhotoClick = {
            showAttachmentOptionsSheet = true
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
        modifier = modifier,
    )

    if (showAttachmentOptionsSheet) {
        ChatAttachmentOptionsSheet(
            onPickPhotoClick = {
                showAttachmentOptionsSheet = false
                showMediaSourceSheet = true
            },
            onAttachLocationClick = {
                showAttachmentOptionsSheet = false
                onAttachLocationClick()
            },
            onSendDelayedLetterClick = {
                showAttachmentOptionsSheet = false
                onSendDelayedLetterClick()
            },
            onStartVoiceClick = {
                showAttachmentOptionsSheet = false
                viewModel.onStartVoiceRecording()
            },
            onDismiss = { showAttachmentOptionsSheet = false },
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
    onSendDelayedLetterClick: () -> Unit = {},
    onOpenChatInfo: () -> Unit = {},
    onOpenNewGroup: () -> Unit = {},
    onUnpinCurrentMessage: () -> Unit = {},
    onCancelReply: () -> Unit = {},
    onCancelVoiceRecording: () -> Unit = {},
    onStopVoiceRecording: () -> Unit = {},
    onSendVoiceMessage: () -> Unit = {},
    onTogglePreviewPlay: () -> Unit = {},
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
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val partnerMoodColor = Color(state.partnerMood.colorHex)

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onOpenChatInfo() },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(partnerMoodColor),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = state.conversationTitle,
                                    style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = AppTheme.colors.ink,
                                )
                                if (state.isPartnerInQuietHours) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bedtime,
                                            contentDescription = "Giờ yên lặng",
                                            tint = AppTheme.colors.quietPurple,
                                            modifier = Modifier.size(11.dp),
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Đang trong giờ yên lặng",
                                            style = AppTheme.typography.bodySmall,
                                            color = AppTheme.colors.quietPurple,
                                            fontSize = 11.sp,
                                        )
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = AppTheme.colors.ink,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onStartVoiceCall) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Gọi thoại",
                                tint = AppTheme.colors.ink,
                            )
                        }
                        IconButton(onClick = onStartVideoCall) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Gọi video",
                                tint = AppTheme.colors.ink,
                            )
                        }
                        IconButton(onClick = onOpenNewGroup) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = "Tạo nhóm mới",
                                tint = AppTheme.colors.ink,
                            )
                        }
                        IconButton(onClick = onOpenChatInfo) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Thông tin hội thoại",
                                tint = AppTheme.colors.ink,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppTheme.colors.surfaceElevated,
                    ),
                )

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
                if (state.isPartnerInQuietHours) {
                    QuietHoursSlowSendBanner(
                        partnerName = state.conversationTitle,
                        onSlowSendClick = onSendDelayedLetterClick,
                    )
                }

                state.replyingTo?.let { replyingMessage ->
                    ReplyQuoteBar(
                        replyingTo = replyingMessage,
                        onCancelReply = onCancelReply,
                    )
                }

                if (state.voiceRecordingState !is VoiceRecordingState.Idle) {
                    VoiceRecorderBar(
                        recordingState = state.voiceRecordingState,
                        selectedMood = state.selectedMood,
                        onCancelRecording = onCancelVoiceRecording,
                        onStopRecording = onStopVoiceRecording,
                        onSendVoiceMessage = onSendVoiceMessage,
                        onTogglePreviewPlay = onTogglePreviewPlay,
                    )
                } else {
                    ChatComposer(
                        inputContent = state.inputContent,
                        selectedMood = state.selectedMood,
                        isMoodTrayOpen = state.isMoodTrayOpen,
                        isSending = state.isSending,
                        pendingPhotoUri = state.pendingPhotoUri,
                        onInputChange = onInputChange,
                        onSelectMood = onSelectMood,
                        onToggleMoodTray = onToggleMoodTray,
                        onAttachPhotoClick = onAttachPhotoClick,
                        onClearPhotoClick = onClearPhoto,
                        onStartVoiceRecording = onStartVoiceRecording,
                        onSendClick = onSendMessage,
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (state.messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(partnerMoodColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(partnerMoodColor),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bắt đầu cuộc trò chuyện",
                            style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = AppTheme.colors.ink,
                        )
                        Text(
                            text = "Gửi lời nhắn mang sắc thái cảm xúc dịu êm.",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.inkSecondary,
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = state.messages,
                        key = { it.id },
                    ) { message ->
                        MessageBubble(
                            message = message,
                            onReplyClick = onReplyToMessage,
                            isPlayingAudio = state.playingAudioMessageId == message.id,
                            audioProgress = state.audioPlaybackProgress,
                            onToggleAudioPlayback = onToggleAudioPlayback,
                            onReactionClick = onToggleReaction,
                            onViewReactions = onOpenReactionDetail,
                            onPinClick = onTogglePinMessage,
                            onForwardClick = onOpenForwardDialog,
                        )
                    }
                }
            }

            // Reaction details dialog
            state.activeReactionDetailMessage?.let { detailMsg ->
                ReactDetailDialog(
                    reactions = detailMsg.reactions,
                    onDismiss = onCloseReactionDetail,
                )
            }

            // Forward dialog
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
private fun QuietHoursSlowSendBanner(
    partnerName: String,
    onSlowSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.quietPurple.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = "Giờ yên lặng",
                    tint = AppTheme.colors.quietPurple,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "$partnerName đang trong giờ yên lặng · Tin nhắn sẽ giữ im lặng",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.quietPurple,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppTheme.colors.quietPurple.copy(alpha = 0.18f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onSlowSendClick),
            ) {
                Text(
                    text = "Gửi chậm",
                    style = AppTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.quietPurple,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatAttachmentOptionsSheet(
    onPickPhotoClick: () -> Unit,
    onAttachLocationClick: () -> Unit,
    onSendDelayedLetterClick: () -> Unit,
    onStartVoiceClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Đính kèm trong trò chuyện",
                style = AppTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
            )

            ChatAttachmentOptionRow(
                icon = Icons.Default.PhotoLibrary,
                title = "Ảnh & Máy ảnh",
                subtitle = "Chọn ảnh từ thư viện hoặc chụp mới",
                onClick = onPickPhotoClick,
            )
            ChatAttachmentOptionRow(
                icon = Icons.Default.LocationOn,
                title = "Chia sẻ vị trí",
                subtitle = "Gửi vị trí hiện tại hoặc chia sẻ trực tiếp",
                onClick = onAttachLocationClick,
            )
            ChatAttachmentOptionRow(
                icon = Icons.Default.MailOutline,
                title = "Thư gửi chậm",
                subtitle = "Hẹn giờ mở thư vào thời khắc dịu dàng",
                onClick = onSendDelayedLetterClick,
            )
            ChatAttachmentOptionRow(
                icon = Icons.Default.Mic,
                title = "Lời nhắn thoại",
                subtitle = "Ghi âm giọng nói gửi ngay",
                onClick = onStartVoiceClick,
            )
        }
    }
}

@Composable
private fun ChatAttachmentOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.ink,
                )
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    AppTheme {
        ChatContent(
            state = ChatUiState(
                conversationTitle = "Linh Dịu",
                partnerMood = MoodType.BINHYEN,
                messages = listOf(
                    ChatMessage(
                        id = "msg_1",
                        conversationId = "conv_1",
                        senderId = "partner",
                        senderName = "Linh",
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
