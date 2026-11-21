package com.uzuu.diuchat.feature.journal.data

import com.uzuu.diuchat.feature.journal.data.datasource.api.service.JournalSupabaseService
import com.uzuu.diuchat.feature.journal.data.mapper.JournalMapper
import com.uzuu.diuchat.feature.journal.data.repository.JournalRepositoryImpl
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import org.koin.dsl.module

val dataModule = module {
    single<JournalSupabaseService> {
        JournalSupabaseService(supabaseClient = getOrNull())
    }
    single { JournalMapper() }
    single<JournalRepository> {
        JournalRepositoryImpl(
            supabaseService = get(),
            mapper = get(),
        )
    }
}
