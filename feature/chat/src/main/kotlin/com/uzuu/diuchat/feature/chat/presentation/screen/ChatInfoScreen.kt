package com.uzuu.diuchat.feature.chat.presentation.screen

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuConfirmationDialog
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSettingsRow
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.GroupMember
import com.uzuu.diuchat.feature.chat.domain.model.GroupRole
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MoodType

private val CORE_GROUP_MOODS = listOf(
    MoodType.THUONG,
    MoodType.VUI,
    MoodType.BINHYEN,
    MoodType.NHO,
    MoodType.TRAM,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInfoScreen(
    conversation: Conversation,
    onBackClick: () -> Unit,
    onLeaveGroup: () -> Unit,
    onUpdateNickname: (String) -> Unit,
    onToggleMute: (Boolean) -> Unit,
    onManageMemberRole: (String, GroupRole) -> Unit,
    @Suppress("UnusedParameter") onRemoveMember: (String) -> Unit,
    modifier: Modifier = Modifier,
    onVoiceCallClick: () -> Unit = {},
    onVideoCallClick: () -> Unit = {},
    onSearchInChatClick: () -> Unit = {},
    onNotificationSoundClick: () -> Unit = {},
    onOpenSharedMediaClick: () -> Unit = {},
    onOpenPendingLettersClick: () -> Unit = {},
    onOpenPhotoViewer: (caption: String, moodName: String, senderName: String, timestamp: String) -> Unit = { _, _, _, _ -> },
    isGroupEditSheetOpen: Boolean = false,
    onOpenGroupEditSheet: (Boolean) -> Unit = {},
    onUpdateGroupTitleAndMood: (String, MoodType) -> Unit = { _, _ -> },
    onOpenNewGroup: () -> Unit = {},
    messages: List<ChatMessage> = emptyList(),
) {
    var isMuted by remember(conversation.isMuted) { mutableStateOf(conversation.isMuted) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var nicknameInput by remember(conversation.customNickname) { mutableStateOf(conversation.customNickname ?: "") }
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }
    var showLastAdminWarning by remember { mutableStateOf(false) }
    var localGroupEditSheetOpen by remember { mutableStateOf(false) }

    val showGroupEdit = isGroupEditSheetOpen || localGroupEditSheetOpen
    val colors = AppTheme.colors
    val moodColor = Color(conversation.mood.colorHex)
    val displayTitle = conversation.customNickname ?: conversation.title

    // DEC-07 Check: If caller is last admin and members > 1, warn and prevent leave
    val callerIsAdmin = conversation.members.any { (it.isFromMe || it.userId == "me") && it.role == GroupRole.ADMIN }
    val otherAdminsCount = conversation.members.count { (it.isFromMe || it.userId == "me").not() && it.role == GroupRole.ADMIN }
    val isLastAdmin = callerIsAdmin && otherAdminsCount == 0 && conversation.members.size > 1

    val mediaPhotos = remember(messages) {
        messages
            .filter { it.mediaType == MessageMediaType.PHOTO && !it.mediaUri.isNullOrBlank() }
            .takeLast(4)
            .reversed()
    }
    val mediaCount = remember(messages) {
        messages.count { it.mediaType == MessageMediaType.PHOTO || it.mediaType == MessageMediaType.VOICE }
    }
    val pendingLettersCount = remember(messages) {
        messages.count { it.mediaType == MessageMediaType.SEALED_LETTER && !it.isSealedRevealed }
    }
    val connectedDays = remember(messages) {
        val earliestMs = messages.minOfOrNull { it.createdAtEpochMs }?.takeIf { it > 0L }
        if (earliestMs != null) {
            (((System.currentTimeMillis() - earliestMs) / 86_400_000L) + 1L).coerceAtLeast(1L).toInt()
        } else {
            1
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
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

                DiuIconButton(
                    onClick = {
                        if (conversation.isGroup) {
                            localGroupEditSheetOpen = true
                            onOpenGroupEditSheet(true)
                        } else {
                            showNicknameDialog = true
                        }
                    },
                    contentDescription = if (conversation.isGroup) "Chỉnh nhóm" else "Đổi biệt danh",
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Edit),
                        contentDescription = null,
                        tint = colors.ink,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = paddingValues.calculateTopPadding() + 12.dp,
                bottom = paddingValues.calculateBottomPadding() + 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Center hero: DiuOrbAvatar(104.dp, isPulsing = true), DiuSerif 34.sp title, subtitle, 4 square .qa buttons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DiuOrbAvatar(
                        initials = displayTitle.take(1).uppercase().ifEmpty { "D" },
                        moodColor = moodColor,
                        size = 104.dp,
                        avatarUrl = conversation.avatarUrl,
                        isPulsing = true,
                        badgeText = if (conversation.isGroup && conversation.members.isNotEmpty()) "${conversation.members.size}" else null,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = displayTitle,
                        style = AppTheme.displaySerif.copy(fontSize = 34.sp),
                        color = colors.ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (conversation.isGroup) {
                            "${conversation.members.size} thành viên · ${conversation.mood.displayName}"
                        } else {
                            "Đang thấy ${conversation.mood.displayName.lowercase()}"
                        },
                        style = AppTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 square .qa buttons (54.dp, RoundedCornerShape(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        ChatInfoQuickActionSquare(
                            iconRes = AppIcons.Phone,
                            label = "Gọi",
                            onClick = onVoiceCallClick,
                        )
                        ChatInfoQuickActionSquare(
                            iconRes = AppIcons.Video,
                            label = "Video",
                            onClick = onVideoCallClick,
                        )
                        ChatInfoQuickActionSquare(
                            iconRes = AppIcons.Search,
                            label = "Tìm",
                            onClick = onSearchInChatClick,
                        )
                        ChatInfoQuickActionSquare(
                            iconRes = if (isMuted) AppIcons.BellOff else AppIcons.Bell,
                            label = if (isMuted) "Đã tắt" else "Âm báo",
                            onClick = {
                                val nextMuted = !isMuted
                                isMuted = nextMuted
                                onToggleMute(nextMuted)
                                onNotificationSoundClick()
                            },
                        )
                    }
                }
            }

            // 3-column real stats card for 1-1 chat
            if (!conversation.isGroup) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AnniversaryStatCard(
                            value = "$connectedDays",
                            label = "ngày kết nối",
                            modifier = Modifier.weight(1f),
                        )
                        AnniversaryStatCard(
                            value = "${messages.size}",
                            label = "tin nhắn",
                            modifier = Modifier.weight(1f),
                        )
                        AnniversaryStatCard(
                            value = "$mediaCount",
                            label = "ảnh & thoại",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Ảnh & lời nhắn thoại (.media-strip + "Xem hết")
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Ảnh & lời nhắn thoại",
                            style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.inkSecondary,
                        )
                        Text(
                            text = "Xem hết",
                            style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenSharedMediaClick)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }

                    if (mediaPhotos.isEmpty()) {
                        Text(
                            text = "Chưa có ảnh nào được chia sẻ trong cuộc trò chuyện này.",
                            style = AppTheme.typography.bodySmall,
                            color = colors.inkTertiary,
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            mediaPhotos.forEach { photoMsg ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.surface)
                                        .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                                        .clickable {
                                            onOpenPhotoViewer(
                                                photoMsg.caption ?: photoMsg.content,
                                                photoMsg.mood.displayName,
                                                photoMsg.senderName,
                                                "",
                                            )
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    AsyncImage(
                                        model = photoMsg.mediaUri,
                                        contentDescription = photoMsg.caption ?: "Ảnh đã chia sẻ",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Thành viên card for group chat
            if (conversation.isGroup) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Thành viên (${conversation.members.size})",
                            style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.inkSecondary,
                        )
                        DiuCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 6.dp),
                        ) {
                            conversation.members.forEachIndexed { idx, member ->
                                val memberMoodColor = Color(member.mood.colorHex)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        DiuOrbAvatar(
                                            initials = member.displayName.take(1).ifEmpty { "D" },
                                            moodColor = memberMoodColor,
                                            size = 34.dp,
                                        )
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Text(
                                                    text = if (member.isFromMe) "${member.displayName} (Bạn)" else member.displayName,
                                                    style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                    color = colors.ink,
                                                )
                                                if (member.role == GroupRole.ADMIN) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = colors.peach.copy(alpha = 0.4f),
                                                    ) {
                                                        Text(
                                                            text = "quản trị",
                                                            style = AppTheme.typography.labelSmall,
                                                            color = colors.ink,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = member.mood.displayName,
                                                style = AppTheme.typography.bodySmall,
                                                color = colors.inkSecondary,
                                            )
                                        }
                                    }

                                    if (callerIsAdmin && !member.isFromMe) {
                                        val nextRole = if (member.role == GroupRole.ADMIN) GroupRole.MEMBER else GroupRole.ADMIN
                                        TextButton(onClick = { onManageMemberRole(member.userId, nextRole) }) {
                                            Text(
                                                text = if (member.role == GroupRole.ADMIN) "Hạ quyền" else "Thăng Admin",
                                                fontSize = 12.sp,
                                                color = colors.accent,
                                            )
                                        }
                                    }
                                }
                                if (idx < conversation.members.lastIndex) {
                                    HorizontalDivider(color = colors.line)
                                }
                            }

                            HorizontalDivider(color = colors.line)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onOpenNewGroup)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    painter = painterResource(AppIcons.Plus),
                                    contentDescription = "Thêm người",
                                    tint = colors.accent,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "+ Thêm người",
                                    style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = colors.accent,
                                )
                            }
                        }
                    }
                }
            }

            // Tuỳ chỉnh card (Biệt danh, Thư hẹn đang chờ, Mã hoá hai đầu)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tuỳ chỉnh",
                        style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.inkSecondary,
                    )
                    DiuCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        DiuSettingsRow(
                            title = "Biệt danh",
                            subtitle = conversation.customNickname ?: "Đặt biệt danh riêng",
                            onClick = { showNicknameDialog = true },
                        )
                        HorizontalDivider(color = colors.line)
                        DiuSettingsRow(
                            title = "Thư hẹn đang chờ",
                            subtitle = "Lời nhắn niêm phong chờ đến giờ",
                            onClick = onOpenPendingLettersClick,
                            trailing = if (pendingLettersCount > 0) {
                                {
                                    Surface(
                                        shape = CircleShape,
                                        color = colors.peach.copy(alpha = 0.6f),
                                    ) {
                                        Text(
                                            text = "$pendingLettersCount",
                                            style = AppTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.ink,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                        )
                        HorizontalDivider(color = colors.line)
                        DiuSettingsRow(
                            title = "Mã hoá hai đầu",
                            subtitle = "Tin nhắn và cuộc gọi được bảo vệ riêng tư",
                            trailing = {
                                Text(
                                    text = "Đã bật",
                                    style = AppTheme.typography.labelMedium,
                                    color = colors.inkSecondary,
                                )
                            },
                        )
                    }
                }
            }

            // Bottom danger card: "Rời nhóm" (group) or "Tạm ngừng liên lạc" (1-1)
            item {
                DiuCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    DiuSettingsRow(
                        title = if (conversation.isGroup) "Rời nhóm" else "Tạm ngừng liên lạc",
                        destructive = true,
                        onClick = {
                            if (conversation.isGroup) {
                                if (isLastAdmin) {
                                    showLastAdminWarning = true
                                } else {
                                    showLeaveConfirmDialog = true
                                }
                            } else {
                                showLeaveConfirmDialog = true
                            }
                        },
                    )
                }
            }
        }
    }

    // GroupEditSheet (#54)
    if (showGroupEdit) {
        GroupEditSheet(
            initialTitle = conversation.title,
            initialMood = conversation.mood,
            memberCount = conversation.members.size.coerceAtLeast(4),
            onSave = { newTitle, newMood ->
                onUpdateGroupTitleAndMood(newTitle, newMood)
                localGroupEditSheetOpen = false
                onOpenGroupEditSheet(false)
            },
            onDismiss = {
                localGroupEditSheetOpen = false
                onOpenGroupEditSheet(false)
            },
        )
    }

    // Nickname edit dialog
    if (showNicknameDialog) {
        BasicAlertDialog(onDismissRequest = { showNicknameDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.line),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Đặt biệt danh",
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.ink,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nicknameInput,
                        onValueChange = { nicknameInput = it },
                        placeholder = { Text("Nhập biệt danh dịu dàng...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showNicknameDialog = false }) {
                            Text("Hủy", color = colors.inkSecondary)
                        }
                        TextButton(
                            onClick = {
                                onUpdateNickname(nicknameInput.trim())
                                showNicknameDialog = false
                            },
                        ) {
                            Text("Lưu", fontWeight = FontWeight.SemiBold, color = colors.accent)
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for leaving group / pausing contact
    if (showLeaveConfirmDialog) {
        DiuConfirmationDialog(
            title = if (conversation.isGroup) "Rời nhóm" else "Tạm ngừng liên lạc",
            message = if (conversation.isGroup) {
                "Bạn có chắc chắn muốn rời khỏi nhóm này không?"
            } else {
                "Tạm ngừng nhận lời nhắn và cuộc gọi từ $displayTitle?"
            },
            confirmLabel = if (conversation.isGroup) "Rời nhóm" else "Tạm ngừng",
            dismissLabel = "Ở lại",
            destructive = true,
            onConfirm = {
                showLeaveConfirmDialog = false
                if (conversation.isGroup) {
                    onLeaveGroup()
                }
            },
            onDismiss = { showLeaveConfirmDialog = false },
        )
    }

    // DEC-07 Guard: Alert when last admin tries to leave
    if (showLastAdminWarning) {
        BasicAlertDialog(onDismissRequest = { showLastAdminWarning = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.line),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Không thể rời nhóm",
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.danger,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Bạn là Quản trị viên duy nhất còn lại trong nhóm. " +
                            "Vui lòng chuyển quyền Quản trị viên cho một thành viên khác trước khi rời nhóm (DEC-07).",
                        style = AppTheme.typography.bodyMedium,
                        color = colors.inkSecondary,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showLastAdminWarning = false }) {
                            Text("Đã hiểu", fontWeight = FontWeight.SemiBold, color = colors.accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInfoQuickActionSquare(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.line),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    tint = colors.ink,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
        Text(
            text = label,
            style = AppTheme.typography.labelSmall,
            color = colors.inkSecondary,
        )
    }
}

@Composable
private fun AnniversaryStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = AppPalette.Peach.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = AppTheme.headlineSerif.copy(
                    fontFamily = DiuSerif,
                    fontSize = 26.sp,
                ),
                color = colors.ink,
                textAlign = TextAlign.Center,
            )
            Text(
                text = label,
                style = AppTheme.typography.labelSmall,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupEditSheet(
    initialTitle: String,
    initialMood: MoodType,
    memberCount: Int,
    onSave: (String, MoodType) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var selectedMood by remember(initialMood) { mutableStateOf(initialMood) }
    val moodColor = Color(selectedMood.colorHex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = colors.surfaceElevated,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Chỉnh nhóm",
                style = AppTheme.headlineSerif.copy(fontSize = 26.sp),
                color = colors.ink,
            )

            DiuOrbAvatar(
                initials = title.trim().take(1).ifEmpty { "N" },
                moodColor = moodColor,
                size = 84.dp,
                badgeText = "$memberCount",
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = AppTheme.headlineSerif.copy(
                        fontFamily = DiuSerif,
                        fontSize = 22.sp,
                        color = colors.ink,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(colors.ink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
                HorizontalDivider(color = moodColor, thickness = 1.5.dp)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CORE_GROUP_MOODS.forEach { mood ->
                    val dotColor = Color(mood.colorHex)
                    val isSelected = mood == selectedMood
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, colors.ink, CircleShape)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { selectedMood = mood },
                    )
                }
            }

            DiuButton(
                onClick = { onSave(title.trim().ifEmpty { initialTitle }, selectedMood) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Lưu thay đổi")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatInfoScreenPreview() {
    AppTheme {
        ChatInfoScreen(
            conversation = Conversation(
                id = "conv_1",
                title = "Minh",
                mood = MoodType.THUONG,
                lastMessage = "Hẹn gặp tối nay nha!",
                lastMessageTimeEpochMs = 1_700_000_000_000L,
                isGroup = false,
                isMuted = false,
                daysTogether = 712,
                anniversaryDate = "12/10",
                members = listOf(
                    GroupMember(
                        userId = "me",
                        displayName = "Tôi",
                        role = GroupRole.ADMIN,
                        mood = MoodType.BINHYEN,
                        isFromMe = true,
                    ),
                ),
            ),
            onBackClick = {},
            onLeaveGroup = {},
            onUpdateNickname = {},
            onToggleMute = {},
            onManageMemberRole = { _, _ -> },
            onRemoveMember = {},
        )
    }
}
