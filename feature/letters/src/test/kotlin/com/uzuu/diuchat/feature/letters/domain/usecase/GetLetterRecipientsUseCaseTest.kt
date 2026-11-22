package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetLetterRecipientsUseCaseTest {
    private val lettersRepository: LettersRepository = mockk()
    private val sut = GetLetterRecipientsUseCase(lettersRepository)

    @Test
    fun `invoke returns recipients flow from repository`() = runTest {
        val expected = listOf("u1" to "Minh", "u2" to "Linh")
        every { lettersRepository.getRecipients() } returns flowOf(expected)

        val result = sut().first()

        result shouldBeEqualTo expected
    }
}
