package com.uzuu.diuchat.feature.calls.presentation

import com.uzuu.diuchat.feature.calls.presentation.screen.CallHistoryViewModel
import com.uzuu.diuchat.feature.calls.presentation.screen.CallViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        CallViewModel(
            acceptCallUseCase = get(),
            callRepository = get(),
            endCallUseCase = get(),
            startCallUseCase = get(),
        )
    }

    viewModel {
        CallHistoryViewModel(
            getCallHistoryUseCase = get(),
            startCallUseCase = get(),
        )
    }
}
