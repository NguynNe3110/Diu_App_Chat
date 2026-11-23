package com.uzuu.diuchat.feature.contacts.presentation.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInviteCodeCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuPersonListSkeleton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuPersonRow
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuQrCodeImage
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSearchField
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTabHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTextButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTextField
import com.uzuu.diuchat.feature.base.presentation.compose.composable.parseMoodColorHex
import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.presentation.composable.QrCameraScannerPreview
import org.koin.androidx.compose.koinViewModel

@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    pendingInviteCode: String? = null,
    onConsumePendingInviteCode: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onStartCall: (peerId: String, peerName: String, peerMoodHex: String) -> Unit = { _, _, _ -> },
    viewModel: ContactsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val currentOnConsumePendingInviteCode by rememberUpdatedState(onConsumePendingInviteCode)

    LaunchedEffect(pendingInviteCode) {
        if (!pendingInviteCode.isNullOrBlank()) {
            viewModel.onOpenInviteConfirmDialog(pendingInviteCode)
            currentOnConsumePendingInviteCode()
        }
    }

    ContactsScreenContent(
        uiState = uiState,
        onNavigateToChat = { contactId ->
            viewModel.onOpenContactChat(contactId, onNavigateToChat)
        },
        onStartCall = onStartCall,
        onShowQrSheet = viewModel::onShowQrSheet,
        onDismissQrSheet = viewModel::onDismissQrSheet,
        onSelectQrTab = viewModel::onSelectQrTab,
        onInviteCodeInputChange = viewModel::onInviteCodeInputChange,
        onSubmitInviteCode = { code ->
            if (code != null) {
                viewModel.onSubmitInviteCode(code)
            } else {
                viewModel.onSubmitInviteCode()
            }
        },
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::onClearSearch,
        onSelectCircle = viewModel::onSelectCircle,
        onSelectMood = viewModel::onSelectMood,
        onAcceptRequest = { id ->
            val req = uiState.pendingRequests.find { it.id == id }
            val circle = req?.proposedCircle ?: ContactCircle.FRIENDS
            viewModel.onAcceptRequest(id, circle)
        },
        onDeclineRequest = viewModel::onDeclineRequest,
        onCopyInviteLink = viewModel::onCopyInviteLink,
        modifier = modifier,
    )

    val inviteConfirmCode = uiState.inviteConfirmCode
    if (inviteConfirmCode != null) {
        InviteConfirmDialog(
            code = inviteConfirmCode,
            isSubmitting = uiState.isSubmittingInviteConfirm,
            errorMessage = uiState.inviteConfirmError,
            onCodeChange = viewModel::onInviteConfirmCodeChange,
            onConfirm = viewModel::onConfirmInviteCode,
            onDismiss = viewModel::onDismissInviteConfirmDialog,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreenContent(
    uiState: ContactsUiState,
    onNavigateToChat: (String) -> Unit = {},
    onStartCall: (peerId: String, peerName: String, peerMoodHex: String) -> Unit = { _, _, _ -> },
    onShowQrSheet: () -> Unit = {},
    onDismissQrSheet: () -> Unit = {},
    onSelectQrTab: (QrSheetTab) -> Unit = {},
    onInviteCodeInputChange: (String) -> Unit = {},
    onSubmitInviteCode: (String?) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    onSelectCircle: (ContactCircle) -> Unit = {},
    onSelectMood: (String?) -> Unit = {},
    onAcceptRequest: (String) -> Unit = {},
    onDeclineRequest: (String) -> Unit = {},
    onCopyInviteLink: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier =
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiuTabHeader(
            title = "Danh bạ",
            actions = {
                DiuIconButton(
                    onClick = onShowQrSheet,
                    contentDescription = "Mã kết nối của bạn",
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCode,
                        contentDescription = null,
                        tint = colors.ink,
                    )
                }
            },
        )

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
            DiuSearchField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                label = "Tìm người thân, lời nhắn…",
                placeholder = "Tìm người thân, số điện thoại…",
                onClear = onClearSearch,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Circle Filter Chips
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ContactCircle.entries.forEach { circle ->
                DiuFilterChip(
                    text = circle.label,
                    selected = uiState.selectedCircle == circle,
                    onClick = { onSelectCircle(circle) },
                )
            }
        }

        if (uiState.isLoading) {
            DiuPersonListSkeleton(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp),
                itemCount = 6,
                showTimestamp = false,
            )
        } else {
            LazyColumn(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 110.dp),
            ) {
                // "Ai đang thấy…" mood filter row (matching prototype .moodfilter)
                if (uiState.searchQuery.isEmpty() && uiState.contacts.isNotEmpty()) {
                    item {
                        MoodFilterRow(
                            contacts = uiState.contacts,
                            selectedMoodHex = uiState.selectedMoodHex,
                            onSelectMood = onSelectMood,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }

                // Pending requests section
                if (uiState.pendingRequests.isNotEmpty() && uiState.searchQuery.isEmpty()) {
                    item {
                        Text(
                            text = "Lời mời kết nối (${uiState.pendingRequests.size})",
                            style =
                            AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Serif,
                            ),
                            color = colors.ink,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                    }

                    items(uiState.pendingRequests, key = { it.id }) { request ->
                        ConnectionRequestItem(
                            request = request,
                            onAccept = { onAcceptRequest(request.id) },
                            onDecline = { onDeclineRequest(request.id) },
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Section header for contacts
                item {
                    val circleLabel = if (uiState.selectedCircle == ContactCircle.ALL) {
                        "Vòng thân"
                    } else {
                        uiState.selectedCircle.label
                    }
                    Text(
                        text = circleLabel,
                        style =
                        AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                        ),
                        color = colors.ink,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }

                // Contacts list or empty state
                if (uiState.filteredContacts.isEmpty()) {
                    item {
                        Box(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                        ) {
                            DiuEmptyState(
                                title = "Chưa có người thân trong vòng này",
                                description = "Chia sẻ mã kết nối hoặc tìm kiếm bằng số điện thoại " +
                                    "để kết nối người thân.",
                            )
                        }
                    }
                } else {
                    items(uiState.filteredContacts, key = { it.id }) { contact ->
                        ContactItemRow(
                            contact = contact,
                            onClick = { onNavigateToChat(contact.id) },
                            onCallClick = {
                                onStartCall(
                                    contact.id,
                                    contact.nickname?.takeIf { it.isNotBlank() } ?: contact.name,
                                    contact.moodColorHex,
                                )
                            },
                        )
                    }

                    // Invite row (matching prototype .invite-row)
                    item {
                        val dashBorderColor = colors.accent.copy(alpha = 0.55f)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .drawBehind {
                                    drawRoundRect(
                                        color = dashBorderColor,
                                        cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                                        style = Stroke(
                                            width = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                                        ),
                                    )
                                }
                                .clickable(onClick = onShowQrSheet)
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                painter = painterResource(AppIcons.Plus),
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Mời thêm người thân thiết",
                                style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = colors.accent,
                            )
                        }
                    }
                }
            }
        }
    }

    // QR Code / Scanner Bottom Sheet (#48)
    if (uiState.isQrSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val context = LocalContext.current
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED,
            )
        }
        val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                hasCameraPermission = granted
            }

        ModalBottomSheet(
            onDismissRequest = onDismissQrSheet,
            sheetState = sheetState,
            containerColor = colors.background,
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    QrSheetTabButton(
                        text = "Mã của tôi",
                        selected = uiState.selectedQrTab == QrSheetTab.MY_CODE,
                        onClick = { onSelectQrTab(QrSheetTab.MY_CODE) },
                        modifier = Modifier.weight(1f),
                    )
                    QrSheetTabButton(
                        text = "Quét mã",
                        selected = uiState.selectedQrTab == QrSheetTab.SCAN_CODE,
                        onClick = { onSelectQrTab(QrSheetTab.SCAN_CODE) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.selectedQrTab == QrSheetTab.MY_CODE) {
                    Text(
                        text = "Mã dịu của bạn",
                        style =
                        AppTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.ink,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Chỉ người có mã này mới có thể tìm thấy và gửi lời mời kết nối tới bạn.",
                        style = AppTheme.typography.bodyMedium,
                        color = colors.inkSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DiuQrCodeImage(
                        content =
                        if (uiState.myInviteLink.startsWith("http")) {
                            uiState.myInviteLink
                        } else {
                            "https://${uiState.myInviteLink}"
                        },
                        size = 200.dp,
                        contentDescription = "Mã QR kết nối cá nhân",
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DiuInviteCodeCard(
                        code = uiState.myInviteLink,
                        label = "Đường dẫn kết nối riêng",
                        copyLabel = if (uiState.copiedLink) "Đã sao chép!" else "Sao chép",
                        onCopy = {
                            val link =
                                if (uiState.myInviteLink.startsWith("http")) {
                                    uiState.myInviteLink
                                } else {
                                    "https://${uiState.myInviteLink}"
                                }
                            clipboardManager.setText(AnnotatedString(link))
                            onCopyInviteLink()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DiuButton(
                        onClick = {
                            val link =
                                if (uiState.myInviteLink.startsWith("http")) {
                                    uiState.myInviteLink
                                } else {
                                    "https://${uiState.myInviteLink}"
                                }
                            val shareText = "Mình mời bạn kết nối trên Dịu nhé!\n$link"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ link mời Dịu"))
                        },
                        enabled = uiState.myInviteLink.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Chia sẻ link mời")
                    }
                } else {
                    Text(
                        text = "Quét mã kết nối",
                        style =
                        AppTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.ink,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hướng camera về mã QR dịu của người thân hoặc nhập mã kết nối thủ công.",
                        style = AppTheme.typography.bodyMedium,
                        color = colors.inkSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = colors.surface,
                    ) {
                        if (hasCameraPermission) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                QrCameraScannerPreview(
                                    onDetectCode = { detectedCode ->
                                        onInviteCodeInputChange(detectedCode)
                                        onSubmitInviteCode(detectedCode)
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(132.dp)
                                        .border(
                                            width = 2.dp,
                                            color = colors.accent.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(16.dp),
                                        ),
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    painter = painterResource(AppIcons.Camera),
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(32.dp),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Cần quyền Camera để quét mã QR trực tiếp",
                                    style = AppTheme.typography.bodySmall,
                                    color = colors.inkSecondary,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                DiuButton(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                ) {
                                    Text("Cấp quyền Camera")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DiuTextField(
                        value = uiState.inviteCodeInput,
                        onValueChange = onInviteCodeInputChange,
                        label = "Nhập mã hoặc đường dẫn mời",
                        placeholder = "Ví dụ: https://diu-chat.vercel.app/invite/8X3K2P",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (uiState.scanResultMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.scanResultMessage,
                            style = AppTheme.typography.bodySmall,
                            color = colors.accent,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DiuButton(
                        onClick = { onSubmitInviteCode(null) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Kết nối bằng mã")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DiuTextButton(
                    text = "Đóng",
                    onClick = onDismissQrSheet,
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InviteConfirmDialog(
    code: String,
    isSubmitting: Boolean,
    errorMessage: String?,
    onCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        containerColor = colors.background,
        title = {
            Text(
                text = "Xác nhận kết nối",
                style = AppTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.ink,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Bạn vừa mở lời mời kết bạn trên Dịu. Kiểm tra mã mời bên dưới và bấm xác nhận để kết nối.",
                    style = AppTheme.typography.bodyMedium,
                    color = colors.inkSecondary,
                )
                DiuTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    label = "Mã mời",
                    placeholder = "Ví dụ: 8X3K2P",
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = AppTheme.typography.bodySmall,
                        color = colors.danger,
                    )
                }
            }
        },
        confirmButton = {
            DiuButton(
                onClick = onConfirm,
                enabled = !isSubmitting && code.isNotBlank(),
            ) {
                Text(if (isSubmitting) "Đang kết nối…" else "Xác nhận")
            }
        },
        dismissButton = {
            DiuTextButton(
                text = "Hủy",
                onClick = {
                    if (!isSubmitting) {
                        onDismiss()
                    }
                },
            )
        },
    )
}

@Composable
private fun QrSheetTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier =
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.accent else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) colors.onAccent else colors.inkSecondary,
        )
    }
}

@Composable
private fun ConnectionRequestItem(
    request: ConnectionRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val moodColor = parseColorSafely(request.senderMoodColorHex, colors.moods.thuong)

    DiuCard(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DiuPersonRow(
                name = request.senderName,
                initials = request.senderInitials,
                moodColor = moodColor,
                subtitle = "Muốn thêm vào: ${request.proposedCircle.label} · ${request.senderPhone}",
                timestamp = request.createdAt,
                onClick = {},
            )

            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                DiuTextButton(text = "Từ chối", onClick = onDecline)

                DiuButton(
                    onClick = onAccept,
                    modifier = Modifier.height(40.dp),
                ) {
                    Text("Chấp nhận", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ContactItemRow(
    contact: Contact,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val moodColor = parseColorSafely(contact.moodColorHex, colors.moods.thuong)

    val subtitle =
        buildString {
            append(contact.circle.label)
            if (!contact.nickname.isNullOrBlank()) {
                append(" · ")
                append(contact.nickname)
            }
            if (contact.isQuiet) {
                append(" · Đang nghỉ ngơi")
            }
        }

    val timestamp = contact.anniversary?.let { "Kỷ niệm $it" }

    DiuPersonRow(
        name = contact.name,
        initials = contact.initials,
        moodColor = moodColor,
        subtitle = subtitle,
        timestamp = timestamp,
        onClick = onClick,
        trailing = {
            DiuIconButton(
                onClick = onCallClick,
                soft = true,
                contentDescription = "Gọi cho ${contact.name}",
            ) {
                Icon(
                    painter = painterResource(AppIcons.Phone),
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
        modifier = modifier,
    )
}

private data class MoodFilterOption(
    val key: String,
    val label: String,
    val hex: String,
    val color: Color,
)

@Composable
private fun MoodFilterRow(
    contacts: List<Contact>,
    selectedMoodHex: String?,
    onSelectMood: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val moods =
        listOf(
            MoodFilterOption("thuong", "Thương", "#F7A3A9", colors.moods.thuong),
            MoodFilterOption("vui", "Vui", "#F4CA84", colors.moods.vui),
            MoodFilterOption("binhYen", "Bình yên", "#A2D7AC", colors.moods.binhyen),
            MoodFilterOption("nho", "Nhớ", "#C5B3EA", colors.moods.nho),
            MoodFilterOption("tram", "Trầm", "#95C4E7", colors.moods.tram),
        )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Ai đang thấy…",
            style =
            AppTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
            ),
            color = colors.ink,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            moods.forEach { mood ->
                val isSelected = selectedMoodHex.equals(mood.hex, ignoreCase = true)
                val matchedContacts =
                    contacts.filter { contact ->
                        contact.moodColorHex.equals(mood.hex, ignoreCase = true) ||
                            contact.moodColorHex.contains(mood.key, ignoreCase = true) ||
                            parseMoodColorHex(contact.moodColorHex, Color.Unspecified) == mood.color
                    }

                Column(
                    modifier =
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(mood.color.copy(alpha = if (isSelected) 0.40f else 0.22f))
                        .then(
                            if (isSelected) {
                                Modifier.border(2.dp, mood.color, RoundedCornerShape(20.dp))
                            } else {
                                Modifier
                            },
                        )
                        .clickable { onSelectMood(mood.hex) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier.height(26.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (matchedContacts.isEmpty()) {
                            Box(
                                modifier =
                                Modifier
                                    .size(26.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = mood.color.copy(alpha = 0.5f),
                                        shape = CircleShape,
                                    ),
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                matchedContacts.take(2).forEachIndexed { index, c ->
                                    Box(
                                        modifier =
                                        Modifier
                                            .offset(x = if (index > 0) (-8).dp else 0.dp)
                                            .size(26.dp)
                                            .border(
                                                width = 2.dp,
                                                color = colors.background,
                                                shape = CircleShape,
                                            ),
                                    ) {
                                        DiuOrbAvatar(
                                            initials = c.initials,
                                            moodColor = parseColorSafely(c.moodColorHex, mood.color),
                                            size = 26.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = mood.label,
                        style =
                        AppTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        color = colors.ink,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun parseColorSafely(
    hex: String,
    fallback: Color,
): Color = parseMoodColorHex(hex, fallback)

@Preview(name = "Contacts Screen — Light", showBackground = true)
@Composable
private fun ContactsScreenPreview() {
    AppTheme {
        ContactsScreenContent(
            uiState = ContactsUiState(
                contacts = listOf(
                    Contact(
                        id = "c1",
                        name = "Minh",
                        phone = "0901234567",
                        initials = "M",
                        circle = ContactCircle.LOVE,
                        moodColorHex = "#F7A3A9",
                        nickname = "Em",
                    ),
                    Contact(
                        id = "c2",
                        name = "Nhà mình",
                        phone = "0909876543",
                        initials = "N",
                        circle = ContactCircle.FAMILY,
                        moodColorHex = "#A2D7AC",
                    ),
                ),
                pendingRequests = listOf(
                    ConnectionRequest(
                        id = "r1",
                        senderId = "u_thao",
                        senderName = "Thảo",
                        senderPhone = "0912345678",
                        senderInitials = "T",
                        senderMoodColorHex = "#95C4E7",
                        proposedCircle = ContactCircle.FRIENDS,
                        createdAt = "Hôm nay",
                    ),
                ),
            ),
        )
    }
}

@Preview(name = "Contacts Screen — QR Sheet", showBackground = true)
@Composable
private fun ContactsScreenQrPreview() {
    AppTheme {
        ContactsScreenContent(
            uiState = ContactsUiState(
                isQrSheetVisible = true,
                myInviteLink = "https://diu.app/invite/c129-61b9",
            ),
        )
    }
}
