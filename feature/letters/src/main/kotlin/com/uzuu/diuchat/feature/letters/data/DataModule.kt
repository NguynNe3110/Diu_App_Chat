package com.uzuu.diuchat.feature.letters.data

import com.uzuu.diuchat.feature.letters.data.datasource.api.service.LettersSupabaseService
import com.uzuu.diuchat.feature.letters.data.mapper.LetterMapper
import com.uzuu.diuchat.feature.letters.data.repository.LettersRepositoryImpl
import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single<LettersSupabaseService> {
        LettersSupabaseService(
            supabaseClient = getOrNull<SupabaseClient>(),
        )
    }
    singleOf(::LetterMapper)
    single<LettersRepository> {
        LettersRepositoryImpl(
            letterMapper = get(),
            supabaseService = get(),
        )
    }
}
