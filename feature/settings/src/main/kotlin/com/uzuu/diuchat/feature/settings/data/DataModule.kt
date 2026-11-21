package com.uzuu.diuchat.feature.settings.data

import com.uzuu.diuchat.feature.settings.data.repository.SettingsRepositoryImpl
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import org.koin.dsl.module

val dataModule = module {
    single<SettingsRepository> { SettingsRepositoryImpl() }
}
