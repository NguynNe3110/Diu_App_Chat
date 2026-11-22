@file:Suppress("ktlint:compose:vm-forwarding-check")

package com.uzuu.diuchat.feature.settings.presentation.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMediaSourceSheet
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSectionHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSwitch
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.base.presentation.util.rememberMediaPickerController
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import com.uzuu.diuchat.feature.settings.presentation.composable.DeleteAccountDialog
import com.uzuu.diuchat.feature.settings.presentation.composable.EditProfileSheet
import com.uzuu.diuchat.feature.settings.presentation.composable.QuietHoursDial
import org.koin.androidx.compose.koinViewModel

private data class ProfileMoodOption(
    val id: String,
    val label: String,
    val color: Color,
)

private val CORE_FIVE_MOODS = listOf(
    ProfileMoodOption("thuong", "Thương", AppPalette.MoodThuong),
    ProfileMoodOption("vui", "Vui", AppPalette.MoodVui),
    ProfileMoodOption("binhyen", "Bình yên", AppPalette.MoodBinhYen),
    ProfileMoodOption("nho", "Nhớ", AppPalette.MoodNho),
    ProfileMoodOption("tram", "Trầm", AppPalette.MoodTram),
)

private fun resolveMoodColor(moodName: String): Color = CORE_FIVE_MOODS.firstOrNull {
    it.label.equals(moodName.trim(), ignoreCase = true) ||
        it.id.equals(moodName.trim(), ignoreCase = true)
}?.color ?: AppPalette.MoodVui

