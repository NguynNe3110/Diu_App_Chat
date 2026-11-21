package com.uzuu.diuchat.feature.contacts.domain.usecase

import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class RespondConnectionRequestUseCaseTest {

    private val mockContactsRepository: ContactsRepository = mockk()

    private val sut = RespondConnectionRequestUseCase(mockContactsRepository)

    @Test
    fun `delegates accept request to repository`() = runBlocking {
        coEvery {
            mockContactsRepository.respondToRequest("req_1", true, ContactCircle.LOVE)
        } returns Result.success(Unit)

        val actual = sut("req_1", true, ContactCircle.LOVE)

        actual shouldBeEqualTo Result.success(Unit)
        coVerify(exactly = 1) {
            mockContactsRepository.respondToRequest("req_1", true, ContactCircle.LOVE)
        }
    }

    @Test
    fun `delegates decline request to repository`() = runBlocking {
        coEvery {
            mockContactsRepository.respondToRequest("req_1", false, ContactCircle.FRIENDS)
        } returns Result.success(Unit)

        val actual = sut("req_1", false, ContactCircle.FRIENDS)

        actual shouldBeEqualTo Result.success(Unit)
        coVerify(exactly = 1) {
            mockContactsRepository.respondToRequest("req_1", false, ContactCircle.FRIENDS)
        }
    }
}
