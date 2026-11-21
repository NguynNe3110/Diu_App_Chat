package com.uzuu.diuchat.feature.contacts.data.mapper

import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ContactDto
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ContactEntity
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle

class ContactMapper {
    fun toEntity(dto: ContactDto): ContactEntity = ContactEntity(
        id = dto.id,
        name = dto.name,
        initials = dto.initials,
        phone = dto.phone,
        circle = dto.circle,
        moodColorHex = dto.moodColorHex,
        nickname = dto.nickname,
        anniversary = dto.anniversary,
        isQuiet = dto.isQuiet,
        lastSeen = dto.lastSeen,
    )

    fun toDto(entity: ContactEntity): ContactDto = ContactDto(
        id = entity.id,
        name = entity.name,
        initials = entity.initials,
        phone = entity.phone,
        circle = entity.circle,
        moodColorHex = entity.moodColorHex,
        nickname = entity.nickname,
        anniversary = entity.anniversary,
        isQuiet = entity.isQuiet,
        lastSeen = entity.lastSeen,
    )

    fun toDomain(entity: ContactEntity): Contact = Contact(
        id = entity.id,
        name = entity.name,
        initials = entity.initials,
        phone = entity.phone,
        circle = ContactCircle.entries.find { it.name == entity.circle } ?: ContactCircle.FRIENDS,
        moodColorHex = entity.moodColorHex,
        nickname = entity.nickname,
        anniversary = entity.anniversary,
        isQuiet = entity.isQuiet,
        lastSeen = entity.lastSeen,
    )

    fun toDomain(dto: ContactDto): Contact = toDomain(toEntity(dto))

    fun toEntity(domain: Contact): ContactEntity = ContactEntity(
        id = domain.id,
        name = domain.name,
        initials = domain.initials,
        phone = domain.phone,
        circle = domain.circle.name,
        moodColorHex = domain.moodColorHex,
        nickname = domain.nickname,
        anniversary = domain.anniversary,
        isQuiet = domain.isQuiet,
        lastSeen = domain.lastSeen,
    )
}
