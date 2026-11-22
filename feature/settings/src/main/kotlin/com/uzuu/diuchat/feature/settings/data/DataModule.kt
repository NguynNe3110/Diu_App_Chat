package com.uzuu.diuchat.feature.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import com.uzuu.diuchat.feature.base.domain.repository.QuietHoursObserver
import com.uzuu.diuchat.feature.settings.data.datasource.api.SettingRemoteDataSource
import com.uzuu.diuchat.feature.settings.data.datasource.api.SettingRemoteDataSourceImpl
import com.uzuu.diuchat.feature.settings.data.datasource.api.service.SettingsSupabaseService
import com.uzuu.diuchat.feature.settings.data.datasource.preferences.SettingsPreferencesDataSource
import com.uzuu.diuchat.feature.settings.data.mapper.UserProfileMapper
import com.uzuu.diuchat.feature.settings.data.mapper.UserSettingsMapper
import com.uzuu.diuchat.feature.settings.data.repository.SettingsQuietHoursObserver
import com.uzuu.diuchat.feature.settings.data.repository.SettingsRepositoryImpl
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "diu_user_preferences")

val dataModule = module {
    single<DataStore<Preferences>> {
        androidContext().settingsDataStore
    }
    single { SettingsPreferencesDataSource(get()) }
    single {
        SettingsSupabaseService(
            supabaseClient = getOrNull<SupabaseClient>(),
            pushRegistration = getOrNull<PushRegistration>(),
        )
    }
    single<SettingRemoteDataSource> {
        SettingRemoteDataSourceImpl(supabaseService = get())
    }
    single { UserProfileMapper() }
    single { UserSettingsMapper() }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            preferencesDataSource = get(),
            remoteDataSource = get(),
            userProfileMapper = get(),
            userSettingsMapper = get(),
        )
    }
    single<QuietHoursObserver> {
        SettingsQuietHoursObserver(settingsRepository = get())
    }
}
