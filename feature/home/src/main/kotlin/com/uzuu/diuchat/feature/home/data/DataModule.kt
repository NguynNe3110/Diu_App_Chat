package com.uzuu.diuchat.feature.home.data

import com.uzuu.diuchat.feature.home.data.datasource.api.service.HomeSupabaseService
import com.uzuu.diuchat.feature.home.data.mapper.HomeMapper
import com.uzuu.diuchat.feature.home.data.repository.HomeRepositoryImpl
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module

internal val dataModule =
    module {
        single<HomeSupabaseService> {
            HomeSupabaseService(
                supabaseClient = getOrNull<SupabaseClient>(),
            )
        }
        single { HomeMapper() }
        single<HomeRepository> {
            HomeRepositoryImpl(
                homeSupabaseService = get(),
                homeMapper = get(),
            )
        }
    }
