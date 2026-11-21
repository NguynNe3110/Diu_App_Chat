package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import kotlinx.coroutines.flow.Flow

class GetLettersUseCase(
    private val lettersRepository: LettersRepository,
) {
    operator fun invoke(): Flow<List<SealedLetter>> = lettersRepository.getLetters()
}
