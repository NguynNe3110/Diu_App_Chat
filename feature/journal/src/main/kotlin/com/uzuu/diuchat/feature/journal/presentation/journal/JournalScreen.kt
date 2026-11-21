package com.uzuu.diuchat.feature.journal.presentation.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButtonVariant
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodDot
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSectionHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.MoodComparison
import com.uzuu.diuchat.feature.journal.domain.model.MonthMoodBreakdown
import com.uzuu.diuchat.feature.journal.presentation.checkin.MoodCheckinSheet
import org.koin.androidx.compose.koinViewModel

@Composable
fun JournalScreen(
    onBackClick: () -> Unit,
    onOpenSearch: () -> Unit = {},
    viewModel: JournalViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DiuTopBar(
                title = "Nhật ký màu",
                subtitle = "Tháng ${state.month}",
                navigationIcon = {
                    DiuIconButton(
                        onClick = onBackClick,
                        contentDescription = "Quay lại",
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Back),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
            )
        },
        containerColor = AppTheme.colors.background,
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                // Calendar Grid Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CalendarGrid(
                        checkins = state.checkins,
                        selectedDay = state.selectedDay,
                        onSelectDay = viewModel::onSelectDay,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Selected Day Detail Card
                item {
                    val selectedCheckin = state.selectedCheckin
                    SelectedDayCard(
                        dayNumber = state.selectedDay,
                        month = state.month,
                        checkin = selectedCheckin,
                        onCheckinClick = viewModel::onOpenCheckinSheet,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Monthly Mood Breakdown Section
                item {
                    val breakdown = state.breakdown
                    if (breakdown != null) {
                        DiuSectionHeader(title = "Tháng này bạn thường thấy")
                        Spacer(modifier = Modifier.height(10.dp))
                        MoodBreakdownBars(breakdown = breakdown)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Mood Comparison ("Cùng Minh") Section
                item {
                    val comparison = state.comparison
                    if (comparison != null) {
                        DiuSectionHeader(title = "Cùng ${comparison.partnerName}")
                        Spacer(modifier = Modifier.height(10.dp))
                        MoodComparisonCard(comparison = comparison)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Bottom spacer for CTA
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }

            // Bottom CTA: Ghi màu hôm nay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(AppTheme.colors.background.copy(alpha = 0.95f))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                DiuButton(
                    onClick = viewModel::onOpenCheckinSheet,
                    variant = DiuButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Ghi màu hôm nay",
                        style = AppTheme.typography.labelMedium,
                    )
                }
            }
        }

        // Check-in Sheet Modal
        if (state.isCheckinSheetVisible) {
            MoodCheckinSheet(
                selectedMood = state.checkinSelectedMood,
                note = state.checkinNote,
                allowlist = state.checkinAllowlist,
                availableContacts = state.availableContacts,
                isSaving = state.isSavingCheckin,
                onSelectMood = viewModel::onSelectCheckinMood,
                onChangeNote = viewModel::onChangeCheckinNote,
                onToggleContact = viewModel::onToggleCheckinContact,
                onSaveClick = viewModel::onSaveCheckin,
                onDismissRequest = viewModel::onDismissCheckinSheet,
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    checkins: List<DailyMoodCheckin>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val checkinMap = checkins.associateBy { it.dayNumber }
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Column(modifier = modifier.fillMaxWidth()) {
        // Weekday headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.inkSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month days grid (30 days)
        val rows = (1..30).chunked(7)
        rows.forEach { weekDays ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                weekDays.forEach { day ->
                    val checkin = checkinMap[day]
                    val isSelected = day == selectedDay
                    val moodColor = checkin?.mood?.color ?: Color.Transparent
                    val isFuture = day > 24

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) {
                                    AppTheme.colors.accent.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                },
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) AppTheme.colors.accent else Color.Transparent,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable(enabled = !isFuture) { onSelectDay(day) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = day.toString(),
                                style = AppTheme.typography.bodySmall,
                                color = if (isFuture) {
                                    AppTheme.colors.inkSecondary.copy(alpha = 0.4f)
                                } else {
                                    AppTheme.colors.ink
                                },
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (checkin != null) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(moodColor),
                                )
                            } else {
                                Spacer(modifier = Modifier.size(6.dp))
                            }
                        }
                    }
                }
                // Fill row if incomplete
                val remaining = 7 - weekDays.size
                repeat(remaining) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SelectedDayCard(
    dayNumber: Int,
    month: Int,
    checkin: DailyMoodCheckin?,
    onCheckinClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mood = checkin?.mood ?: JournalMoodType.BINH_YEN
    val moodColor = mood.color

    DiuCard(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = moodColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp),
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DiuMoodDot(color = moodColor, size = 12.dp)
                Text(
                    text = "$dayNumber tháng $month · ${mood.displayName}",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.ink,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (checkin != null && !checkin.privateNote.isNullOrBlank()) {
                Text(
                    text = checkin.privateNote,
                    style = AppTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = AppTheme.colors.inkSecondary,
                )
            } else {
                Text(
                    text = "Chưa có ghi chú riêng cho ngày này. Chạm để thêm cảm xúc.",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.inkSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onCheckinClick() },
                )
            }
        }
    }
}

@Composable
private fun MoodBreakdownBars(
    breakdown: MonthMoodBreakdown,
    modifier: Modifier = Modifier,
) {
    DiuCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            breakdown.stats.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stat.mood.displayName,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.ink,
                        modifier = Modifier.width(68.dp),
                    )

                    // Multi-segment progress bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppTheme.colors.line),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(stat.percentage.coerceIn(0f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(stat.mood.color),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${stat.count} ngày",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.inkSecondary,
                        modifier = Modifier.width(52.dp),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodComparisonCard(
    comparison: MoodComparison,
    modifier: Modifier = Modifier,
) {
    DiuCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // My strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Bạn",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.inkSecondary,
                    modifier = Modifier.width(44.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    comparison.myMoods.take(14).forEach { mood ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(mood.color),
                        )
                    }
                }
            }

            // Partner strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = comparison.partnerName,
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.inkSecondary,
                    modifier = Modifier.width(44.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    comparison.partnerMoods.take(14).forEach { mood ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(mood.color),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comparison.summaryMessage,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.ink,
            )
        }
    }
}
