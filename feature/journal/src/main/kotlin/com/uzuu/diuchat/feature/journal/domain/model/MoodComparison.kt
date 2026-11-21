package com.uzuu.diuchat.feature.journal.domain.model

data class MoodComparison(
    val partnerId: String,
    val partnerName: String,
    val matchingDaysCount: Int,
    val primaryMood: JournalMoodType,
    val myMoods: List<JournalMoodType>,
    val partnerMoods: List<JournalMoodType>,
    val summaryMessage: String,
)
