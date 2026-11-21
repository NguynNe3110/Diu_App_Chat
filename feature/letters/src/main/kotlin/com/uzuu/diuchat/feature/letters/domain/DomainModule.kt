package com.uzuu.diuchat.feature.letters.domain

import com.uzuu.diuchat.feature.letters.domain.usecase.CancelSealedLetterUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.GetLettersUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.OpenSealedLetterUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.SendLetterImmediatelyUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.SendSealedLetterUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.UpdateSealedLetterUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { CancelSealedLetterUseCase(get()) }
    factory { GetLettersUseCase(get()) }
    factory { OpenSealedLetterUseCase(get()) }
    factory { SendLetterImmediatelyUseCase(get()) }
    factory { SendSealedLetterUseCase(get()) }
    factory { UpdateSealedLetterUseCase(get()) }
}
