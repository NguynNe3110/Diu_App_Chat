package com.uzuu.diuchat.feature.favourite

import com.uzuu.diuchat.feature.favourite.data.dataModule
import com.uzuu.diuchat.feature.favourite.domain.domainModule
import com.uzuu.diuchat.feature.favourite.presentation.presentationModule

val featureFavouriteModules =
    listOf(
        presentationModule,
        domainModule,
        dataModule,
    )
