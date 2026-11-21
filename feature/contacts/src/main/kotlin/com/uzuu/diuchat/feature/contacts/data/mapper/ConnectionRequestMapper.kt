package com.uzuu.diuchat.feature.contacts.data.mapper

import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ConnectionRequestDto
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ConnectionRequestEntity
import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle

class ConnectionRequestMapper {
    fun toEntity(dto: ConnectionRequestDto): ConnectionRequestEntity = ConnectionRequestEntity(
        id = dto.id,
        senderId = dto.senderId,
        senderName = dto.senderName,
        senderInitials = dto.senderInitials,
        senderPhone = dto.senderPhone,
        senderMoodColorHex = dto.senderMoodColorHex,
        proposedCircle = dto.proposedCircle,
        createdAt = dto.createdAt,
    )

    fun toDto(entity: ConnectionRequestEntity): ConnectionRequestDto = ConnectionRequestDto(
        id = entity.id,
        senderId = entity.senderId,
        senderName = entity.senderName,
        senderInitials = entity.senderInitials,
        senderPhone = entity.senderPhone,
        senderMoodColorHex = entity.senderMoodColorHex,
        proposedCircle = entity.proposedCircle,
        createdAt = entity.createdAt,
    )

    fun toDomain(entity: ConnectionRequestEntity): ConnectionRequest = ConnectionRequest(
        id = entity.id,
        senderId = entity.senderId,
        senderName = entity.senderName,
        senderInitials = entity.senderInitials,
        senderPhone = entity.senderPhone,
        senderMoodColorHex = entity.senderMoodColorHex,
        proposedCircle = ContactCircle.entries.find { it.name == entity.proposedCircle } ?: ContactCircle.FRIENDS,
        createdAt = entity.createdAt,
    )

    fun toDomain(dto: ConnectionRequestDto): ConnectionRequest = toDomain(toEntity(dto))

    fun toEntity(domain: ConnectionRequest): ConnectionRequestEntity = ConnectionRequestEntity(
        id = domain.id,
        senderId = domain.senderId,
        senderName = domain.senderName,
        senderInitials = domain.senderInitials,
        senderPhone = domain.senderPhone,
        senderMoodColorHex = domain.senderMoodColorHex,
        proposedCircle = domain.proposedCircle.name,
        createdAt = domain.createdAt,
    )
}
