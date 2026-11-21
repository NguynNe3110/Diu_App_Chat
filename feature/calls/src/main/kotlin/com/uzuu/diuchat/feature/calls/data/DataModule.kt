package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.data.repository.CallRepositoryImpl
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import org.koin.dsl.module

val dataModule = module {
    single<WebRtcSignalingAdapter> { WebRtcSignalingAdapterImpl() }
    single<CallServiceCoordinator> { CallServiceCoordinatorImpl() }
    single<CallRepository> {
        CallRepositoryImpl(
            callServiceCoordinator = get(),
            webRtcSignalingAdapter = get(),
        )
    }
}
