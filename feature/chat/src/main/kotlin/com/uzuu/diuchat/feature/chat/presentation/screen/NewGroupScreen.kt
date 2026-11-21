package com.uzuu.diuchat.feature.chat.presentation.screen

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSelectedPersonChip
import com.uzuu.diuchat.feature.chat.domain.model.MoodType

private data class SelectableContact(
    val id: String,
    val name: String,
    val mood: MoodType,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupScreen(
    onBackClick: () -> Unit,
    onCreateGroupSuccess: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCreateGroup: ((String, MoodType, List<String>, (String) -> Unit) -> Unit)? = null,
) {
    var groupTitle by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(MoodType.THUONG) }
    val selectedMembers = remember { mutableStateListOf<SelectableContact>() }

    val sampleContacts = remember {
        listOf(
            SelectableContact("user_linh", "Linh Dịu", MoodType.BINHYEN),
            SelectableContact("user_minh", "Minh Trí", MoodType.VUI),
            SelectableContact("user_ha", "Thu Hà", MoodType.THUONG),
            SelectableContact("user_nam", "Hoàng Nam", MoodType.NHO),
            SelectableContact("user_anh", "Quỳnh Anh", MoodType.TRAM),
        )
    }

    val moodColor = Color(selectedMood.colorHex)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tạo nhóm mới",
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
        bottomBar = {
            Surface(
                color = AppTheme.colors.surfaceElevated,
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.line),
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    val canCreate = groupTitle.trim().isNotEmpty() && selectedMembers.isNotEmpty()
                    DiuButton(
                        onClick = {
                            onCreateGroup?.invoke(
                                groupTitle.trim(),
                                selectedMood,
                                selectedMembers.map { it.id },
                                onCreateGroupSuccess,
                            ) ?: onCreateGroupSuccess("group_new")
                        },
                        enabled = canCreate,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Tạo nhóm (${selectedMembers.size} thành viên)")
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            // Group Title Field
            Text(
                text = "Tên nhóm",
                style = AppTheme.typography.labelMedium,
                color = AppTheme.colors.inkSecondary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = groupTitle,
                onValueChange = { groupTitle = it },
                placeholder = { Text("Ví dụ: Gia đình nhỏ, Bạn bè thân thiết...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = moodColor,
                    unfocusedBorderColor = AppTheme.colors.line,
                ),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Group Mood Theme Selector
            Text(
                text = "Sắc thái cảm xúc chủ đạo của nhóm",
                style = AppTheme.typography.labelMedium,
                color = AppTheme.colors.inkSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MoodType.entries.forEach { mood ->
                    val color = Color(mood.colorHex)
                    val isSelected = mood == selectedMood
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) color.copy(alpha = 0.25f) else AppTheme.colors.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) color else AppTheme.colors.line,
                        ),
                        modifier = Modifier.clickable { selectedMood = mood },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color),
                            )
                            Text(
                                text = mood.displayName,
                                style = AppTheme.typography.labelMedium,
                                color = if (isSelected) AppTheme.colors.ink else AppTheme.colors.inkSecondary,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Members Chips row using DiuSelectedPersonChip
            if (selectedMembers.isNotEmpty()) {
                Text(
                    text = "Đã chọn (${selectedMembers.size})",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.inkSecondary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selectedMembers.forEach { contact ->
                        DiuSelectedPersonChip(
                            name = contact.name,
                            initials = contact.name.take(1),
                            moodColor = Color(contact.mood.colorHex),
                            removeLabel = "Xóa ${contact.name}",
                            onRemove = { selectedMembers.remove(contact) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Contact selection list
            Text(
                text = "Chọn thành viên từ danh bạ",
                style = AppTheme.typography.labelMedium,
                color = AppTheme.colors.inkSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(sampleContacts) { contact ->
                    val isSelected = selectedMembers.any { it.id == contact.id }
                    val contactMoodColor = Color(contact.mood.colorHex)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) contactMoodColor.copy(alpha = 0.12f) else AppTheme.colors.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) contactMoodColor else AppTheme.colors.line,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedMembers.removeAll { it.id == contact.id }
                                } else {
                                    selectedMembers.add(contact)
                                }
                            },
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
                                        .background(contactMoodColor.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = contact.name.take(1),
                                        style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = contactMoodColor,
                                    )
                                }
                                Column {
                                    Text(
                                        text = contact.name,
                                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = AppTheme.colors.ink,
                                    )
                                    Text(
                                        text = contact.mood.displayName,
                                        style = AppTheme.typography.bodySmall,
                                        color = contactMoodColor,
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(contactMoodColor),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Đã chọn",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
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
