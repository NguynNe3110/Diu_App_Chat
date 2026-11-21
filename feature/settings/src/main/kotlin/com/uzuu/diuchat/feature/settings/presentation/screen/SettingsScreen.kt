@file:Suppress("ktlint:compose:vm-forwarding-check")

package com.uzuu.diuchat.feature.settings.presentation.screen

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButtonVariant
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSettingsRow
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSwitch
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import com.uzuu.diuchat.feature.settings.presentation.composable.DeleteAccountDialog
import com.uzuu.diuchat.feature.settings.presentation.composable.EditProfileSheet
import org.koin.androidx.compose.koinViewModel

@Suppress("compose:vm-forwarding-check")
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppTheme.colors.background,
    ) {
        when (uiState.subScreen) {
            SettingsSubScreen.OVERVIEW -> {
                SettingsOverviewContent(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onShowEditProfileSheet = viewModel::showEditProfile,
                    onToggleQuietHours = viewModel::toggleQuietHours,
                    onShowQuietHoursDialog = viewModel::showQuietHoursDialog,
                    onShowPrivacyDialog = viewModel::showPrivacyDialog,
                    onToggleReadReceipts = viewModel::toggleReadReceipts,
                    onNavigateToBlockedContacts = viewModel::navigateToBlockedContacts,
                    onShowLogoutConfirm = viewModel::showLogoutConfirm,
                    onShowDeleteAccount = viewModel::showDeleteAccount,
                    onUpdateQuietTimeRange = viewModel::updateQuietTimeRange,
                    onSetNotificationPrivacy = viewModel::setNotificationPrivacy,
                    onUpdateProfile = viewModel::updateProfile,
                    onLogout = viewModel::logout,
                    onDeleteAccount = viewModel::deleteAccount,
                )
            }

            SettingsSubScreen.BLOCKED_CONTACTS -> {
                BlockedContactsContent(
                    uiState = uiState,
                    onBackClick = { viewModel.navigateBackToOverview() },
                    onUnblock = { viewModel.unblockContact(it) },
                )
            }
        }
    }
}

