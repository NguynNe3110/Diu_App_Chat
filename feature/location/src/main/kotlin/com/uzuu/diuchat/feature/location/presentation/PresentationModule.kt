package com.uzuu.diuchat.feature.location.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::LocationPickerViewModel)
    viewModelOf(::LiveLocationViewModel)
}
