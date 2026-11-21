package com.uzuu.diuchat.feature.auth.presentation

import com.uzuu.diuchat.feature.auth.presentation.screen.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::AuthViewModel)
    }
