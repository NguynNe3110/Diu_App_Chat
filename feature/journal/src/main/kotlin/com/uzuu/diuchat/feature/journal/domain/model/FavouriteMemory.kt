package com.uzuu.diuchat.feature.journal.domain.model

data class FavouriteMemory(
    val id: String,
    val senderName: String,
    val mood: JournalMoodType,
    val content: String,
    val savedDate: String,
    val conversationId: String = "",
)
