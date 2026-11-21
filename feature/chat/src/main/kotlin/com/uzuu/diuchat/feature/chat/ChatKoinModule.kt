package com.uzuu.diuchat.feature.chat

import com.uzuu.diuchat.feature.chat.data.dataModule
import com.uzuu.diuchat.feature.chat.domain.domainModule
import com.uzuu.diuchat.feature.chat.presentation.presentationModule

val featureChatModules = listOf(
    dataModule,
    domainModule,
    presentationModule,
)
