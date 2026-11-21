package com.uzuu.diuchat.feature.auth.data.datasource.api.service

import com.uzuu.diuchat.feature.auth.data.datasource.api.response.UserProfileResponse

interface AuthSupabaseService {
    suspend fun sendOtp(phoneNumber: String): Result<Unit>

    suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
    ): Result<Unit>

    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    suspend fun fetchCurrentUserProfile(): Result<UserProfileResponse>

    suspend fun claimUsername(
        username: String,
        displayName: String?,
    ): Result<Unit>
}
