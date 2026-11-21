package com.uzuu.diuchat.feature.home

import com.uzuu.diuchat.feature.home.data.dataModule
import com.uzuu.diuchat.feature.home.domain.domainModule
import com.uzuu.diuchat.feature.home.presentation.presentationModule

val featureHomeModules =
    listOf(
        dataModule,
        domainModule,
        presentationModule,
    )
