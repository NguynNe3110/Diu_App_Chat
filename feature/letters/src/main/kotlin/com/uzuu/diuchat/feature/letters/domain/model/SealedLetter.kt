package com.uzuu.diuchat.feature.letters.domain.model

data class SealedLetter(
    val id: String,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val recipientName: String,
    val title: String,
    val scheduledUnlockEpochMs: Long,
    val scheduledUnlockFormatted: String,
    val status: LetterStatus,
    val isSender: Boolean,
    val moodName: String = "Thương",
    val contentPayload: String = "",
    val openedAtEpochMs: Long? = null,
    val openedAtFormatted: String? = null,
)
