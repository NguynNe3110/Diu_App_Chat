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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import java.time.LocalDate
import java.time.YearMonth
import org.koin.androidx.compose.koinViewModel

@Composable
fun JournalScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSearch: () -> Unit = {},
    viewModel: JournalViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    JournalContent(
        state = state,
        onBackClick = onBackClick,
        onOpenSearch = onOpenSearch,
        onSelectDay = viewModel::onSelectDay,
        onOpenCheckinSheet = viewModel::onOpenCheckinSheet,
        onSelectCheckinMood = viewModel::onSelectCheckinMood,
        onChangeCheckinNote = viewModel::onChangeCheckinNote,
        onToggleCheckinContact = viewModel::onToggleCheckinContact,
        onSaveCheckin = viewModel::onSaveCheckin,
        onDismissCheckinSheet = viewModel::onDismissCheckinSheet,
        modifier = modifier,
    )
}

@Composable
fun JournalContent(
    state: JournalUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSearch: () -> Unit = {},
    onSelectDay: (Int) -> Unit = {},
    onOpenCheckinSheet: () -> Unit = {},
    onSelectCheckinMood: (JournalMoodType) -> Unit = {},
    onChangeCheckinNote: (String) -> Unit = {},
    onToggleCheckinContact: (String) -> Unit = {},
    onSaveCheckin: () -> Unit = {},
    onDismissCheckinSheet: () -> Unit = {},
) {
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
                actions = {
                    DiuIconButton(
                        onClick = onOpenSearch,
                        contentDescription = "Tìm kiếm",
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Search),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
            )
        },
        containerColor = Color.Transparent,
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
                        year = state.year,
                        month = state.month,
                        checkins = state.checkins,
                        selectedDay = state.selectedDay,
                        onSelectDay = onSelectDay,
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
                        onCheckinClick = onOpenCheckinSheet,
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                DiuButton(
                    onClick = onOpenCheckinSheet,
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
                onSelectMood = onSelectCheckinMood,
                onChangeNote = onChangeCheckinNote,
                onToggleContact = onToggleCheckinContact,
                onSaveClick = onSaveCheckin,
                onDismissRequest = onDismissCheckinSheet,
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    checkins: List<DailyMoodCheckin>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val checkinMap = checkins.associateBy { it.dayNumber }
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val yearMonth = YearMonth.of(year, month)
    val today = LocalDate.now()
    val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value - 1
    val daysInMonth = yearMonth.lengthOfMonth()
    val cells: List<Int?> = List(firstDayOffset) { null } + (1..daysInMonth).toList()

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

        // Month days grid aligned to real weekday of day 1
        val rows = cells.chunked(7)
        rows.forEach { weekDays ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                weekDays.forEach { day ->
                    if (day == null) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                        )
                    } else {
                        val checkin = checkinMap[day]
                        val isSelected = day == selectedDay
                        val moodColor = checkin?.mood?.color ?: Color.Transparent
                        val isFuture = yearMonth.atDay(day).isAfter(today)
                        val inkColor = AppTheme.colors.ink
                        val futureBorderColor = AppTheme.colors.inkSecondary.copy(alpha = 0.45f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .drawBehind {
                                        val radius = size.minDimension / 2f
                                        if (isSelected) {
                                            drawCircle(
                                                color = inkColor,
                                                radius = radius,
                                                style = Stroke(width = 1.5.dp.toPx()),
                                            )
                                            drawCircle(
                                                color = inkColor,
                                                radius = radius - 3.dp.toPx(),
                                                style = Stroke(width = 1.dp.toPx()),
                                            )
                                        } else if (isFuture) {
                                            drawCircle(
                                                color = futureBorderColor,
                                                radius = radius - 1.dp.toPx(),
                                                style = Stroke(
                                                    width = 1.5.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
                                                ),
                                            )
                                        }
                                    }
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            checkin != null -> moodColor.copy(alpha = 0.78f)
                                            !isFuture -> AppTheme.colors.surface.copy(alpha = 0.45f)
                                            else -> Color.Transparent
                                        },
                                    )
                                    .clickable(enabled = !isFuture) { onSelectDay(day) },
                                contentAlignment = Alignment.Center,
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
                    val myStrip = comparison.myMoods.take(14)
                    myStrip.forEach { mood ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(mood.color.copy(alpha = 0.85f)),
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
                    val partnerStrip = comparison.partnerMoods.take(14)
                    partnerStrip.forEach { mood ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(mood.color.copy(alpha = 0.85f)),
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

@Preview(showBackground = true)
@Composable
private fun JournalScreenPreview() {
    val sampleCheckins = listOf(
        DailyMoodCheckin(
            dateIso = "2026-11-20",
            dayNumber = 20,
            mood = JournalMoodType.BINH_YEN,
            privateNote = "Một buổi chiều trà ấm và nhẹ tênh.",
            displayDate = "20 tháng 11",
        ),
        DailyMoodCheckin(
            dateIso = "2026-11-22",
            dayNumber = 22,
            mood = JournalMoodType.THUONG,
            privateNote = "Gửi lời thương đến những người bên cạnh.",
            displayDate = "22 tháng 11",
        ),
        DailyMoodCheckin(
            dateIso = "2026-11-24",
            dayNumber = 24,
            mood = JournalMoodType.VUI,
            privateNote = "Ngày nắng đẹp và lòng thong thả.",
            displayDate = "24 tháng 11",
        ),
    )
    val breakdown = calculateBreakdown(
        monthName = "11",
        checkins = sampleCheckins,
        totalDays = 30,
    )
    val comparison = MoodComparison(
        partnerId = "minh",
        partnerName = "Minh",
        matchingDaysCount = 18,
        primaryMood = JournalMoodType.BINH_YEN,
        myMoods = listOf(JournalMoodType.BINH_YEN, JournalMoodType.THUONG, JournalMoodType.VUI),
        partnerMoods = listOf(JournalMoodType.BINH_YEN, JournalMoodType.BINH_YEN, JournalMoodType.THUONG),
        summaryMessage = "Bạn và Minh cùng chia sẻ sự bình yên trong phần lớn tháng này.",
    )

    AppTheme {
        JournalContent(
            state = JournalUiState(
                month = 11,
                year = 2026,
                checkins = sampleCheckins,
                selectedDay = 24,
                breakdown = breakdown,
                comparison = comparison,
            ),
            onBackClick = {},
        )
    }
}
