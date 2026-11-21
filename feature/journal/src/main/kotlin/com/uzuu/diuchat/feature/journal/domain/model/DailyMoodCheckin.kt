package com.uzuu.diuchat.feature.journal.domain.model

data class DailyMoodCheckin(
    val dateIso: String,
    val dayNumber: Int,
    val mood: JournalMoodType,
    val privateNote: String? = null,
    val sharedWithContactIds: List<String> = emptyList(),
    val displayDate: String = "",
)
