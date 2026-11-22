package com.uzuu.diuchat.feature.letters.presentation

import com.uzuu.diuchat.feature.letters.presentation.screen.LettersViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        LettersViewModel(
            cancelSealedLetterUseCase = get(),
            getLetterRecipientsUseCase = get(),
            getLettersUseCase = get(),
            openSealedLetterUseCase = get(),
            sendLetterImmediatelyUseCase = get(),
            sendSealedLetterUseCase = get(),
            updateSealedLetterUseCase = get(),
        )
    }
}