@Suppress("compose:vm-forwarding-check", "LongParameterList", "UnusedParameter")
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNavigateToStorage: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToLetters: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToFavourites: () -> Unit = {},
    onNavigateToOnBoarding: () -> Unit = {},
    onReplayOnboarding: () -> Unit = onNavigateToOnBoarding,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    BackHandler(enabled = uiState.subScreen != SettingsSubScreen.PROFILE_OVERVIEW) {
        if (uiState.subScreen == SettingsSubScreen.BLOCKED_CONTACTS) {
            viewModel.navigateToSubScreen(SettingsSubScreen.GENERAL_SETTINGS)
        } else {
            viewModel.navigateBackToOverview()
        }
    }

    // Transparent root container so MainShowcaseScreen's DiuAmbientBackground shines through
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
    ) {
        when (uiState.subScreen) {
            SettingsSubScreen.PROFILE_OVERVIEW -> {
                ProfileOverviewContent(
                    uiState = uiState,
                    onOpenEditProfile = { viewModel.navigateToSubScreen(SettingsSubScreen.EDIT_PROFILE) },
                    onSelectMood = viewModel::updatePrimaryMoodQuickly,
                    onToggleQuietHours = viewModel::toggleQuietHours,
                    onOpenQuietHours = { viewModel.navigateToSubScreen(SettingsSubScreen.QUIET_HOURS) },
                    onToggleDarkMode = { enabled ->
                        viewModel.setThemeMode(if (enabled) "DARK" else "LIGHT")
                    },
                    onToggleSoftSendAnimation = viewModel::toggleSoftSendAnimation,
                    onNavigateToJournal = onNavigateToJournal,
                    onOpenNotifications = { viewModel.navigateToSubScreen(SettingsSubScreen.NOTIFICATIONS) },
                    onToggleReadReceipts = viewModel::toggleReadReceipts,
                    onNavigateToLetters = onNavigateToLetters,
                    onNavigateToContacts = onNavigateToContacts,
                    onNavigateToFavourites = onNavigateToFavourites,
                    onOpenGeneralSettings = { viewModel.navigateToSubScreen(SettingsSubScreen.GENERAL_SETTINGS) },
                    onReplayOnboarding = onReplayOnboarding,
                )
            }

            SettingsSubScreen.EDIT_PROFILE -> {
                EditProfileSubScreen(
                    profile = uiState.userProfile,
                    onBackClick = viewModel::navigateBackToOverview,
                    onSave = { name, bio, birthday, mood, avatarUrl ->
                        viewModel.updateProfile(
                            displayName = name,
                            bio = bio,
                            birthday = birthday,
                            primaryMood = mood,
                            avatarUrl = avatarUrl,
                        )
                    },
                )
            }

            SettingsSubScreen.QUIET_HOURS -> {
                QuietHoursSubScreen(
                    schedule = uiState.quietSchedule,
                    onBackClick = viewModel::navigateBackToOverview,
                    onToggleQuietHours = viewModel::toggleQuietHours,
                    onStepStart = viewModel::stepQuietStartTime,
                    onStepEnd = viewModel::stepQuietEndTime,
                    onToggleRepeatDay = viewModel::toggleQuietRepeatDay,
                    onToggleEmergencyContact = viewModel::toggleEmergencyContact,
                )
            }

            SettingsSubScreen.NOTIFICATIONS -> {
                NotificationsSubScreen(
                    privacy = uiState.notificationPrivacy,
                    sound = uiState.notificationSound,
                    vibrateOnce = uiState.vibrateOnceEnabled,
                    eveningDigest = uiState.eveningGroupDigestEnabled,
                    onBackClick = viewModel::navigateBackToOverview,
                    onSelectPrivacy = viewModel::setNotificationPrivacy,
                    onSelectSound = viewModel::setNotificationSound,
                    onToggleVibrateOnce = viewModel::toggleVibrateOnce,
                    onToggleEveningDigest = viewModel::toggleEveningGroupDigest,
                )
            }

            SettingsSubScreen.GENERAL_SETTINGS -> {
                GeneralSettingsSubScreen(
                    uiState = uiState,
                    onBackClick = viewModel::navigateBackToOverview,
                    onOpenEditProfile = { viewModel.navigateToSubScreen(SettingsSubScreen.EDIT_PROFILE) },
                    onOpenBlockedContacts = { viewModel.navigateToSubScreen(SettingsSubScreen.BLOCKED_CONTACTS) },
                    onOpenNotifications = { viewModel.navigateToSubScreen(SettingsSubScreen.NOTIFICATIONS) },
                    onOpenQuietHours = { viewModel.navigateToSubScreen(SettingsSubScreen.QUIET_HOURS) },
                    onNavigateToStorage = onNavigateToStorage,
                    onToggleDarkMode = { enabled ->
                        viewModel.setThemeMode(if (enabled) "DARK" else "LIGHT")
                    },
                    onShowLogoutConfirm = { viewModel.showLogoutConfirm(true) },
                    onShowDeleteAccount = { viewModel.showDeleteAccount(true) },
                )
            }

            SettingsSubScreen.BLOCKED_CONTACTS -> {
                BlockedContactsContent(
                    uiState = uiState,
                    onBackClick = { viewModel.navigateToSubScreen(SettingsSubScreen.GENERAL_SETTINGS) },
                    onUnblock = viewModel::unblockContact,
                )
            }
        }

        if (uiState.showEditProfileSheet) {
            EditProfileSheet(
                currentProfile = uiState.userProfile,
                onSave = { name, bio, bday, mood, avatarUrl ->
                    viewModel.updateProfile(name, bio, bday, mood, avatarUrl)
                },
                onDismiss = { viewModel.showEditProfile(false) },
            )
        }

        if (uiState.showLogoutConfirmDialog) {
            val colors = AppTheme.colors
            AlertDialog(
                onDismissRequest = { viewModel.showLogoutConfirm(false) },
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
                    TextButton(onClick = viewModel::logout) {
                        Text("Đăng xuất", color = colors.danger, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showLogoutConfirm(false) }) {
                        Text("Huỷ", color = colors.inkSecondary)
                    }
                },
            )
        }

        if (uiState.showDeleteAccountDialog) {
            DeleteAccountDialog(
                userPhone = uiState.userProfile.phone,
                onConfirmDelete = viewModel::deleteAccount,
                onDismiss = { viewModel.showDeleteAccount(false) },
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// #34 — PROFILE_OVERVIEW (Tab "Tôi" — No Back Button)
// ═══════════════════════════════════════════════════════════════════════

@Suppress("LongParameterList")
@Composable
private fun ProfileOverviewContent(
    uiState: SettingsUiState,
    onOpenEditProfile: () -> Unit,
    onSelectMood: (String) -> Unit,
    onToggleQuietHours: (Boolean) -> Unit,
    onOpenQuietHours: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleSoftSendAnimation: (Boolean) -> Unit,
    onNavigateToJournal: () -> Unit,
    onOpenNotifications: () -> Unit,
    onToggleReadReceipts: (Boolean) -> Unit,
    onNavigateToLetters: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToFavourites: () -> Unit,
    onOpenGeneralSettings: () -> Unit,
    onReplayOnboarding: () -> Unit,
) {
    val colors = AppTheme.colors
    val profile = uiState.userProfile
    val activeMoodColor = resolveMoodColor(profile.primaryMood)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 110.dp),
    ) {
        // ── .prof-top ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DiuOrbAvatar(
                    initials = profile.displayName.ifEmpty { "L" }.take(1).uppercase(),
                    moodColor = activeMoodColor,
                    size = 112.dp,
                    avatarUrl = profile.avatarUrl,
                    isPulsing = true,
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = profile.displayName.ifEmpty { "Lan" },
                        fontFamily = DiuSerif,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.ink,
                    )
                    IconButton(
                        onClick = onOpenEditProfile,
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Edit),
                            contentDescription = "Sửa hồ sơ",
                            tint = colors.inkSecondary,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }

                Text(
                    text = buildAnnotatedString {
                        append("Đang thấy ")
                        withStyle(SpanStyle(color = colors.ink, fontWeight = FontWeight.Medium)) {
                            append(profile.primaryMood.ifEmpty { "vui" }.lowercase())
                        }
                    },
                    fontSize = 14.sp,
                    color = colors.inkSecondary,
                )

                Spacer(modifier = Modifier.height(16.dp))

                MoodDotSelectorRow(
                    selectedMood = profile.primaryMood,
                    onSelectMood = onSelectMood,
                )
            }
        }

        // ── .quiet-card ──
        item {
            QuietOverviewCard(
                schedule = uiState.quietSchedule,
                onToggle = onToggleQuietHours,
                onOpenQuietHours = onOpenQuietHours,
            )
        }

        // ── GIAO DIỆN ──
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DiuSectionHeader(title = "GIAO DIỆN")
        }
        item {
            val isDark = uiState.themeMode == "DARK"
            SettingsGlassGroupCard(
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                PrototypeSettingRow(
                    iconRes = if (isDark) AppIcons.Moon else AppIcons.Sun,
                    title = "Chế độ tối",
                    trailing = {
                        DiuSwitch(
                            checked = isDark,
                            onCheckedChange = onToggleDarkMode,
                            contentDescription = "Chế độ tối",
                        )
                    },
                )
                HorizontalDivider(thickness = 1.dp, color = colors.line)
                PrototypeSettingRow(
                    iconRes = AppIcons.Chat,
                    title = "Hoạt ảnh gửi tin mềm",
                    trailing = {
                        DiuSwitch(
                            checked = uiState.softSendAnimationEnabled,
                            onCheckedChange = onToggleSoftSendAnimation,
                            contentDescription = "Hoạt ảnh gửi tin mềm",
                        )
                    },
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SettingsGlassGroupCard(
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                PrototypeSettingRow(
                    iconRes = AppIcons.Calendar,
                    title = "Nhật ký màu",
                    onClick = onNavigateToJournal,
                    showChevron = true,
                )
                HorizontalDivider(thickness = 1.dp, color = colors.line)
                PrototypeSettingRow(
                    iconRes = AppIcons.Bell,
                    title = "Thông báo",
                    onClick = onOpenNotifications,
                    showChevron = true,
                )
            }
        }

        // ── RIÊNG TƯ ──
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DiuSectionHeader(title = "RIÊNG TƯ")
        }
        item {
            SettingsGlassGroupCard(
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                PrototypeSettingRow(
                    iconRes = AppIcons.Check,
                    title = "Cho biết đã xem",
                    trailing = {
                        DiuSwitch(
                            checked = uiState.readReceiptsEnabled,
                            onCheckedChange = onToggleReadReceipts,
                            contentDescription = "Cho biết đã xem",
                        )
                    },
                )
                HorizontalDivider(thickness = 1.dp, color = colors.line)
                PrototypeSettingRow(
                    iconRes = AppIcons.Envelope,
                    title = "Thư hẹn đang chờ",
                    badgeText = "1",
                    onClick = onNavigateToLetters,
                    showChevron = true,
                )
                HorizontalDivider(thickness = 1.dp, color = colors.line)
                PrototypeSettingRow(
                    iconRes = AppIcons.People,
                    title = "Vòng thân của tôi",
                    badgeText = "7",
                    onClick = onNavigateToContacts,
                    showChevron = true,
                )
                HorizontalDivider(thickness = 1.dp, color = colors.line)
                PrototypeSettingRow(
                    iconRes = AppIcons.Heart,
                    title = "Kỷ niệm đã lưu",
                    onClick = onNavigateToFavourites,
                    showChevron = true,
                )
            }
        }

        // ── Bottom links ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Cài đặt",
                    color = colors.inkSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenGeneralSettings)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
                Text(
                    text = "Xem lại phần giới thiệu",
                    color = colors.inkSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onReplayOnboarding)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun MoodDotSelectorRow(
    selectedMood: String,
    onSelectMood: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CORE_FIVE_MOODS.forEach { mood ->
            val isSelected = mood.label.equals(selectedMood.trim(), ignoreCase = true) ||
                mood.id.equals(selectedMood.trim(), ignoreCase = true)
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.18f else 1f,
                label = "Mood dot scale",
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .scale(scale)
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(8.dp, CircleShape, ambientColor = mood.color, spotColor = mood.color)
                                .border(2.5.dp, colors.background, CircleShape)
                                .border(1.5.dp, mood.color, CircleShape)
                        } else {
                            Modifier
                        },
                    )
                    .clip(CircleShape)
                    .background(mood.color)
                    .clickable { onSelectMood(mood.label) },
            )
        }
    }
}

