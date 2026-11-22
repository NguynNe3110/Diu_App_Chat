package com.uzuu.diuchat.feature.journal.presentation.checkin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButtonVariant
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTextField
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

            Spacer(modifier = Modifier.height(24.dp))

            // 5 Glowing 70.dp Floating Orbs
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                JournalMoodType.coreFiveMoods().forEach { mood ->
                    val isSelected = selectedMood == mood
                    val targetScale = if (isSelected) 1.18f else 1f
                    val targetAlpha = when {
                        selectedMood == null -> 1f
                        isSelected -> 1f
                        else -> 0.4f
                    }
                    val scale by animateFloatAsState(targetValue = targetScale, label = "orbScale")
                    val orbAlpha by animateFloatAsState(targetValue = targetAlpha, label = "orbAlpha")

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                alpha = orbAlpha
                            }
                            .clickable { onSelectMood(mood) }
                            .padding(4.dp),
                    ) {
                        DiuOrbAvatar(
                            initials = "",
                            moodColor = mood.color,
                            size = 70.dp,
                            isPulsing = isSelected,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = mood.displayName,
                            fontFamily = DiuSerif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) AppTheme.colors.ink else AppTheme.colors.inkSecondary,
                        )
                    }
                }
            }

            if (selectedMood != null) {
                Spacer(modifier = Modifier.height(24.dp))

                // Private note field (DEC-08)
                Text(
                    text = "Một dòng cho riêng mình",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.inkSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))

                DiuTextField(
                    value = note,
                    onValueChange = onChangeNote,
                    label = "Một dòng cho riêng mình",
                    placeholder = "Một dòng cho riêng mình (tuỳ chọn)…",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Allowlist selection with 30.dp DiuOrbAvatar chips
                Text(
                    text = "Cho ai thấy màu này",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.inkSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    availableContacts.forEach { (contactId, name) ->
                        val isShared = allowlist.contains(contactId)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isShared) {
                                        moodColor.copy(alpha = 0.25f)
                                    } else {
                                        AppTheme.colors.surface
                                    },
                                )
                                .border(
                                    width = if (isShared) 1.5.dp else 1.dp,
                                    color = if (isShared) moodColor else AppTheme.colors.line,
                                    shape = RoundedCornerShape(50),
                                )
                                .clickable { onToggleContact(contactId) }
                                .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                        ) {
                            DiuOrbAvatar(
                                initials = name.take(1),
                                moodColor = moodColor,
                                size = 30.dp,
                            )
                            Text(
                                text = name,
                                style = AppTheme.typography.bodySmall.copy(
                                    fontWeight = if (isShared) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = AppTheme.colors.ink,
                            )
                        }
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

@Preview(showBackground = true)
@Composable
private fun MoodCheckinSheetPreview() {
    AppTheme {
        MoodCheckinSheet(
            selectedMood = JournalMoodType.BINH_YEN,
            note = "Một ngày êm dịu và lòng thảnh thơi.",
            allowlist = setOf("minh", "me"),
            availableContacts = listOf(
                "minh" to "Minh",
                "me" to "Mẹ",
                "thao" to "Thảo",
            ),
            isSaving = false,
            onSelectMood = {},
            onChangeNote = {},
            onToggleContact = {},
            onSaveClick = {},
            onDismissRequest = {},
        )
    }
}
