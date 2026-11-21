package com.uzuu.diuchat.feature.location

import com.uzuu.diuchat.feature.location.data.adapter.DefaultLocationProviderAdapter
import com.uzuu.diuchat.feature.location.data.adapter.LocationProviderAdapter
import com.uzuu.diuchat.feature.location.data.repository.LocationRepositoryImpl
import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository
import com.uzuu.diuchat.feature.location.domain.usecase.GetLocationSnapshotUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.ObserveLiveLocationUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.StartLiveLocationUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.StopLiveLocationUseCase
import com.uzuu.diuchat.feature.location.presentation.LiveLocationViewModel
import com.uzuu.diuchat.feature.location.presentation.LocationPickerViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureLocationModule = module {
    singleOf(::DefaultLocationProviderAdapter) { bind<LocationProviderAdapter>() }
    singleOf(::LocationRepositoryImpl) { bind<LocationRepository>() }

    singleOf(::GetLocationSnapshotUseCase)
    singleOf(::StartLiveLocationUseCase)
    singleOf(::StopLiveLocationUseCase)
    singleOf(::ObserveLiveLocationUseCase)

    viewModelOf(::LocationPickerViewModel)
    viewModelOf(::LiveLocationViewModel)
}
