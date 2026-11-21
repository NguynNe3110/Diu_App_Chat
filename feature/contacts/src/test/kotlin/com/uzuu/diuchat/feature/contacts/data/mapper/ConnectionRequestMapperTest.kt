package com.uzuu.diuchat.feature.contacts.data.mapper

import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ConnectionRequestDto
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ConnectionRequestEntity
import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ConnectionRequestMapperTest {
    private val mapper = ConnectionRequestMapper()

    @Test
    fun `toEntity and toDto roundtrip ConnectionRequestDto`() {
        val dto = ConnectionRequestDto(
            id = "req_1",
            senderId = "u_1",
            senderName = "Hoàng Nam",
            senderInitials = "HN",
            senderPhone = "0933555777",
            senderMoodColorHex = "#A2D7AC",
            proposedCircle = "FRIENDS",
            createdAt = "Hôm nay",
        )

        val entity = mapper.toEntity(dto)
        val backToDto = mapper.toDto(entity)

        entity.id shouldBeEqualTo "req_1"
        entity.senderId shouldBeEqualTo "u_1"
        backToDto shouldBeEqualTo dto
    }

    @Test
    fun `toDomain maps ConnectionRequestEntity and falls back to FRIENDS for unknown circle`() {
        val entity = ConnectionRequestEntity(
            id = "req_2",
            senderId = "u_2",
            senderName = "An",
            senderInitials = "A",
            senderPhone = "0911222333",
            senderMoodColorHex = "#F7A3A9",
            proposedCircle = "OTHER",
            createdAt = "Hôm qua",
        )

        val domain = mapper.toDomain(entity)

        domain.id shouldBeEqualTo "req_2"
        domain.proposedCircle shouldBeEqualTo ContactCircle.FRIENDS
    }

    @Test
    fun `toEntity maps ConnectionRequest domain to ConnectionRequestEntity`() {
        val domain = ConnectionRequest(
            id = "req_3",
            senderId = "u_3",
            senderName = "Minh",
            senderInitials = "M",
            senderPhone = "0988111222",
            senderMoodColorHex = "#F7A3A9",
            proposedCircle = ContactCircle.LOVE,
            createdAt = "10:00",
        )

        val entity = mapper.toEntity(domain)

        entity.proposedCircle shouldBeEqualTo "LOVE"
        entity.senderName shouldBeEqualTo "Minh"
    }
}
