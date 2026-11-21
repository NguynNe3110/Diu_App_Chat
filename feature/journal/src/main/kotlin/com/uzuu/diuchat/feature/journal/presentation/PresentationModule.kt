package com.uzuu.diuchat.feature.journal.presentation

import com.uzuu.diuchat.feature.journal.presentation.favourites.FavouritesViewModel
import com.uzuu.diuchat.feature.journal.presentation.journal.JournalViewModel
import com.uzuu.diuchat.feature.journal.presentation.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::JournalViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::FavouritesViewModel)
}
