package com.uzuu.diuchat.feature.letters.data.mapper

import com.uzuu.diuchat.feature.letters.data.datasource.api.model.LetterDto
import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class LetterMapperTest {
    private val sut = LetterMapper()

    @Test
    fun `domainToDto and dtoToDomain round trip preserves all fields`() {
        val domain = SealedLetter(
            id = "letter_99",
            senderId = "user_me",
            senderName = "Tôi",
            recipientId = "user_linh",
            recipientName = "Linh",
            title = "Thư hẹn cuối tuần",
            scheduledUnlockEpochMs = 1_800_000_000_000L,
            scheduledUnlockFormatted = "08:00 Chủ nhật",
            status = LetterStatus.OPENED,
            isSender = true,
            moodName = "Thương",
            contentPayload = "Chúc cuối tuần thật nhiều niềm vui!",
            openedAtEpochMs = 1_800_000_500_000L,
            openedAtFormatted = "Vừa xong",
        )

        val dto = sut.domainToDto(domain)
        val roundTripped = sut.dtoToDomain(dto)

        roundTripped shouldBeEqualTo domain
    }

    @Test
    fun `dtoToDomain falls back to SEALED_PENDING on unknown status`() {
        val dto = LetterDto(
            id = "letter_unknown",
            senderId = "u1",
            senderName = "A",
            recipientId = "u2",
            recipientName = "B",
            title = "Test",
            scheduledUnlockEpochMs = 1_700_000_000_000L,
            scheduledUnlockFormatted = "Mai",
            status = "UNKNOWN_STATUS",
        )

        val domain = sut.dtoToDomain(dto)

        domain.status shouldBeEqualTo LetterStatus.SEALED_PENDING
    }
}
