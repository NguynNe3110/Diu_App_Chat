package com.uzuu.diuchat.feature.contacts.domain.model

data class ConnectionRequest(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderInitials: String,
    val senderPhone: String,
    val senderMoodColorHex: String,
    val proposedCircle: ContactCircle,
    val createdAt: String,
)
