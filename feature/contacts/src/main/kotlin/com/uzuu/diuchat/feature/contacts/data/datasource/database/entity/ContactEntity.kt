package com.uzuu.diuchat.feature.contacts.data.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val initials: String,
    val phone: String,
    val circle: String,
    val moodColorHex: String,
    val nickname: String? = null,
    val anniversary: String? = null,
    val isQuiet: Boolean = false,
    val lastSeen: String? = null,
)
