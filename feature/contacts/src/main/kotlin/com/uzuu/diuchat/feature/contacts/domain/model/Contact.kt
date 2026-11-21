package com.uzuu.diuchat.feature.contacts.domain.model

data class Contact(
    val id: String,
    val name: String,
    val initials: String,
    val phone: String,
    val circle: ContactCircle,
    val moodColorHex: String,
    val nickname: String? = null,
    val anniversary: String? = null,
    val isQuiet: Boolean = false,
    val lastSeen: String? = null,
)
