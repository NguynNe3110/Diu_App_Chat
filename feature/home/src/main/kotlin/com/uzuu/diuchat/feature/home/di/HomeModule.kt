package com.uzuu.diuchat.feature.home.di

import com.uzuu.diuchat.feature.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule =
    module {
        viewModelOf(::HomeViewModel)
    }
