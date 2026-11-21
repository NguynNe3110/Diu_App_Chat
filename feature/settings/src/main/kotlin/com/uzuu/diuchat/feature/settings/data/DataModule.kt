package com.uzuu.diuchat.feature.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.uzuu.diuchat.feature.settings.data.datasource.preferences.SettingsPreferencesDataSource
import com.uzuu.diuchat.feature.settings.data.repository.SettingsRepositoryImpl
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "diu_user_preferences")

val dataModule = module {
    single<DataStore<Preferences>> {
        androidContext().settingsDataStore
    }
    single { SettingsPreferencesDataSource(get()) }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            preferencesDataSource = get(),
        )
    }
}
