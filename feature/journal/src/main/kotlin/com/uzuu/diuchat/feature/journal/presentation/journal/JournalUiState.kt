package com.uzuu.diuchat.feature.journal.presentation.journal

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.MonthMoodBreakdown
import com.uzuu.diuchat.feature.journal.domain.model.MoodComparison
import com.uzuu.diuchat.feature.journal.domain.model.MoodStat

data class JournalUiState(
    val isLoading: Boolean = false,
    val month: Int = 11,
    val year: Int = 2026,
    val checkins: List<DailyMoodCheckin> = emptyList(),
    val selectedDay: Int = 24,
    val comparison: MoodComparison? = null,
    val breakdown: MonthMoodBreakdown? = null,
    val isCheckinSheetVisible: Boolean = false,
    val checkinSelectedMood: JournalMoodType? = null,
    val checkinNote: String = "",
    val checkinAllowlist: Set<String> = setOf("minh", "me", "thao"),
    val availableContacts: List<Pair<String, String>> = listOf(
        "minh" to "Minh",
        "me" to "Mẹ",
        "thao" to "Thảo",
    ),
    val isSavingCheckin: Boolean = false,
    val errorMessage: String? = null,
) : BaseState {
    val selectedCheckin: DailyMoodCheckin?
        get() = checkins.find { it.dayNumber == selectedDay }
}

fun calculateBreakdown(
    monthName: String,
    checkins: List<DailyMoodCheckin>,
    totalDays: Int = 30,
): MonthMoodBreakdown {
    val counts = checkins.groupingBy { it.mood }.eachCount()
    val stats = JournalMoodType.entries.map { mood ->
        val count = counts[mood] ?: 0
        val percentage = if (checkins.isNotEmpty()) count.toFloat() / checkins.size else 0f
        MoodStat(mood = mood, count = count, percentage = percentage)
    }
    return MonthMoodBreakdown(
        monthName = monthName,
        totalDays = totalDays,
        stats = stats,
    )
}
