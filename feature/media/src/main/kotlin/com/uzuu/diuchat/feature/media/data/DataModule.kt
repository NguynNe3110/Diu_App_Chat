package com.uzuu.diuchat.feature.media.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.uzuu.diuchat.feature.media.data.datasource.api.service.MediaSupabaseService
import com.uzuu.diuchat.feature.media.data.datasource.preferences.MediaPreferencesDataSource
import com.uzuu.diuchat.feature.media.data.mapper.MediaMapper
import com.uzuu.diuchat.feature.media.data.repository.MediaRepositoryImpl
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private val Context.mediaDataStore: DataStore<Preferences> by preferencesDataStore(name = "diu_media_preferences")

val dataModule = module {
    single<DataStore<Preferences>> {
        androidContext().mediaDataStore
    }
    single { MediaPreferencesDataSource(get()) }
    single<MediaSupabaseService> {
        MediaSupabaseService(
            supabaseClient = getOrNull<SupabaseClient>(),
        )
    }
    singleOf(::MediaMapper)
    single<MediaRepository> {
        MediaRepositoryImpl(
            context = androidContext(),
            preferencesDataSource = get(),
            mediaMapper = get(),
            supabaseService = get(),
        )
    }
}
