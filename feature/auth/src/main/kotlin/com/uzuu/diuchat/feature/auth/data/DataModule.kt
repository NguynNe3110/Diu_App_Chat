package com.uzuu.diuchat.feature.auth.data

import com.uzuu.diuchat.feature.auth.data.datasource.api.service.AuthSupabaseService
import com.uzuu.diuchat.feature.auth.data.mapper.AuthMapper
import com.uzuu.diuchat.feature.auth.data.repository.AuthRepositoryImpl
import com.uzuu.diuchat.feature.auth.data.repository.MockAuthRepository
import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module

internal val dataModule =
    module {
        single<AuthSupabaseService> {
            AuthSupabaseService(supabaseClient = getOrNull())
        }
        single { AuthMapper() }
        single<AuthRepository> {
            val supabaseClient = getOrNull<SupabaseClient>()
            if (supabaseClient != null) {
                AuthRepositoryImpl(
                    supabaseService = get(),
                    authMapper = get(),
                )
            } else {
                MockAuthRepository()
            }
        }
    }
