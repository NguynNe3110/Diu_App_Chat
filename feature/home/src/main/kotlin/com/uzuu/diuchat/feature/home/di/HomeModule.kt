package com.uzuu.diuchat.feature.home.di

import com.uzuu.diuchat.feature.home.data.dataModule
import com.uzuu.diuchat.feature.home.domain.domainModule
import com.uzuu.diuchat.feature.home.presentation.presentationModule
import org.koin.dsl.module

val featureHomeModule =
    module {
        includes(dataModule, domainModule, presentationModule)
    }
