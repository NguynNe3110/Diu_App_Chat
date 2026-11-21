package com.uzuu.diuchat.feature.journal.domain.model

data class SearchResultItem(
    val id: String,
    val conversationId: String,
    val conversationName: String,
    val senderName: String,
    val snippet: String,
    val timestamp: String,
    val mood: JournalMoodType? = null,
    val mediaType: String? = null,
    val isDirect: Boolean = true,
)
