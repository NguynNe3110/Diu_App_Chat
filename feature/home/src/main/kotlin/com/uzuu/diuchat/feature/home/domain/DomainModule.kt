package com.uzuu.diuchat.feature.home.domain

import com.uzuu.diuchat.feature.home.domain.usecase.GetHomeStarsUseCase
import com.uzuu.diuchat.feature.home.domain.usecase.GetRecentChatsUseCase
import org.koin.dsl.module

internal val domainModule =
    module {
        factory { GetHomeStarsUseCase(get()) }
        factory { GetRecentChatsUseCase(get()) }
    }
