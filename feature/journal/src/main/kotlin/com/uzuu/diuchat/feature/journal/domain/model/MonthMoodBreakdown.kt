package com.uzuu.diuchat.feature.journal.domain.model

data class MoodStat(
    val mood: JournalMoodType,
    val count: Int,
    val percentage: Float,
)

data class MonthMoodBreakdown(
    val monthName: String,
    val totalDays: Int,
    val stats: List<MoodStat>,
)
