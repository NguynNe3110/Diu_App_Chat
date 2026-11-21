package com.uzuu.diuchat.feature.onboarding.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.uzuu.diuchat.feature.onboarding.data.datasource.preferences.OnboardingPreferencesDataSource
import com.uzuu.diuchat.feature.onboarding.data.repository.OnboardingRepositoryImpl
import com.uzuu.diuchat.feature.onboarding.domain.repository.OnboardingRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "diu_onboarding_preferences",
)
private val ONBOARDING_DATASTORE_QUALIFIER = named("onboarding_preferences")

val dataModule =
    module {
        single<DataStore<Preferences>>(ONBOARDING_DATASTORE_QUALIFIER) {
            androidContext().onboardingDataStore
        }
        single {
            OnboardingPreferencesDataSource(
                dataStore = get(ONBOARDING_DATASTORE_QUALIFIER),
            )
        }
        single<OnboardingRepository> {
            OnboardingRepositoryImpl(
                preferencesDataSource = get(),
            )
        }
    }
