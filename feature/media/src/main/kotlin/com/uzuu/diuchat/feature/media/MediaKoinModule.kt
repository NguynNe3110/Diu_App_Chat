package com.uzuu.diuchat.feature.media

import com.uzuu.diuchat.feature.media.data.repository.MediaRepositoryImpl
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import com.uzuu.diuchat.feature.media.domain.usecase.ClearCacheUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetStorageStatsUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetVaultMediaUseCase
import com.uzuu.diuchat.feature.media.presentation.storage.StorageViewModel
import com.uzuu.diuchat.feature.media.presentation.vault.MediaVaultViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureMediaModule = module {
    singleOf(::MediaRepositoryImpl) { bind<MediaRepository>() }

    singleOf(::GetVaultMediaUseCase)
    singleOf(::GetStorageStatsUseCase)
    singleOf(::ClearCacheUseCase)

    viewModelOf(::MediaVaultViewModel)
    viewModelOf(::StorageViewModel)
}
