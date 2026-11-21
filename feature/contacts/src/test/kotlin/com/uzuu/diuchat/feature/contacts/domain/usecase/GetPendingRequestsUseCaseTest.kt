package com.uzuu.diuchat.feature.contacts.domain.usecase

import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetPendingRequestsUseCaseTest {
    private val mockContactsRepository: ContactsRepository = mockk()

    private val sut = GetPendingRequestsUseCase(mockContactsRepository)

    @Test
    fun `returns pending requests flow from repository`() {
        runBlocking {
            val expected =
                listOf(
                    ConnectionRequest(
                        id = "req_1",
                        senderId = "user_2",
                        senderName = "Hoàng",
                        senderInitials = "H",
                        senderPhone = "0933 555 777",
                        senderMoodColorHex = "#A6C5A2",
                        proposedCircle = ContactCircle.FRIENDS,
                        createdAt = "Vừa xong",
                    ),
                )
            every { mockContactsRepository.getPendingRequests() } returns flowOf(expected)

            val actual = sut().first()

            actual shouldBeEqualTo expected
        }
    }
}
