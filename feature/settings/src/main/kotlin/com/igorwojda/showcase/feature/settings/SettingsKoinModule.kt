package com.uzuu.diuchat.feature.settings

import com.uzuu.diuchat.feature.settings.data.dataModule
import com.uzuu.diuchat.feature.settings.domain.domainModule
import com.uzuu.diuchat.feature.settings.presentation.presentationModule

val featureSettingsModules =
    listOf(
        presentationModule,
        domainModule,
        dataModule,
    )