@Composable
private fun QuietOverviewCard(
    schedule: QuietSchedule,
    onToggle: (Boolean) -> Unit,
    onOpenQuietHours: () -> Unit,
) {
    val colors = AppTheme.colors
    val cardBg by animateColorAsState(
        targetValue = if (schedule.enabled) {
            AppPalette.QuietPurple.copy(alpha = 0.26f)
        } else {
            colors.surface
        },
        label = "Quiet card background",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppPalette.QuietPurple.copy(alpha = 0.40f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Moon),
                        contentDescription = null,
                        tint = colors.ink,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Giờ yên lặng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.ink,
                    )
                    Text(
                        text = if (schedule.enabled) {
                            "Đang nghỉ ngơi · đến ${schedule.endTimeFormatted}"
                        } else {
                            "Tự động ${schedule.startTimeFormatted} – ${schedule.endTimeFormatted}"
                        },
                        fontSize = 13.sp,
                        color = colors.inkSecondary,
                    )
                }

                DiuSwitch(
                    checked = schedule.enabled,
                    onCheckedChange = onToggle,
                    contentDescription = "Giờ yên lặng",
                )
            }

            Text(
                text = "Chỉnh lịch & ngoại lệ →",
                fontSize = 14.sp,
                color = colors.inkSecondary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenQuietHours)
                    .padding(vertical = 4.dp),
            )

            Text(
                text = "Người thân sẽ thấy bạn đang nghỉ. Tin nhắn vẫn đến, nhưng lặng lẽ — không rung, không đèn.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = colors.inkSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// #35 — EDIT_PROFILE (Màn "Sửa hồ sơ" toàn màn hình)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun EditProfileSubScreen(
    profile: UserProfile,
    onBackClick: () -> Unit,
    onSave: (displayName: String, bio: String, birthday: String, primaryMood: String, avatarUrl: String?) -> Unit,
) {
    val colors = AppTheme.colors
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var bio by remember(profile.bio) { mutableStateOf(profile.bio) }
    var birthday by remember(profile.birthday) { mutableStateOf(profile.birthday) }
    var primaryMood by remember(profile.primaryMood) { mutableStateOf(profile.primaryMood) }
    var avatarUrl by remember(profile.avatarUrl) { mutableStateOf(profile.avatarUrl) }
    var showMediaSourceSheet by remember { mutableStateOf(false) }

    val mediaPickerController = rememberMediaPickerController(
        onSelectImage = { uri: Uri ->
            avatarUrl = uri.toString()
        },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiuTopBar(
            title = "Sửa hồ sơ",
            navigationIcon = {
                PrototypeBackButton(onClick = onBackClick)
            },
            actions = {
                Text(
                    text = "Lưu",
                    color = colors.ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = displayName.isNotBlank()) {
                            onSave(displayName.trim(), bio.trim(), birthday.trim(), primaryMood, avatarUrl)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        DiuOrbAvatar(
                            initials = displayName.ifEmpty { "?" }.take(1).uppercase(),
                            moodColor = resolveMoodColor(primaryMood),
                            size = 112.dp,
                            avatarUrl = avatarUrl,
                            isPulsing = true,
                        )
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { showMediaSourceSheet = true },
                            shape = CircleShape,
                            color = colors.ink,
                            border = BorderStroke(3.dp, colors.background),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(AppIcons.Camera),
                                    contentDescription = "Đổi ảnh đại diện",
                                    tint = colors.background,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MoodDotSelectorRow(
                        selectedMood = primaryMood,
                        onSelectMood = { primaryMood = it },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Màu quả cầu của bạn",
                        fontSize = 14.sp,
                        color = colors.inkSecondary,
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProfileInputCard(
                        label = "Tên",
                        value = displayName,
                        onValueChange = { displayName = it },
                    )
                    ProfileInputCard(
                        label = "Đôi dòng về bạn",
                        value = bio,
                        onValueChange = { bio = it },
                        singleLine = false,
                    )
                    ProfileInputCard(
                        label = "Sinh nhật",
                        value = birthday,
                        onValueChange = { birthday = it },
                    )
                    ProfileInputCard(
                        label = "Số điện thoại",
                        value = profile.phone,
                        onValueChange = {},
                        readOnly = true,
                        trailingIconRes = AppIcons.Lock,
                    )
                }
            }
        }
    }

    if (showMediaSourceSheet) {
        DiuMediaSourceSheet(
            title = "Đổi ảnh đại diện",
            onPickFromGallery = mediaPickerController::launchPhotoPicker,
            onTakePhoto = mediaPickerController::launchCamera,
            onDismiss = { showMediaSourceSheet = false },
        )
    }
}

@Composable
private fun ProfileInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    @DrawableRes trailingIconRes: Int? = null,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = colors.inkSecondary,
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    readOnly = readOnly,
                    singleLine = singleLine,
                    textStyle = AppTheme.typography.bodyLarge.copy(
                        color = colors.ink,
                        fontSize = 16.sp,
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (trailingIconRes != null) {
                Icon(
                    painter = painterResource(trailingIconRes),
                    contentDescription = null,
                    tint = colors.inkTertiary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// #37 — QUIET_HOURS (Màn "Giờ yên lặng" với QuietHoursDial)
// ═══════════════════════════════════════════════════════════════════════

private data class EmergencyContactItem(
    val id: String,
    val name: String,
    val moodColor: Color,
)

private val EMERGENCY_CONTACTS = listOf(
    EmergencyContactItem("minh", "Minh", AppPalette.MoodThuong),
    EmergencyContactItem("me", "Mẹ", AppPalette.MoodVui),
    EmergencyContactItem("thao", "Thảo", AppPalette.MoodNho),
)

private val WEEKDAY_ITEMS = listOf(
    1 to "T2",
    2 to "T3",
    3 to "T4",
    4 to "T5",
    5 to "T6",
    6 to "T7",
    7 to "CN",
)

@Suppress("LongParameterList")
@Composable
private fun QuietHoursSubScreen(
    schedule: QuietSchedule,
    onBackClick: () -> Unit,
    onToggleQuietHours: (Boolean) -> Unit,
    onStepStart: (Int) -> Unit,
    onStepEnd: (Int) -> Unit,
    onToggleRepeatDay: (Int) -> Unit,
    onToggleEmergencyContact: (String) -> Unit,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiuTopBar(
            title = "Giờ yên lặng",
            navigationIcon = {
                PrototypeBackButton(onClick = onBackClick)
            },
            actions = {
                DiuSwitch(
                    checked = schedule.enabled,
                    onCheckedChange = onToggleQuietHours,
                    contentDescription = "Bật giờ yên lặng",
                )
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp),
        ) {
            // ── 24h Dial ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    QuietHoursDial(schedule = schedule)
                }
            }

            // ── Steppers (.qs-steps) ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QuietTimeStepperCard(
                        label = "Bắt đầu",
                        formattedTime = schedule.startTimeFormatted,
                        onMinus = { onStepStart(-30) },
                        onPlus = { onStepStart(30) },
                        modifier = Modifier.weight(1f),
                    )
                    QuietTimeStepperCard(
                        label = "Kết thúc",
                        formattedTime = schedule.endTimeFormatted,
                        onMinus = { onStepEnd(-30) },
                        onPlus = { onStepEnd(30) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── LẶP LẠI (.days) ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DiuSectionHeader(title = "LẶP LẠI")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WEEKDAY_ITEMS.forEach { (dayNum, label) ->
                        val isSelected = dayNum in schedule.repeatDays
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleRepeatDay(dayNum) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) {
                                AppPalette.QuietPurple.copy(alpha = 0.45f)
                            } else {
                                Color.Transparent
                            },
                            border = if (isSelected) null else BorderStroke(1.dp, colors.line),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    color = if (isSelected) colors.ink else colors.inkSecondary,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }

            // ── VẪN CHO PHÉP GỌI KHẨN ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DiuSectionHeader(title = "VẪN CHO PHÉP GỌI KHẨN")
                DiuCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    EMERGENCY_CONTACTS.forEachIndexed { index, contact ->
                        if (index > 0) {
                            HorizontalDivider(thickness = 1.dp, color = colors.line)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            DiuOrbAvatar(
                                initials = contact.name.take(1),
                                moodColor = contact.moodColor,
                                size = 34.dp,
                            )
                            Text(
                                text = contact.name,
                                fontSize = 15.sp,
                                color = colors.ink,
                                modifier = Modifier.weight(1f),
                            )
                            DiuSwitch(
                                checked = contact.id in schedule.allowedEmergencyContactIds,
                                onCheckedChange = { onToggleEmergencyContact(contact.id) },
                                contentDescription = "Cho phép ${contact.name} gọi khẩn",
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Khi bạn nghỉ, người thân thấy vầng trăng nhỏ cạnh tên bạn. Tin nhắn vẫn đến, lặng lẽ chờ bạn thức dậy.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun QuietTimeStepperCard(
    label: String,
    formattedTime: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = colors.inkSecondary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.ink.copy(alpha = 0.06f))
                        .clickable(onClick = onMinus),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "−",
                        fontSize = 18.sp,
                        color = colors.ink,
                    )
                }

                Text(
                    text = formattedTime,
                    fontFamily = DiuSerif,
                    fontSize = 22.sp,
                    color = colors.ink,
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.ink.copy(alpha = 0.06f))
                        .clickable(onClick = onPlus),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        color = colors.ink,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// #38 — NOTIFICATIONS (Màn "Thông báo" với 3 thẻ preview màn hình khóa)
// ═══════════════════════════════════════════════════════════════════════

private data class LockscreenPreviewStyle(
    val privacy: NotificationPrivacy,
    val title: String,
    val previewText: String,
)

private val LOCKSCREEN_STYLES = listOf(
    LockscreenPreviewStyle(
        privacy = NotificationPrivacy.MOOD_COLOR_ONLY,
        title = "Chỉ màu",
        previewText = "Một lời nhắn mang màu Thương",
    ),
    LockscreenPreviewStyle(
        privacy = NotificationPrivacy.SENDER_NAME,
        title = "Tên người gửi",
        previewText = "Minh gửi cho bạn một lời nhắn",
    ),
    LockscreenPreviewStyle(
        privacy = NotificationPrivacy.FULL_CONTENT,
        title = "Toàn bộ",
        previewText = "Anh tới đầu hẻm rồi nè…",
    ),
)

@Suppress("LongParameterList")
@Composable
private fun NotificationsSubScreen(
    privacy: NotificationPrivacy,
    sound: NotificationSound,
    vibrateOnce: Boolean,
    eveningDigest: Boolean,
    onBackClick: () -> Unit,
    onSelectPrivacy: (NotificationPrivacy) -> Unit,
    onSelectSound: (NotificationSound) -> Unit,
    onToggleVibrateOnce: (Boolean) -> Unit,
    onToggleEveningDigest: (Boolean) -> Unit,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiuTopBar(
            title = "Thông báo",
            navigationIcon = {
                PrototypeBackButton(onClick = onBackClick)
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp),
        ) {
            item {
                DiuSectionHeader(title = "HIỂN THỊ TRÊN MÀN HÌNH KHOÁ")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LOCKSCREEN_STYLES.forEach { item ->
                        val isSelected = privacy == item.privacy
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { onSelectPrivacy(item.privacy) },
                            shape = RoundedCornerShape(22.dp),
                            color = if (isSelected) colors.surface else Color.Transparent,
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = if (isSelected) colors.accent else colors.line,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                // .ns-prev
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = colors.surfaceElevated,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .shadow(
                                                    elevation = 8.dp,
                                                    shape = CircleShape,
                                                    ambientColor = AppPalette.MoodThuong,
                                                    spotColor = AppPalette.MoodThuong,
                                                )
                                                .clip(CircleShape)
                                                .background(AppPalette.MoodThuong),
                                        )
                                        Text(
                                            text = item.previewText,
                                            fontSize = 13.sp,
                                            color = colors.inkSecondary,
                                        )
                                    }
                                }

                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.ink,
                                    modifier = Modifier.padding(start = 4.dp),
                                )
                            }
                        }
                    }
                }
            }

            // ── ÂM BÁO ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DiuSectionHeader(title = "ÂM BÁO")
                SettingsGlassGroupCard(
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    NotificationSound.entries.forEachIndexed { index, soundOption ->
                        if (index > 0) {
                            HorizontalDivider(thickness = 1.dp, color = colors.line)
                        }
                        val isSelected = sound == soundOption
                        PrototypeSettingRow(
                            iconRes = if (soundOption == NotificationSound.SILENT) {
                                AppIcons.BellOff
                            } else {
                                AppIcons.Speaker
                            },
                            title = soundOption.label,
                            onClick = { onSelectSound(soundOption) },
                            trailing = {
                                PrototypeRadioIndicator(selected = isSelected)
                            },
                        )
                    }
                }
            }

            // ── KHÁC ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DiuSectionHeader(title = "KHÁC")
                SettingsGlassGroupCard(
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    PrototypeSettingRow(
                        iconRes = AppIcons.Phone,
                        title = "Rung nhẹ một lần",
                        trailing = {
                            DiuSwitch(
                                checked = vibrateOnce,
                                onCheckedChange = onToggleVibrateOnce,
                                contentDescription = "Rung nhẹ một lần",
                            )
                        },
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    PrototypeSettingRow(
                        iconRes = AppIcons.Calendar,
                        title = "Gom tin nhóm thành bản tóm tắt buổi tối",
                        trailing = {
                            DiuSwitch(
                                checked = eveningDigest,
                                onCheckedChange = onToggleEveningDigest,
                                contentDescription = "Gom tin nhóm thành bản tóm tắt buổi tối",
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PrototypeRadioIndicator(selected: Boolean) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(
                width = if (selected) 7.dp else 1.5.dp,
                color = if (selected) colors.accent else colors.line,
                shape = CircleShape,
            ),
    )
}

// ═══════════════════════════════════════════════════════════════════════
// #36 — GENERAL_SETTINGS (Màn "Cài đặt chung")
// ═══════════════════════════════════════════════════════════════════════

@Suppress("LongParameterList")
@Composable
private fun GeneralSettingsSubScreen(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenBlockedContacts: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenQuietHours: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onShowLogoutConfirm: () -> Unit,
    onShowDeleteAccount: () -> Unit,
) {
    val colors = AppTheme.colors
    val isDark = uiState.themeMode == "DARK"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiuTopBar(
            title = "Cài đặt",
            navigationIcon = {
                PrototypeBackButton(onClick = onBackClick)
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp),
        ) {
            // ── TÀI KHOẢN ──
            item {
                DiuSectionHeader(title = "TÀI KHOẢN")
                SettingsGlassGroupCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PrototypeSettingRow(
                        iconRes = AppIcons.User,
                        title = "Hồ sơ cá nhân",
                        onClick = onOpenEditProfile,
                        showChevron = true,
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    PrototypeSettingRow(
                        iconRes = AppIcons.Phone,
                        title = "Số điện thoại",
                        valueText = uiState.userProfile.phone,
                    )
                }
            }

            // ── BẢO MẬT ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DiuSectionHeader(title = "BẢO MẬT")
                SettingsGlassGroupCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PrototypeSettingRow(
                        iconRes = AppIcons.Lock,
                        title = "Mã hoá hai đầu",
                        valueText = "Đã bật",
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    PrototypeSettingRow(
                        iconRes = AppIcons.Block,
                        title = "Đã tạm ngừng liên lạc",
                        onClick = onOpenBlockedContacts,
                        showChevron = true,
                    )
                }
            }

            // ── ỨNG DỤNG ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DiuSectionHeader(title = "ỨNG DỤNG")
                SettingsGlassGroupCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PrototypeSettingRow(
                        iconRes = AppIcons.Bell,
                        title = "Thông báo",
                        onClick = onOpenNotifications,
                        showChevron = true,
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    PrototypeSettingRow(
                        iconRes = AppIcons.Moon,
                        title = "Giờ yên lặng",
                        onClick = onOpenQuietHours,
                        showChevron = true,
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    PrototypeSettingRow(
                        iconRes = AppIcons.Image,
                        title = "Lưu trữ & dữ liệu",
                        onClick = onNavigateToStorage,
                        showChevron = true,
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    PrototypeSettingRow(
                        iconRes = if (isDark) AppIcons.Moon else AppIcons.Sun,
                        title = "Chế độ tối",
                        trailing = {
                            DiuSwitch(
                                checked = isDark,
                                onCheckedChange = onToggleDarkMode,
                                contentDescription = "Chế độ tối",
                            )
                        },
                    )
                }
            }

            // ── GIỚI THIỆU ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DiuSectionHeader(title = "GIỚI THIỆU")
                SettingsGlassGroupCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PrototypeSettingRow(
                        iconRes = AppIcons.Heart,
                        title = "Về dịu",
                        valueText = "v1.0.0",
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    PrototypeSettingRow(
                        iconRes = AppIcons.Share,
                        title = "Giới thiệu cho bạn bè",
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    PrototypeSettingRow(
                        iconRes = AppIcons.Chat,
                        title = "Góp ý & hỗ trợ",
                    )
                }
            }

            // ── DANGER ACTIONS ──
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SettingsGlassGroupCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PrototypeSettingRow(
                        iconRes = AppIcons.Back,
                        title = "Đăng xuất",
                        isDanger = true,
                        onClick = onShowLogoutConfirm,
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    PrototypeSettingRow(
                        iconRes = AppIcons.Trash,
                        title = "Xoá tài khoản",
                        isDanger = true,
                        onClick = onShowDeleteAccount,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// #53 — BLOCKED_CONTACTS (Màn "Tạm ngừng liên lạc")
// ═══════════════════════════════════════════════════════════════════════

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
            title = "Tạm ngừng liên lạc",
            navigationIcon = {
                PrototypeBackButton(onClick = onBackClick)
            },
        )

        if (uiState.blockedContacts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                DiuEmptyState(
                    title = "Không ai bị tạm ngừng liên lạc",
                    description = "Bình yên ghê. Người bị tạm ngừng sẽ không thể gửi tin nhắn hay gọi cho bạn.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp),
            ) {
                items(uiState.blockedContacts, key = { it.id }) { blocked ->
                    BlockedContactRow(
                        contact = blocked,
                        onUnblock = { onUnblock(blocked.id) },
                    )
                }
                item {
                    Text(
                        text = "Người bị tạm ngừng sẽ không thể gửi tin nhắn hay gọi cho bạn. Họ không biết rằng bạn đã tạm ngừng.",
                        color = colors.inkSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
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
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DiuOrbAvatar(
            initials = contact.name.ifEmpty { "?" }.take(1).uppercase(),
            moodColor = AppPalette.MoodTram,
            size = 44.dp,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                color = colors.ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "từ ${contact.blockedAt}",
                color = colors.inkSecondary,
                fontSize = 14.sp,
            )
        }

        Text(
            text = "Bỏ chặn",
            color = colors.inkSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onUnblock)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Shared Settings Primitives (.card, .set, .ib)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun SettingsGlassGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column {
            content()
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun PrototypeSettingRow(
    @DrawableRes iconRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    valueText: String? = null,
    isDanger: Boolean = false,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val contentTint = if (isDanger) colors.danger else colors.ink

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (isDanger) colors.danger else colors.inkSecondary,
            modifier = Modifier.size(19.dp),
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                color = contentTint,
                fontSize = 15.sp,
            )
            if (!badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.peach.copy(alpha = 0.50f),
                ) {
                    Text(
                        text = badgeText,
                        color = colors.ink,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                    )
                }
            }
        }

        if (!valueText.isNullOrBlank()) {
            Text(
                text = valueText,
                color = colors.inkTertiary,
                fontSize = 12.sp,
            )
        }

        trailing?.invoke()

        if (showChevron) {
            Icon(
                painter = painterResource(AppIcons.Chev),
                contentDescription = null,
                tint = colors.inkSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun PrototypeBackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            painter = painterResource(AppIcons.Back),
            contentDescription = "Quay lại",
            tint = AppTheme.colors.inkSecondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileOverviewPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.background,
        ) {
            ProfileOverviewContent(
                uiState = SettingsUiState(
                    userProfile = UserProfile(
                        id = "u1",
                        displayName = "Lan",
                        username = "lan",
                        bio = "Thích mưa, cà phê sữa và những buổi chiều chậm.",
                        birthday = "14/02",
                        phone = "+84 912 345 678",
                        primaryMood = "Vui",
                        avatarUrl = null,
                    ),
                ),
                onOpenEditProfile = {},
                onSelectMood = {},
                onToggleQuietHours = {},
                onOpenQuietHours = {},
                onToggleDarkMode = {},
                onToggleSoftSendAnimation = {},
                onNavigateToJournal = {},
                onOpenNotifications = {},
                onToggleReadReceipts = {},
                onNavigateToLetters = {},
                onNavigateToContacts = {},
                onNavigateToFavourites = {},
                onOpenGeneralSettings = {},
                onReplayOnboarding = {},
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
                        BlockedContact("b1", "Huy", "+84900000001", "12/8"),
                    ),
                ),
                onBackClick = {},
                onUnblock = {},
            )
        }
    }
}
