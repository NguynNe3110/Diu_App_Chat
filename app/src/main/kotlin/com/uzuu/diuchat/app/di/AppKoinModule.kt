package com.uzuu.diuchat.app.di

import com.uzuu.diuchat.app.data.push.PushInstallationStore
import com.uzuu.diuchat.app.data.push.SupabasePushRegistration
import com.uzuu.diuchat.app.data.supabase.SupabaseSessionChecker
import com.uzuu.diuchat.app.data.supabase.supabaseModule
import com.uzuu.diuchat.app.presentation.AppSessionViewModel
import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Root Koin module for the app.
 * Aggregates all infrastructure modules (Retrofit, Supabase, etc.)
 *
 * Feature modules are loaded separately in [com.uzuu.diuchat.app.ShowcaseApplication].
 */
val appModule =
    module {
        includes(supabaseModule)
        single {
            PushInstallationStore(androidContext())
        }
        single {
            SupabasePushRegistration(
                client = getOrNull(),
                installationStore = get(),
            )
        }
        single<PushRegistration> {
            get<SupabasePushRegistration>()
        }
        singleOf(::SupabaseSessionChecker)
        viewModelOf(::AppSessionViewModel)
    }
