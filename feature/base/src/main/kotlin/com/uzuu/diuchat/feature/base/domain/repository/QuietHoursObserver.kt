package com.uzuu.diuchat.feature.base.domain.repository

import kotlinx.coroutines.flow.Flow

data class QuietHoursStatus(
    val isActive: Boolean = false,
    val bannerText: String = "",
)

interface QuietHoursObserver {
    fun observeQuietHoursStatus(): Flow<QuietHoursStatus>
}
