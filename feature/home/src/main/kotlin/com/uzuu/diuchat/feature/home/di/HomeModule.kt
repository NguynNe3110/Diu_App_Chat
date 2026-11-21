package com.uzuu.diuchat.feature.home.di

import com.uzuu.diuchat.feature.home.featureHomeModules
import org.koin.dsl.module

val featureHomeModule =
    module {
        includes(featureHomeModules)
    }
