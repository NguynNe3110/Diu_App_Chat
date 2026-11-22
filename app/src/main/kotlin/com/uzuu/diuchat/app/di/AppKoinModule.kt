package com.uzuu.diuchat.app.di

import com.uzuu.diuchat.app.data.supabase.supabaseModule
import com.uzuu.diuchat.app.presentation.AppSessionViewModel
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
        viewModelOf(::AppSessionViewModel)
    }
