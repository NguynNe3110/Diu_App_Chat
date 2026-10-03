package com.igorwojda.showcase.app.di

import com.igorwojda.showcase.app.data.retrofit.retrofitModule
import com.igorwojda.showcase.app.data.supabase.supabaseModule
import org.koin.dsl.module

/**
 * Root Koin module for the app.
 * Aggregates all infrastructure modules (Retrofit, Supabase, etc.)
 *
 * Feature modules are loaded separately in [com.igorwojda.showcase.app.ShowcaseApplication].
 */
val appModule =
    module {
        includes(retrofitModule)
        includes(supabaseModule)
    }
