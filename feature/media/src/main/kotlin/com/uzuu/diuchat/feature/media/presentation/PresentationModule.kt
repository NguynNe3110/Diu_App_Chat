package com.uzuu.diuchat.feature.media.presentation

import com.uzuu.diuchat.feature.media.presentation.storage.StorageViewModel
import com.uzuu.diuchat.feature.media.presentation.vault.MediaVaultViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::MediaVaultViewModel)
    viewModelOf(::StorageViewModel)
}
