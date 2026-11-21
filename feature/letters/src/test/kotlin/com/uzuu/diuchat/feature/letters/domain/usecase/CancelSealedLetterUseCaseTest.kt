package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CancelSealedLetterUseCaseTest {

    private val lettersRepository: LettersRepository = mockk(relaxed = true)

    val sut = CancelSealedLetterUseCase(lettersRepository)

    @Test
    fun `cancelSealedLetter delegates to repository`() {
        runBlocking {
            coEvery { lettersRepository.cancelSealedLetter("l_1") } returns Unit

            sut("l_1")

            coVerify { lettersRepository.cancelSealedLetter("l_1") }
        }
    }
}
