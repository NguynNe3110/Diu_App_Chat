package com.uzuu.diuchat.feature.contacts.presentation

import com.uzuu.diuchat.feature.contacts.presentation.screen.ContactsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::ContactsViewModel)
    }
