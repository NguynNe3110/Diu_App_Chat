package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import kotlinx.coroutines.flow.Flow

class GetLetterRecipientsUseCase(
    private val lettersRepository: LettersRepository,
) {
    operator fun invoke(): Flow<List<Pair<String, String>>> = lettersRepository.getRecipients()
}
