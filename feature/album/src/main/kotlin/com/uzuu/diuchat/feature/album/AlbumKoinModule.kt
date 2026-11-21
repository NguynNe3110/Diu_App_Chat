package com.uzuu.diuchat.feature.album

import com.uzuu.diuchat.feature.album.data.dataModule
import com.uzuu.diuchat.feature.album.domain.domainModule
import com.uzuu.diuchat.feature.album.presentation.presentationModule

val featureAlbumModules =
    listOf(
        presentationModule,
        domainModule,
        dataModule,
    )
