package com.uzuu.diuchat.feature.home.data

import com.uzuu.diuchat.feature.home.data.repository.HomeRepositoryImpl
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import org.koin.dsl.module

internal val dataModule =
    module {
        single<HomeRepository> { HomeRepositoryImpl() }
    }
