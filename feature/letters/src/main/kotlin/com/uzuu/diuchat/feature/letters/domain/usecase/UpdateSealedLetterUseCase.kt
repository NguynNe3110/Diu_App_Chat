package com.uzuu.diuchat.feature.letters.domain.usecase

import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository

class UpdateSealedLetterUseCase(
    private val lettersRepository: LettersRepository,
) {
    suspend operator fun invoke(letter: SealedLetter) {
        lettersRepository.updateSealedLetter(letter)
    }
}
