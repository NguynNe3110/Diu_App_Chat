package com.uzuu.diuchat.feature.auth.data

import com.uzuu.diuchat.feature.auth.data.repository.AuthRepositoryImpl
import com.uzuu.diuchat.feature.auth.data.repository.MockAuthRepository
import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module

internal val dataModule =
    module {
        single<AuthRepository> {
            val supabaseClient = getOrNull<SupabaseClient>()
            if (supabaseClient != null) {
                AuthRepositoryImpl(supabaseClient = supabaseClient)
            } else {
                MockAuthRepository()
            }
        }
    }
