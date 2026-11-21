package com.uzuu.diuchat.feature.onboarding

import com.uzuu.diuchat.feature.onboarding.data.dataModule
import com.uzuu.diuchat.feature.onboarding.domain.domainModule
import com.uzuu.diuchat.feature.onboarding.presentation.presentationModule

/** Danh sách các Koin module của feature :onboarding. */
val featureOnboardingModules =
    listOf(
        dataModule,
        domainModule,
        presentationModule,
    )
