package com.uzuu.diuchat.feature.contacts.domain.usecase

import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetContactsUseCaseTest {

    private val mockContactsRepository: ContactsRepository = mockk()

    private val sut = GetContactsUseCase(mockContactsRepository)

    @Test
    fun `returns contacts flow from repository`() {
        runBlocking {
            val expected = listOf(
                Contact(
                    id = "1",
                    name = "Minh An",
                    initials = "MA",
                    phone = "0912 345 678",
                    circle = ContactCircle.LOVE,
                    moodColorHex = "#F5A894",
                ),
            )
            every { mockContactsRepository.getContacts() } returns flowOf(expected)

            val actual = sut().first()

            actual shouldBeEqualTo expected
        }
    }
}
