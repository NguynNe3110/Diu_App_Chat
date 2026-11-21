package com.uzuu.diuchat.feature.contacts.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInviteCodeCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuPersonRow
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSearchField
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTextButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import org.koin.androidx.compose.koinViewModel

@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    onNavigateToChat: (String) -> Unit = {},
    onStartCall: (String) -> Unit = {},
    viewModel: ContactsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    ContactsScreenContent(
        uiState = uiState,
        onNavigateToChat = onNavigateToChat,
        onStartCall = onStartCall,
        onShowQrSheet = viewModel::onShowQrSheet,
        onDismissQrSheet = viewModel::onDismissQrSheet,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::onClearSearch,
        onSelectCircle = viewModel::onSelectCircle,
        onAcceptRequest = { id ->
            val req = uiState.pendingRequests.find { it.id == id }
            val circle = req?.proposedCircle ?: ContactCircle.FRIENDS
            viewModel.onAcceptRequest(id, circle)
        },
        onDeclineRequest = viewModel::onDeclineRequest,
        onCopyInviteLink = viewModel::onCopyInviteLink,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreenContent(
    uiState: ContactsUiState,
    onNavigateToChat: (String) -> Unit = {},
    onStartCall: (String) -> Unit = {},
    onShowQrSheet: () -> Unit = {},
    onDismissQrSheet: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    onSelectCircle: (ContactCircle) -> Unit = {},
    onAcceptRequest: (String) -> Unit = {},
    onDeclineRequest: (String) -> Unit = {},
    onCopyInviteLink: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Column(
        modifier =
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiuTopBar(
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
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.accent)
            }
        } else {
            LazyColumn(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                // "Ai đang thấy…" mood filter row (matching prototype .moodfilter)
                if (uiState.searchQuery.isEmpty() && uiState.contacts.isNotEmpty()) {
                    item {
                        MoodFilterRow(
                            contacts = uiState.contacts,
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
                            onCallClick = { onStartCall(contact.id) },
                        )
                    }

                    // Invite row (matching prototype .invite-row)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
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

    // QR Code Bottom Sheet
    if (uiState.isQrSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                )

                Spacer(modifier = Modifier.height(24.dp))

                DiuInviteCodeCard(
                    code = uiState.myInviteLink,
                    label = "Đường dẫn kết nối riêng",
                    copyLabel = if (uiState.copiedLink) "Đã sao chép!" else "Sao chép",
                    onCopy = onCopyInviteLink,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                DiuButton(
                    onClick = onDismissQrSheet,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Đóng")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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

@Composable
private fun MoodFilterRow(
    contacts: List<Contact>,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val moods = listOf(
        Triple("thuong", "Thương", colors.moods.thuong),
        Triple("vui", "Vui", colors.moods.vui),
        Triple("binhYen", "Bình yên", colors.moods.binhyen),
        Triple("nho", "Nhớ", colors.moods.nho),
        Triple("tram", "Trầm", colors.moods.tram),
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
            moods.forEach { (moodKey, moodName, moodColor) ->
                val matchedContacts = contacts.filter { contact ->
                    contact.moodColorHex.contains(moodKey, ignoreCase = true) ||
                        runCatching {
                            val parsed = Color(android.graphics.Color.parseColor(contact.moodColorHex))
                            parsed == moodColor
                        }.getOrDefault(false)
                }

                Column(
                    modifier =
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(moodColor.copy(alpha = 0.22f))
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
                                        color = moodColor.copy(alpha = 0.5f),
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
                                            moodColor = parseColorSafely(c.moodColorHex, moodColor),
                                            size = 26.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = moodName,
                        style = AppTheme.typography.bodySmall.copy(fontSize = 12.sp),
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
): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    fallback
}

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
                        moodColorHex = "#E57373",
                        nickname = "Em",
                    ),
                    Contact(
                        id = "c2",
                        name = "Nhà mình",
                        phone = "0909876543",
                        initials = "N",
                        circle = ContactCircle.FAMILY,
                        moodColorHex = "#81C784",
                    ),
                ),
                pendingRequests = listOf(
                    ConnectionRequest(
                        id = "r1",
                        senderId = "u_thao",
                        senderName = "Thảo",
                        senderPhone = "0912345678",
                        senderInitials = "T",
                        senderMoodColorHex = "#64B5F6",
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
