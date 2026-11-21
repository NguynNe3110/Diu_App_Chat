package com.uzuu.diuchat.feature.journal

import com.uzuu.diuchat.feature.journal.data.repository.JournalRepositoryImpl
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import com.uzuu.diuchat.feature.journal.domain.usecase.GetMonthlyMoodCheckinsUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.GetMoodComparisonUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.SaveMoodCheckinUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.SearchMessagesUseCase
import com.uzuu.diuchat.feature.journal.presentation.journal.JournalViewModel
import com.uzuu.diuchat.feature.journal.presentation.search.SearchViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val journalRepositoryModule = module {
    singleOf(::JournalRepositoryImpl) bind JournalRepository::class
}

val journalDomainModule = module {
    factoryOf(::GetMonthlyMoodCheckinsUseCase)
    factoryOf(::SaveMoodCheckinUseCase)
    factoryOf(::GetMoodComparisonUseCase)
    factoryOf(::SearchMessagesUseCase)
}

val journalPresentationModule = module {
    viewModelOf(::JournalViewModel)
    viewModelOf(::SearchViewModel)
}

val featureJournalModules = listOf(
    journalRepositoryModule,
    journalDomainModule,
    journalPresentationModule,
)
