package com.uzuu.diuchat.feature.chat.presentation

import com.uzuu.diuchat.feature.chat.presentation.screen.ChatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::ChatViewModel)
}
