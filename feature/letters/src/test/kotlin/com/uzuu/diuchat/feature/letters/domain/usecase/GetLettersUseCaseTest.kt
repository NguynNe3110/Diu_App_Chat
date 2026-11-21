package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetLettersUseCaseTest {

    private val lettersRepository: LettersRepository = mockk()

    val sut = GetLettersUseCase(lettersRepository)

    @Test
    fun `getLetters delegates to repository`() {
        runBlocking {
            val expected = listOf(
                SealedLetter(
                    id = "l1",
                    senderId = "s1",
                    senderName = "Tôi",
                    recipientId = "r1",
                    recipientName = "Linh",
                    title = "Thư sáng mai",
                    scheduledUnlockEpochMs = 1000L,
                    scheduledUnlockFormatted = "08:00",
                    status = LetterStatus.SEALED_PENDING,
                    isSender = true,
                ),
            )
            every { lettersRepository.getLetters() } returns flowOf(expected)

            val actual = sut().first()

            actual shouldBeEqualTo expected
        }
    }
}