@Composable
private fun SettingsOverviewContent(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onShowEditProfileSheet: (Boolean) -> Unit = {},
    onToggleQuietHours: (Boolean) -> Unit = {},
    onShowQuietHoursDialog: (Boolean) -> Unit = {},
    onShowPrivacyDialog: (Boolean) -> Unit = {},
    onToggleReadReceipts: (Boolean) -> Unit = {},
    onNavigateToBlockedContacts: () -> Unit = {},
    onShowLogoutConfirm: (Boolean) -> Unit = {},
    onShowDeleteAccount: (Boolean) -> Unit = {},
    onUpdateQuietTimeRange: (Int, Int) -> Unit = { _, _ -> },
    onSetNotificationPrivacy: (NotificationPrivacy) -> Unit = {},
    onUpdateProfile: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onLogout: () -> Unit = {},
    onDeleteAccount: (String) -> Unit = {},
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiuTopBar(
            title = "Cài đặt & Riêng tư",
            subtitle = "Không gian bình yên của bạn",
            navigationIcon = if (onBackClick != {}) {
                {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colors.ink,
                        )
                    }
                }
            } else {
                null
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            // ── Hồ sơ cá nhân (Account & Profile) ──
            item {
                SectionHeader(title = "HỒ SƠ CÁ NHÂN")
            }
            item {
                ProfileHeaderCard(
                    profile = uiState.userProfile,
                    onEditClick = { onShowEditProfileSheet(true) },
                )
            }

            // ── Khung giờ yên lặng ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "GIỜ YÊN LẶNG (QUIET HOURS)")
            }
            item {
                DiuSettingsRow(
                    title = "Bật chế độ yên lặng",
                    subtitle = uiState.quietSchedule.timeRangeFormatted,
                    trailing = {
                        DiuSwitch(
                            checked = uiState.quietSchedule.enabled,
                            onCheckedChange = onToggleQuietHours,
                            contentDescription = "Bật tắt giờ yên lặng",
                        )
                    },
                )
            }
            item {
                DiuSettingsRow(
                    title = "Khung giờ nghỉ ngơi",
                    subtitle = "${uiState.quietSchedule.startTimeFormatted} đến ${uiState.quietSchedule.endTimeFormatted} (Hỗ trợ qua đêm)",
                    onClick = { onShowQuietHoursDialog(true) },
                )
            }
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    DiuCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "🌙 Trong giờ yên lặng, các tin nhắn gửi đến sẽ không phát chuông hoặc rung mạnh. " +
                                "Người thân trong chòm sao sẽ nhìn thấy biểu tượng trăng khuyết bên cạnh tên bạn.",
                            color = colors.inkSecondary,
                            style = AppTheme.typography.bodySmall,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }

            // ── Thông báo & Riêng tư (REQ-37) ──
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "THÔNG BÁO & RIÊNG TƯ (REQ-37)")
            }
            item {
                DiuSettingsRow(
                    title = "Mức hiển thị thông báo",
                    subtitle = uiState.notificationPrivacy.label,
                    onClick = { onShowPrivacyDialog(true) },
                )
            }
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(
                        text = uiState.notificationPrivacy.description,
                        color = colors.inkSecondary,
                        style = AppTheme.typography.bodySmall,
                        lineHeight = 18.sp,
                    )
                }
            }

            // ── Bảo mật & Chặn (REQ-40) ──
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "BẢO MẬT & QUYỀN RIÊNG TƯ (REQ-40)")
            }
            item {
                DiuSettingsRow(
                    title = "Danh sách người bị chặn",
                    subtitle = if (uiState.blockedContacts.isEmpty()) "Chưa chặn ai" else "${uiState.blockedContacts.size} người bị chặn",
                    onClick = onNavigateToBlockedContacts,
                )
            }
            item {
                DiuSettingsRow(
                    title = "Xác nhận đã đọc (Read receipts)",
                    subtitle = "Hiển thị trạng thái đã xem cho người gửi",
                    trailing = {
                        DiuSwitch(
                            checked = uiState.readReceiptsEnabled,
                            onCheckedChange = onToggleReadReceipts,
                            contentDescription = "Bật tắt xác nhận đã đọc",
                        )
                    },
                )
            }

            // ── Tài khoản & Hành động nguy hiểm ──
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "TÀI KHOẢN")
            }
            item {
                DiuSettingsRow(
                    title = "Đăng xuất tài khoản",
                    subtitle = "Xoá phiên đăng nhập hiện tại trên thiết bị",
                    onClick = { onShowLogoutConfirm(true) },
                )
            }
            item {
                DiuSettingsRow(
                    title = "Xoá tài khoản vĩnh viễn",
                    subtitle = "Xoá toàn bộ hồ sơ và dữ liệu cá nhân",
                    destructive = true,
                    onClick = { onShowDeleteAccount(true) },
                )
            }

            // ── Thông tin ứng dụng ──
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "HỆ THỐNG")
            }
            item {
                DiuSettingsRow(
                    title = "Phiên bản ứng dụng",
                    subtitle = "Dịu App Chat v0.0.1 (Release Hardening Build)",
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Sheet chỉnh sửa hồ sơ
    if (uiState.showEditProfileSheet) {
        EditProfileSheet(
            currentProfile = uiState.userProfile,
            onSave = { name, bio, bday, mood ->
                onUpdateProfile(name, bio, bday, mood)
            },
            onDismiss = { onShowEditProfileSheet(false) },
        )
    }

    // Dialog xác nhận đăng xuất
    if (uiState.showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onShowLogoutConfirm(false) },
            title = {
                Text(
                    text = "Đăng xuất?",
                    style = AppTheme.typography.titleMedium,
                    color = colors.ink,
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất? Dữ liệu lưu cục bộ sẽ được bảo vệ an toàn.",
                    style = AppTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = onLogout) {
                    Text("Đăng xuất", color = colors.danger, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowLogoutConfirm(false) }) {
                    Text("Hủy", color = colors.inkSecondary)
                }
            },
        )
    }

    // Dialog xác nhận xoá tài khoản
    if (uiState.showDeleteAccountDialog) {
        DeleteAccountDialog(
            userPhone = uiState.userProfile.phone,
            onConfirmDelete = onDeleteAccount,
            onDismiss = { onShowDeleteAccount(false) },
        )
    }

    // Dialog chọn mức hiển thị thông báo (REQ-37)
    if (uiState.showPrivacyDialog) {
        NotificationPrivacyDialog(
            currentPrivacy = uiState.notificationPrivacy,
            onSelect = {
                onSetNotificationPrivacy(it)
            },
            onDismiss = { onShowPrivacyDialog(false) },
        )
    }

    // Dialog chọn khung giờ yên lặng
    if (uiState.showQuietHoursDialog) {
        QuietHoursPresetDialog(
            currentSchedule = uiState.quietSchedule,
            onSelectRange = { start, end ->
                onUpdateQuietTimeRange(start, end)
            },
            onDismiss = { onShowQuietHoursDialog(false) },
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    profile: com.uzuu.diuchat.feature.settings.domain.model.UserProfile,
    onEditClick: () -> Unit,
) {
    val colors = AppTheme.colors

    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        DiuCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = profile.displayName,
                            style = AppTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink,
                        )
                        Text(
                            text = "• ${profile.primaryMood}",
                            style = AppTheme.typography.labelSmall,
                            color = colors.accent,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Text(
                        text = profile.bio,
                        style = AppTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                        lineHeight = 16.sp,
                        maxLines = 2,
                    )
                    Text(
                        text = "${profile.phone} • ${profile.birthday}",
                        style = AppTheme.typography.labelSmall,
                        color = colors.inkSecondary.copy(alpha = 0.7f),
                    )
                }

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa hồ sơ",
                        tint = colors.accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedContactsContent(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onUnblock: (String) -> Unit,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiuTopBar(
            title = "Danh sách người bị chặn",
            subtitle = "${uiState.blockedContacts.size} liên hệ",
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = colors.ink,
                    )
                }
            },
        )

        if (uiState.blockedContacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                DiuEmptyState(
                    title = "Chưa chặn ai",
                    description = "Danh sách chặn của bạn đang trống. Bạn chưa chặn bất kỳ liên hệ nào.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        text = "Những người trong danh sách này sẽ không thể gửi tin nhắn, " +
                            "gọi điện hay nhìn thấy trạng thái cảm xúc của bạn.",
                        color = colors.inkSecondary,
                        style = AppTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        lineHeight = 18.sp,
                    )
                }
                items(uiState.blockedContacts, key = { it.id }) { blocked ->
                    BlockedContactRow(
                        contact = blocked,
                        onUnblock = { onUnblock(blocked.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedContactRow(
    contact: BlockedContact,
    onUnblock: () -> Unit,
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = null,
                tint = colors.danger,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                color = colors.ink,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${contact.phone} • Đã chặn ${contact.blockedAt}",
                color = colors.inkSecondary,
                style = AppTheme.typography.bodySmall,
            )
        }

        DiuButton(
            onClick = onUnblock,
            variant = DiuButtonVariant.Outline,
            shape = RoundedCornerShape(20.dp),
        ) {
            Text(
                text = "Bỏ chặn",
                style = AppTheme.typography.labelSmall,
                color = colors.ink,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = AppTheme.colors.inkSecondary,
        style = AppTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun NotificationPrivacyDialog(
    currentPrivacy: NotificationPrivacy,
    onSelect: (NotificationPrivacy) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Mức hiển thị thông báo",
                style = AppTheme.typography.titleMedium,
                color = colors.ink,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotificationPrivacy.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(option) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option == currentPrivacy,
                            onClick = { onSelect(option) },
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = option.label,
                                style = AppTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = colors.ink,
                            )
                            Text(
                                text = option.description,
                                style = AppTheme.typography.bodySmall,
                                color = colors.inkSecondary,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = colors.accent)
            }
        },
    )
}

@Composable
private fun QuietHoursPresetDialog(
    currentSchedule: com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule,
    onSelectRange: (startMinute: Int, endMinute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors

    val presets = listOf(
        Triple("22:30 – 06:00 (Mặc định)", 22 * 60 + 30, 6 * 60),
        Triple("23:00 – 07:00 (Thức khuya dậy muộn)", 23 * 60, 7 * 60),
        Triple("22:00 – 05:30 (Dậy sớm bình minh)", 22 * 60, 5 * 60 + 30),
        Triple("00:00 – 08:00 (Nửa đêm tới sáng)", 0, 8 * 60),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn khung giờ nghỉ ngơi",
                style = AppTheme.typography.titleMedium,
                color = colors.ink,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { (label, start, end) ->
                    val isSelected = currentSchedule.startMinuteOfDay == start && currentSchedule.endMinuteOfDay == end
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectRange(start, end) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = label,
                            style = AppTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colors.accent else colors.ink,
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.accent,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = colors.accent)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsOverviewPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.background,
        ) {
            SettingsOverviewContent(
                uiState = SettingsUiState(
                    userProfile = UserProfile(
                        id = "u1",
                        displayName = "Minh Anh",
                        username = "minhanh",
                        bio = "Nhẹ nhàng lắng nghe từng ngày trôi qua",
                        birthday = "15/08/2000",
                        phone = "+84901234567",
                        primaryMood = "Bình yên",
                        avatarUrl = null,
                    ),
                    quietSchedule = QuietSchedule(
                        enabled = true,
                        startMinuteOfDay = 1350,
                        endMinuteOfDay = 360,
                    ),
                    notificationPrivacy = NotificationPrivacy.FULL_CONTENT,
                    readReceiptsEnabled = true,
                ),
                onBackClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsBlockedContactsPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.background,
        ) {
            BlockedContactsContent(
                uiState = SettingsUiState(
                    blockedContacts = listOf(
                        BlockedContact("b1", "Người lạ", "+84900000001", "10/11/2026"),
                        BlockedContact("b2", "Spam cuộc gọi", "+84900000002", "12/11/2026"),
                    ),
                ),
                onBackClick = {},
                onUnblock = {},
            )
        }
    }
}
