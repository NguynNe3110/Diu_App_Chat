package com.uzuu.diuchat.feature.calls

import com.uzuu.diuchat.feature.calls.data.dataModule
import com.uzuu.diuchat.feature.calls.domain.domainModule
import com.uzuu.diuchat.feature.calls.presentation.presentationModule

val featureCallsModules = listOf(
    dataModule,
    domainModule,
    presentationModule,
)
