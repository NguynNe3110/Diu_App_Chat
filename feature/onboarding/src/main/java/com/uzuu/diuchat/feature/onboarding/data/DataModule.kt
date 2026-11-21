package com.uzuu.diuchat.feature.onboarding.data

import com.uzuu.diuchat.feature.onboarding.data.repository.OnBoardingRepositoryImpl
import com.uzuu.diuchat.feature.onboarding.domain.repository.OnBoardingRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule =
    module {
        single<OnBoardingRepository> { OnBoardingRepositoryImpl(androidContext()) }
    }
