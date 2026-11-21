package com.uzuu.diuchat.feature.location.data

import com.uzuu.diuchat.feature.location.data.adapter.DefaultLocationProviderAdapter
import com.uzuu.diuchat.feature.location.data.adapter.LocationProviderAdapter
import com.uzuu.diuchat.feature.location.data.datasource.api.service.LocationSupabaseService
import com.uzuu.diuchat.feature.location.data.datasource.api.service.LocationSupabaseServiceImpl
import com.uzuu.diuchat.feature.location.data.mapper.LocationMapper
import com.uzuu.diuchat.feature.location.data.repository.LocationRepositoryImpl
import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<LocationProviderAdapter> { DefaultLocationProviderAdapter(androidContext()) }
    single<LocationSupabaseService> {
        LocationSupabaseServiceImpl(supabaseClient = getOrNull())
    }
    single { LocationMapper() }
    single<LocationRepository> {
        LocationRepositoryImpl(
            locationProviderAdapter = get(),
            supabaseService = get(),
            mapper = get(),
        )
    }
}
