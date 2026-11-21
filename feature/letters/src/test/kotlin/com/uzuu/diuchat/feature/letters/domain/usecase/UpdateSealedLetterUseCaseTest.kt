package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class UpdateSealedLetterUseCaseTest {

    private val lettersRepository: LettersRepository = mockk(relaxed = true)

    val sut = UpdateSealedLetterUseCase(lettersRepository)

    @Test
    fun `updateSealedLetter delegates to repository`() {
        runBlocking {
            val letter = SealedLetter(
                id = "l_edit",
                senderId = "user_me",
                senderName = "Tôi",
                recipientId = "user_linh",
                recipientName = "Linh",
                title = "Thư đã sửa",
                scheduledUnlockEpochMs = 5000L,
                scheduledUnlockFormatted = "22:00",
                status = LetterStatus.SEALED_PENDING,
                isSender = true,
                contentPayload = "Nội dung mới",
            )
            coEvery { lettersRepository.updateSealedLetter(letter) } returns Unit

            sut(letter)

            coVerify { lettersRepository.updateSealedLetter(letter) }
        }
    }
}
