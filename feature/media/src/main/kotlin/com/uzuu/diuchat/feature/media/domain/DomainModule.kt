package com.uzuu.diuchat.feature.media.domain

import com.uzuu.diuchat.feature.media.domain.usecase.ClearCacheUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.ClearConversationMediaUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetAutoDownloadPolicyUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetStorageStatsUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetVaultMediaUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.UpdateAutoDownloadPolicyUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetVaultMediaUseCase(get()) }
    factory { GetStorageStatsUseCase(get()) }
    factory { ClearCacheUseCase(get()) }
    factory { ClearConversationMediaUseCase(get()) }
    factory { GetAutoDownloadPolicyUseCase(get()) }
    factory { UpdateAutoDownloadPolicyUseCase(get()) }
}
