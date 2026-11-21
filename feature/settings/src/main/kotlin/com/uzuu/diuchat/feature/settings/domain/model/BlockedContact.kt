package com.uzuu.diuchat.feature.settings.domain.model

data class BlockedContact(
    val id: String,
    val name: String,
    val phone: String,
    val blockedAt: String,
)
