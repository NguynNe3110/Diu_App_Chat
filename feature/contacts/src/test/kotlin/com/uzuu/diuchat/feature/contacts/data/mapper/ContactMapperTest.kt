package com.uzuu.diuchat.feature.contacts.data.mapper

import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ContactDto
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ContactEntity
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ContactMapperTest {
    private val mapper = ContactMapper()

    @Test
    fun `toEntity and toDto roundtrip ContactDto and ContactEntity`() {
        val dto = ContactDto(
            id = "c1",
            name = "Minh An",
            initials = "MA",
            phone = "0912345678",
            circle = "LOVE",
            moodColorHex = "#F7A3A9",
            nickname = "Anh thương",
            anniversary = "24/11",
            isQuiet = true,
            lastSeen = "Vừa xong",
        )

        val entity = mapper.toEntity(dto)
        val backToDto = mapper.toDto(entity)

        entity.id shouldBeEqualTo "c1"
        entity.circle shouldBeEqualTo "LOVE"
        entity.isQuiet shouldBeEqualTo true
        backToDto shouldBeEqualTo dto
    }

    @Test
    fun `toDomain maps ContactEntity to Contact and falls back to FRIENDS for unknown circle`() {
        val entity = ContactEntity(
            id = "c2",
            name = "Thảo",
            initials = "T",
            phone = "0900111222",
            circle = "UNKNOWN_CIRCLE",
            moodColorHex = "#A2D7AC",
        )

        val domain = mapper.toDomain(entity)

        domain.id shouldBeEqualTo "c2"
        domain.circle shouldBeEqualTo ContactCircle.FRIENDS
    }

    @Test
    fun `toEntity maps Contact domain to ContactEntity`() {
        val domain = Contact(
            id = "c3",
            name = "Mẹ",
            initials = "M",
            phone = "0903111222",
            circle = ContactCircle.FAMILY,
            moodColorHex = "#F4CA84",
            nickname = "Mẹ yêu",
            anniversary = "15/08",
            isQuiet = false,
            lastSeen = "5 phút trước",
        )

        val entity = mapper.toEntity(domain)

        entity.id shouldBeEqualTo "c3"
        entity.circle shouldBeEqualTo "FAMILY"
        entity.nickname shouldBeEqualTo "Mẹ yêu"
    }
}
