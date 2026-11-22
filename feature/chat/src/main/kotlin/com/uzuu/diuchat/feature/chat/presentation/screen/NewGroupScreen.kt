package com.uzuu.diuchat.feature.chat.presentation.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSelectedPersonChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.chat.domain.model.MoodType

private val CORE_FIVE_GROUP_MOODS = listOf(
    MoodType.THUONG,
    MoodType.VUI,
    MoodType.BINHYEN,
    MoodType.NHO,
    MoodType.TRAM,
)

private const val MAX_SELECTABLE_PEERS = 7

private data class SelectableContact(
    val id: String,
    val name: String,
    val roleLabel: String,
    val mood: MoodType,
)

@Composable
fun NewGroupScreen(
    onBackClick: () -> Unit,
    onCreateGroupSuccess: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedMemberIds: List<String>? = null,
    groupTitle: String? = null,
    selectedMood: MoodType? = null,
    onToggleMember: ((String) -> Unit)? = null,
    onUpdateDraft: ((String, MoodType) -> Unit)? = null,
    onCreateGroup: ((String, MoodType, List<String>, (String) -> Unit) -> Unit)? = null,
) {
    var localTitle by remember { mutableStateOf("") }
    var localMood by remember { mutableStateOf(MoodType.THUONG) }
    val localSelectedIds = remember { mutableStateListOf<String>() }

    val activeTitle = groupTitle ?: localTitle
    val activeMood = selectedMood ?: localMood
    val activeSelectedIds: List<String> = selectedMemberIds ?: localSelectedIds

    val updateTitleAndMood: (String, MoodType) -> Unit = { newTitle, newMood ->
        if (onUpdateDraft != null) {
            onUpdateDraft(newTitle, newMood)
        } else {
            localTitle = newTitle
            localMood = newMood
        }
    }

    val toggleMemberSelection: (String) -> Unit = { memberId ->
        if (onToggleMember != null) {
            onToggleMember(memberId)
        } else {
            if (localSelectedIds.contains(memberId)) {
                localSelectedIds.remove(memberId)
            } else if (localSelectedIds.size < MAX_SELECTABLE_PEERS) {
                localSelectedIds.add(memberId)
            }
        }
    }

    val sampleContacts = remember {
        listOf(
            SelectableContact("user_minh", "Minh", "Người thương", MoodType.THUONG),
            SelectableContact("user_me", "Mẹ", "Gia đình", MoodType.VUI),
            SelectableContact("user_thao", "Thảo", "Bạn thân", MoodType.NHO),
            SelectableContact("user_ba", "Ba", "Gia đình", MoodType.BINHYEN),
            SelectableContact("user_an", "An", "Bạn thân", MoodType.TRAM),
            SelectableContact("user_linh", "Chị Linh", "Gia đình", MoodType.THUONG),
            SelectableContact("user_khoa", "Khoa", "Bạn thân", MoodType.VUI),
            SelectableContact("user_ha", "Thu Hà", "Bạn thân", MoodType.BINHYEN),
        )
    }

    val colors = AppTheme.colors
    val moodColor = Color(activeMood.colorHex)
    val totalMemberCount = activeSelectedIds.size + 1

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            DiuTopBar(
                title = "Nhóm nhỏ mới",
                subtitle = "tối đa 8 người",
                navigationIcon = {
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
                },
            )
        },
        bottomBar = {
            // Floating bottom CTA (.float-cta)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                val canCreate = activeTitle.trim().isNotEmpty() && activeSelectedIds.size in 2..MAX_SELECTABLE_PEERS
                DiuButton(
                    onClick = {
                        onCreateGroup?.invoke(
                            activeTitle.trim(),
                            activeMood,
                            activeSelectedIds,
                            onCreateGroupSuccess,
                        ) ?: onCreateGroupSuccess("group_new")
                    },
                    enabled = canCreate,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Tạo nhóm · $totalMemberCount người")
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp),
        ) {
            // Header (.ng-head): Live 72.dp DiuOrbAvatar with badgeText + underlined DiuSerif 22.sp input + 5 mood dots
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.line),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DiuOrbAvatar(
                        initials = activeTitle.trim().take(1).ifEmpty { "N" },
                        moodColor = moodColor,
                        size = 72.dp,
                        badgeText = "$totalMemberCount",
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column {
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (activeTitle.isEmpty()) {
                                    Text(
                                        text = "Đặt tên nhóm…",
                                        style = AppTheme.headlineSerif.copy(
                                            fontFamily = DiuSerif,
                                            fontSize = 22.sp,
                                        ),
                                        color = colors.inkTertiary,
                                    )
                                }
                                BasicTextField(
                                    value = activeTitle,
                                    onValueChange = { updateTitleAndMood(it, activeMood) },
                                    textStyle = AppTheme.headlineSerif.copy(
                                        fontFamily = DiuSerif,
                                        fontSize = 22.sp,
                                        color = colors.ink,
                                    ),
                                    cursorBrush = SolidColor(colors.ink),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                )
                            }
                            HorizontalDivider(thickness = 1.dp, color = colors.line)
                        }

                        // 5 core mood dots (22.dp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CORE_FIVE_GROUP_MOODS.forEach { mood ->
                                val dotColor = Color(mood.colorHex)
                                val isSelected = mood == activeMood
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(2.dp, colors.ink, CircleShape)
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .clickable { updateTitleAndMood(activeTitle, mood) },
                                )
                            }
                        }
                    }
                }
            }

            // Selected Members Chips row (.ng-sel)
            val selectedContacts = sampleContacts.filter { activeSelectedIds.contains(it.id) }
            if (selectedContacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selectedContacts.forEach { contact ->
                        DiuSelectedPersonChip(
                            name = contact.name,
                            initials = contact.name.take(1),
                            moodColor = Color(contact.mood.colorHex),
                            removeLabel = "Xóa ${contact.name}",
                            onRemove = { toggleMemberSelection(contact.id) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "VÒNG THÂN",
                style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.inkSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = sampleContacts,
                    key = { it.id },
                ) { contact ->
                    val isSelected = activeSelectedIds.contains(contact.id)
                    val contactMoodColor = Color(contact.mood.colorHex)

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) contactMoodColor else colors.line,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { toggleMemberSelection(contact.id) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                DiuOrbAvatar(
                                    initials = contact.name.take(1),
                                    moodColor = contactMoodColor,
                                    size = 44.dp,
                                )
                                Column {
                                    Text(
                                        text = contact.name,
                                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = colors.ink,
                                    )
                                    Text(
                                        text = contact.roleLabel,
                                        style = AppTheme.typography.bodySmall,
                                        color = colors.inkSecondary,
                                    )
                                }
                            }

                            // 26.dp circular .chk indicator
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) colors.ink else Color.Transparent)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) colors.ink else colors.line,
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSelected) {
                                    Icon(
                                        painter = painterResource(AppIcons.Check),
                                        contentDescription = "Đã chọn",
                                        tint = colors.background,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewGroupScreenPreview() {
    AppTheme {
        NewGroupScreen(
            onBackClick = {},
            onCreateGroupSuccess = {},
        )
    }
}
