package com.uzuu.diuchat.feature.contacts

import com.uzuu.diuchat.feature.contacts.data.dataModule
import com.uzuu.diuchat.feature.contacts.domain.domainModule
import com.uzuu.diuchat.feature.contacts.presentation.presentationModule

val featureContactsModules =
    listOf(
        presentationModule,
        domainModule,
        dataModule,
    )
