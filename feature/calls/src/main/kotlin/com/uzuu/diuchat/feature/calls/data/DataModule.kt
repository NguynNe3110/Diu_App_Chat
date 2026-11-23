package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.data.repository.CallRepositoryImpl
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import org.koin.dsl.module

val dataModule = module {
    single<WebRtcSignalingAdapter> { WebRtcSignalingAdapterImpl(supabaseClient = getOrNull()) }
    single<CallServiceCoordinator> {
        CallServiceCoordinatorImpl(
            context = getOrNull(),
            foregroundServiceController = getOrNull(),
        )
    }
    single { CallTokenService(supabaseClient = getOrNull()) }
    single { CallSupabaseService(supabaseClient = getOrNull()) }
    single<CallMediaEngine> { LiveKitCallMediaEngine(context = getOrNull()) }
    single<CallRepository> {
        CallRepositoryImpl(
            callServiceCoordinator = get(),
            webRtcSignalingAdapter = get(),
            callTokenService = get(),
            callMediaEngine = get(),
            callSupabaseService = get(),
            appSoundPlayer = getOrNull(),
        )
    }
}
