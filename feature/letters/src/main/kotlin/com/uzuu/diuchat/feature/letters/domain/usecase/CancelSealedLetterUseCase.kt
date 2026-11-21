package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository

class CancelSealedLetterUseCase(
    private val lettersRepository: LettersRepository,
) {
    suspend operator fun invoke(letterId: String) {
        lettersRepository.cancelSealedLetter(letterId)
    }
}
