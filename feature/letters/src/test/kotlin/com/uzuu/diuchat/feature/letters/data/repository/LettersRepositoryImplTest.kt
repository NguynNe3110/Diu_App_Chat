package com.uzuu.diuchat.feature.letters.data.repository

import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

class LettersRepositoryImplTest {
    private val sut = LettersRepositoryImpl()

    @Test
    fun `openSealedLetter before unlock time throws IllegalStateException`() = runTest {
        // letter_2 is scheduled for 5 days in the future
        val action = {
            kotlinx.coroutines.runBlocking {
                sut.openSealedLetter("letter_2")
            }
        }
        action shouldThrow IllegalStateException::class
    }

    @Test
    fun `openSealedLetter when letter is READY_TO_OPEN succeeds`() = runTest {
        // letter_3 has status READY_TO_OPEN
        sut.openSealedLetter("letter_3")

        val letters = sut.getLetters().first()
        val letter3 = letters.first { it.id == "letter_3" }
        letter3.status shouldBeEqualTo LetterStatus.OPENED
    }

    @Test
    fun `openSealedLetter after unlock time succeeds`() = runTest {
        val expiredLetter = SealedLetter(
            id = "test_expired",
            senderId = "user_linh",
            senderName = "Linh",
            recipientId = "user_me",
            recipientName = "Tôi",
            title = "Thư quá hạn mở",
            scheduledUnlockEpochMs = System.currentTimeMillis() - 10_000L,
            scheduledUnlockFormatted = "Vừa rồi",
            status = LetterStatus.SEALED_PENDING,
            isSender = false,
        )
        sut.sendSealedLetter(expiredLetter)
        sut.openSealedLetter("test_expired")

        val letters = sut.getLetters().first()
        val opened = letters.first { it.id == "test_expired" }
        opened.status shouldBeEqualTo LetterStatus.OPENED
    }

    @Test
    fun `sendLetterImmediately transitions letter to READY_TO_OPEN`() = runTest {
        sut.sendLetterImmediately("letter_1")

        val letters = sut.getLetters().first()
        val letter1 = letters.first { it.id == "letter_1" }
        letter1.status shouldBeEqualTo LetterStatus.READY_TO_OPEN
    }
}
