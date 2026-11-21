package com.uzuu.diuchat.feature.auth.domain.repository

interface AuthRepository {
    suspend fun sendOtp(phoneNumber: String): Result<Unit>

    suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
    ): Result<Unit>

    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    suspend fun checkHasUsername(): Result<Boolean>

    suspend fun claimUsername(
        username: String,
        displayName: String? = null,
    ): Result<Unit>
}
