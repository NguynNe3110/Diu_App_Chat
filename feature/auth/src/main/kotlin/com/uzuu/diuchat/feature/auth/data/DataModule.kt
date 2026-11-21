package com.uzuu.diuchat.feature.auth.data

import com.uzuu.diuchat.feature.auth.data.repository.AuthRepositoryImpl
import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import org.koin.dsl.module

internal val dataModule =
    module {
        single<AuthRepository> {
            AuthRepositoryImpl(
                supabaseClient = getOrNull(),
            )
        }
    }
