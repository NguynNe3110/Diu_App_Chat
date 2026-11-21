package com.uzuu.diuchat.feature.home.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::HomeViewModel)
    }
