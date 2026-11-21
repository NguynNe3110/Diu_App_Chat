package com.uzuu.diuchat.feature.auth

import com.uzuu.diuchat.feature.auth.data.dataModule
import com.uzuu.diuchat.feature.auth.domain.domainModule
import com.uzuu.diuchat.feature.auth.presentation.presentationModule

val featureAuthModules =
    listOf(
        presentationModule,
        domainModule,
        dataModule,
    )
