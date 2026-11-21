package com.uzuu.diuchat.feature.journal

import com.uzuu.diuchat.feature.journal.data.dataModule
import com.uzuu.diuchat.feature.journal.domain.domainModule
import com.uzuu.diuchat.feature.journal.presentation.presentationModule

val featureJournalModules = listOf(
    dataModule,
    domainModule,
    presentationModule,
)
