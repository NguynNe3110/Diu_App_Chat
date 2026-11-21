package com.uzuu.diuchat.feature.media

import com.uzuu.diuchat.feature.media.data.dataModule
import com.uzuu.diuchat.feature.media.domain.domainModule
import com.uzuu.diuchat.feature.media.presentation.presentationModule

val featureMediaModule = listOf(
    dataModule,
    domainModule,
    presentationModule,
)
