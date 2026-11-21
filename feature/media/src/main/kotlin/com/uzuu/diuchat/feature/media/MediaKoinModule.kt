package com.uzuu.diuchat.feature.media

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.uzuu.diuchat.feature.media.data.datasource.preferences.MediaPreferencesDataSource
import com.uzuu.diuchat.feature.media.data.repository.MediaRepositoryImpl
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import com.uzuu.diuchat.feature.media.domain.usecase.ClearCacheUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.ClearConversationMediaUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetAutoDownloadPolicyUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetStorageStatsUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetVaultMediaUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.UpdateAutoDownloadPolicyUseCase
import com.uzuu.diuchat.feature.media.presentation.storage.StorageViewModel
import com.uzuu.diuchat.feature.media.presentation.vault.MediaVaultViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val Context.mediaDataStore: DataStore<Preferences> by preferencesDataStore(name = "diu_media_preferences")

val featureMediaModule = module {
    single { MediaPreferencesDataSource(androidContext().mediaDataStore) }
    single<MediaRepository> {
        MediaRepositoryImpl(
            context = androidContext(),
            preferencesDataSource = get(),
        )
    }

    singleOf(::GetVaultMediaUseCase)
    singleOf(::GetStorageStatsUseCase)
    singleOf(::ClearCacheUseCase)
    singleOf(::ClearConversationMediaUseCase)
    singleOf(::GetAutoDownloadPolicyUseCase)
    singleOf(::UpdateAutoDownloadPolicyUseCase)

    viewModelOf(::MediaVaultViewModel)
    viewModelOf(::StorageViewModel)
}
