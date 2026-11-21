package com.uzuu.diuchat.feature.chat.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButtonVariant
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuConfirmationDialog
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSettingsRow
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSwitch
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.GroupRole

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
) {
    var isMuted by remember(conversation.isMuted) { mutableStateOf(conversation.isMuted) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var nicknameInput by remember(conversation.customNickname) { mutableStateOf(conversation.customNickname ?: "") }
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }
    var showLastAdminWarning by remember { mutableStateOf(false) }

    val moodColor = Color(conversation.mood.colorHex)

    // DEC-07 Check: If caller is last admin and members > 1, warn and prevent leave
    val callerIsAdmin = conversation.members.any { (it.isFromMe || it.userId == "me") && it.role == GroupRole.ADMIN }
    val otherAdminsCount = conversation.members.count { (it.isFromMe || it.userId == "me").not() && it.role == GroupRole.ADMIN }
    val isLastAdmin = callerIsAdmin && otherAdminsCount == 0 && conversation.members.size > 1

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (conversation.isGroup) "Thông tin nhóm" else "Thông tin hội thoại",
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppTheme.colors.ink,
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surfaceElevated,
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Avatar, Name, Mood
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(moodColor.copy(alpha = 0.2f))
                            .border(2.dp, moodColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = conversation.title.take(1),
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                            ),
                            color = moodColor,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = conversation.customNickname ?: conversation.title,
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.ink,
                    )
                    Text(
                        text = "Sắc thái: ${conversation.mood.displayName}",
                        style = AppTheme.typography.bodySmall,
                        color = moodColor,
                    )
                }
            }

            // Anniversary section (Ngày bên nhau, Ngày kỷ niệm)
            item {
                DiuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Kỷ niệm",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Kỷ niệm bên nhau",
                                style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = AppTheme.colors.ink,
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${conversation.daysTogether ?: 100} ngày",
                                    style = AppTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.accent,
                                    ),
                                )
                                Text(
                                    text = "Ngày bên nhau",
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.inkSecondary,
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = conversation.anniversaryDate ?: "14/02/2024",
                                    style = AppTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = moodColor,
                                    ),
                                )
                                Text(
                                    text = "Ngày kỷ niệm",
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.inkSecondary,
                                )
                            }
                        }
                    }
                }
            }

            // Settings: Nickname & Mute notifications
            item {
                DiuCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        DiuSettingsRow(
                            title = "Biệt danh",
                            subtitle = conversation.customNickname ?: "Đặt biệt danh riêng",
                            onClick = { showNicknameDialog = true },
                        )
                        DiuSettingsRow(
                            title = "Tắt thông báo",
                            subtitle = if (isMuted) "Đang tắt chuông" else "Đang bật chuông",
                            trailing = {
                                DiuSwitch(
                                    checked = isMuted,
                                    onCheckedChange = { checked ->
                                        isMuted = checked
                                        onToggleMute(checked)
                                    },
                                    contentDescription = "Tắt thông báo",
                                )
                            },
                        )
                    }
                }
            }

            // Group Members management (if isGroup)
            if (conversation.isGroup) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Thành viên nhóm (${conversation.members.size})",
                            style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.colors.inkSecondary,
                        )
                    }
                }

                items(conversation.members) { member ->
                    val memberMoodColor = Color(member.mood.colorHex)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppTheme.colors.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.line),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(memberMoodColor.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = member.displayName.take(1),
                                        style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = memberMoodColor,
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = if (member.isFromMe) "${member.displayName} (Bạn)" else member.displayName,
                                            style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = AppTheme.colors.ink,
                                        )
                                        if (member.role == GroupRole.ADMIN) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AppTheme.colors.accent.copy(alpha = 0.15f),
                                            ) {
                                                Text(
                                                    text = "Quản trị viên",
                                                    style = AppTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                                    color = AppTheme.colors.accent,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = member.mood.displayName,
                                        style = AppTheme.typography.bodySmall,
                                        color = memberMoodColor,
                                    )
                                }
                            }

                            // Role management action if caller is admin and member is not caller
                            if (callerIsAdmin && !member.isFromMe) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val nextRole = if (member.role == GroupRole.ADMIN) GroupRole.MEMBER else GroupRole.ADMIN
                                    TextButton(onClick = { onManageMemberRole(member.userId, nextRole) }) {
                                        Text(
                                            text = if (member.role == GroupRole.ADMIN) "Hạ quyền" else "Thăng Admin",
                                            fontSize = 12.sp,
                                            color = AppTheme.colors.accent,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Leave group button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    DiuButton(
                        onClick = {
                            if (isLastAdmin) {
                                showLastAdminWarning = true
                            } else {
                                showLeaveConfirmDialog = true
                            }
                        },
                        variant = DiuButtonVariant.Danger,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Rời nhóm")
                    }
                }
            }
        }
    }

    // Nickname edit dialog
    if (showNicknameDialog) {
        BasicAlertDialog(onDismissRequest = { showNicknameDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.surfaceElevated,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Đặt biệt danh",
                        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppTheme.colors.ink,
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
                            Text("Hủy", color = AppTheme.colors.inkSecondary)
                        }
                        TextButton(
                            onClick = {
                                onUpdateNickname(nicknameInput.trim())
                                showNicknameDialog = false
                            },
                        ) {
                            Text("Lưu", fontWeight = FontWeight.SemiBold, color = AppTheme.colors.accent)
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for leaving group
    if (showLeaveConfirmDialog) {
        DiuConfirmationDialog(
            title = "Rời nhóm",
            message = "Bạn có chắc chắn muốn rời khỏi nhóm này không?",
            confirmLabel = "Rời nhóm",
            dismissLabel = "Ở lại",
            destructive = true,
            onConfirm = {
                showLeaveConfirmDialog = false
                onLeaveGroup()
            },
            onDismiss = { showLeaveConfirmDialog = false },
        )
    }

    // DEC-07 Guard: Alert when last admin tries to leave
    if (showLastAdminWarning) {
        BasicAlertDialog(onDismissRequest = { showLastAdminWarning = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.surfaceElevated,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Bảo vệ quản trị",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = "Không thể rời nhóm",
                            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.ink,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Bạn là Quản trị viên duy nhất còn lại trong nhóm. " +
                            "Để đảm bảo nhóm có người quản lý, vui lòng chuyển quyền Quản trị viên " +
                            "cho một thành viên khác trước khi rời nhóm (DEC-07).",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.inkSecondary,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showLastAdminWarning = false }) {
                            Text("Đã hiểu", fontWeight = FontWeight.SemiBold, color = AppTheme.colors.accent)
                        }
                    }
                }
            }
        }
    }
}
