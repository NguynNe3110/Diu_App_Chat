package com.uzuu.diuchat.feature.chat.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.diuchat.feature.base.common.res.AppTheme
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
    viewModel: ChatViewModel = koinViewModel(),
) {
    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onPhotoSelected(uri.toString())
        }
    }

    // Scroll to bottom on new message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

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
            try {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            } catch (_: Exception) {
                viewModel.onPhotoSelected("https://picsum.photos/400/300")
            }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    state: ChatUiState,
    onBackClick: () -> Unit,
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
                        onBarClick = { /* scroll to message */ },
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
