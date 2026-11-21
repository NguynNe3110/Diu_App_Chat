package com.uzuu.diuchat.feature.letters.data

import com.uzuu.diuchat.feature.letters.data.repository.LettersRepositoryImpl
import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import org.koin.dsl.module

val dataModule = module {
    single<LettersRepository> { LettersRepositoryImpl() }
}
