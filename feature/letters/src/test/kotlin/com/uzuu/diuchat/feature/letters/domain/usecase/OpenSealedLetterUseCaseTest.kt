package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class OpenSealedLetterUseCaseTest {

    private val lettersRepository: LettersRepository = mockk(relaxed = true)

    val sut = OpenSealedLetterUseCase(lettersRepository)

    @Test
    fun `openSealedLetter delegates to repository`() {
        runBlocking {
            coEvery { lettersRepository.openSealedLetter("l_2") } returns Unit

            sut("l_2")

            coVerify { lettersRepository.openSealedLetter("l_2") }
        }
    }
}
