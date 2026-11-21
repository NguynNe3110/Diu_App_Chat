package com.uzuu.diuchat.feature.location.domain

import com.uzuu.diuchat.feature.location.domain.usecase.GetLocationSnapshotUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.ObserveLiveLocationUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.StartLiveLocationUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.StopLiveLocationUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetLocationSnapshotUseCase)
    factoryOf(::StartLiveLocationUseCase)
    factoryOf(::StopLiveLocationUseCase)
    factoryOf(::ObserveLiveLocationUseCase)
}
