package com.uzuu.diuchat.feature.contacts.data.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_requests")
data class ConnectionRequestEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderInitials: String,
    val senderPhone: String,
    val senderMoodColorHex: String,
    val proposedCircle: String,
    val createdAt: String,
)
