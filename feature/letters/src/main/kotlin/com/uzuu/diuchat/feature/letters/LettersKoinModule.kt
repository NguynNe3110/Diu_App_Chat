package com.uzuu.diuchat.feature.letters

import com.uzuu.diuchat.feature.letters.data.dataModule
import com.uzuu.diuchat.feature.letters.domain.domainModule
import com.uzuu.diuchat.feature.letters.presentation.presentationModule

val featureLettersModules = listOf(
    dataModule,
    domainModule,
    presentationModule,
)
