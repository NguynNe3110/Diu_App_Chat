package com.uzuu.diuchat.feature.letters.data.mapper

import com.uzuu.diuchat.feature.letters.data.datasource.api.model.LetterDto
import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter

class LetterMapper {
    fun dtoToDomain(dto: LetterDto): SealedLetter = SealedLetter(
        id = dto.id,
        senderId = dto.senderId,
        senderName = dto.senderName,
        recipientId = dto.recipientId,
        recipientName = dto.recipientName,
        title = dto.title,
        scheduledUnlockEpochMs = dto.scheduledUnlockEpochMs,
        scheduledUnlockFormatted = dto.scheduledUnlockFormatted,
        status = runCatching { LetterStatus.valueOf(dto.status.uppercase()) }.getOrDefault(LetterStatus.SEALED_PENDING),
        isSender = dto.isSender,
        moodName = dto.moodName,
        contentPayload = dto.contentPayload,
        openedAtEpochMs = dto.openedAtEpochMs,
        openedAtFormatted = dto.openedAtFormatted,
    )

    fun domainToDto(domain: SealedLetter): LetterDto = LetterDto(
        id = domain.id,
        senderId = domain.senderId,
        senderName = domain.senderName,
        recipientId = domain.recipientId,
        recipientName = domain.recipientName,
        title = domain.title,
        scheduledUnlockEpochMs = domain.scheduledUnlockEpochMs,
        scheduledUnlockFormatted = domain.scheduledUnlockFormatted,
        status = domain.status.name,
        isSender = domain.isSender,
        moodName = domain.moodName,
        contentPayload = domain.contentPayload,
        openedAtEpochMs = domain.openedAtEpochMs,
        openedAtFormatted = domain.openedAtFormatted,
    )
}
