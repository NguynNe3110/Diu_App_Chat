package com.uzuu.diuchat.feature.contacts.data

import com.uzuu.diuchat.feature.contacts.data.repository.ContactsRepositoryImpl
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import org.koin.dsl.module

internal val dataModule =
    module {
        single<ContactsRepository> {
            ContactsRepositoryImpl()
        }
    }
