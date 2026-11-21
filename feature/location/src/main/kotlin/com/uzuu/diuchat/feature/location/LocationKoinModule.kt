package com.uzuu.diuchat.feature.location

import com.uzuu.diuchat.feature.location.data.dataModule
import com.uzuu.diuchat.feature.location.domain.domainModule
import com.uzuu.diuchat.feature.location.presentation.presentationModule
import org.koin.dsl.module

val featureLocationModule = module {
    includes(
        dataModule,
        domainModule,
        presentationModule,
    )
}
