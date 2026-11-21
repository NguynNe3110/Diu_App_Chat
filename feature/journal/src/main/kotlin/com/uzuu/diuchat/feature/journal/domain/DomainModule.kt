package com.uzuu.diuchat.feature.journal.domain

import com.uzuu.diuchat.feature.journal.domain.usecase.GetFavouritesUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.GetMonthlyMoodCheckinsUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.GetMoodComparisonUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.SaveMoodCheckinUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.SearchMessagesUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.ToggleFavouriteUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetMonthlyMoodCheckinsUseCase)
    factoryOf(::SaveMoodCheckinUseCase)
    factoryOf(::GetMoodComparisonUseCase)
    factoryOf(::SearchMessagesUseCase)
    factoryOf(::GetFavouritesUseCase)
    factoryOf(::ToggleFavouriteUseCase)
}
