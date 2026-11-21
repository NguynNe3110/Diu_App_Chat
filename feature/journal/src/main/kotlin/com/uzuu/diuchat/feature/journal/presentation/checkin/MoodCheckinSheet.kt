package com.uzuu.diuchat.feature.journal.presentation.checkin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButtonVariant
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTextField
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodCheckinSheet(
    selectedMood: JournalMoodType?,
    note: String,
    allowlist: Set<String>,
    availableContacts: List<Pair<String, String>>,
    isSaving: Boolean,
    onSelectMood: (JournalMoodType) -> Unit,
    onChangeNote: (String) -> Unit,
    onToggleContact: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val moodColor = selectedMood?.color ?: AppTheme.colors.accent
    val animatedBgColor by animateColorAsState(
        targetValue = moodColor.copy(alpha = 0.08f),
        animationSpec = tween(600),
        label = "sheetBgGlow",
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AppTheme.colors.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(animatedBgColor, AppTheme.colors.background),
                    ),
                )
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Ghi màu hôm nay",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.inkSecondary,
                )
                Text(
                    text = "Để sau",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.inkSecondary,
                    modifier = Modifier.clickable { onDismissRequest() },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hôm nay bạn\nthấy thế nào?",
                style = AppTheme.typography.headlineMedium,
                color = AppTheme.colors.ink,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 6 Mood Orbs Grid (2 rows of 3)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                JournalMoodType.entries.chunked(3).forEach { rowMoods ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowMoods.forEach { mood ->
                            val isSelected = selectedMood == mood
                            val borderColor = if (isSelected) mood.color else AppTheme.colors.line
                            val containerColor = if (isSelected) {
                                mood.color.copy(alpha = 0.2f)
                            } else {
                                AppTheme.colors.surface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(containerColor)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(18.dp),
                                    )
                                    .clickable { onSelectMood(mood) }
                                    .padding(vertical = 14.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(mood.color),
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = mood.displayName,
                                        style = AppTheme.typography.labelSmall,
                                        color = if (isSelected) AppTheme.colors.ink else AppTheme.colors.inkSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (selectedMood != null) {
                Spacer(modifier = Modifier.height(24.dp))

                // Private note field (DEC-08)
                Text(
                    text = "Ghi chú riêng tư (chỉ mình bạn đọc được)",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.inkSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))

                DiuTextField(
                    value = note,
                    onValueChange = onChangeNote,
                    label = "Ghi chú riêng tư",
                    placeholder = "Một dòng cho riêng mình (tuỳ chọn)…",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Allowlist selection
                Text(
                    text = "Cho ai thấy màu này trong danh bạ",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.inkSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    availableContacts.forEach { (contactId, name) ->
                        val isShared = allowlist.contains(contactId)
                        DiuFilterChip(
                            text = name,
                            selected = isShared,
                            onClick = { onToggleContact(contactId) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CTA Button
            DiuButton(
                onClick = onSaveClick,
                enabled = selectedMood != null && !isSaving,
                variant = DiuButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (isSaving) "Đang lưu…" else "Lưu màu hôm nay",
                    style = AppTheme.typography.labelMedium,
                )
            }
        }
    }
}
