package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class SendLetterImmediatelyUseCaseTest {

    private val lettersRepository: LettersRepository = mockk(relaxed = true)

    val sut = SendLetterImmediatelyUseCase(lettersRepository)

    @Test
    fun `sendLetterImmediately delegates to repository`() {
        runBlocking {
            coEvery { lettersRepository.sendLetterImmediately("l_3") } returns Unit

            sut("l_3")

            coVerify { lettersRepository.sendLetterImmediately("l_3") }
        }
    }
}
