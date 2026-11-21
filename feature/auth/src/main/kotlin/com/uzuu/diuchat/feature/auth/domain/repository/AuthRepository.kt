package com.uzuu.diuchat.feature.auth.domain.repository

interface AuthRepository {
    suspend fun sendOtp(phoneNumber: String): Result<Unit>
    suspend fun verifyOtp(phoneNumber: String, code: String): Result<Unit>
}
