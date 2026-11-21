package com.uzuu.diuchat.feature.auth.data.repository

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

private const val PREVIEW_DELAY_MS = 150L
private const val OTP_DIGITS = 6
private val USERNAME_REGEX = Regex("^[a-z0-9_]{3,30}$")

/**
 * Preview/offline [AuthRepository] used only when [io.github.jan.supabase.SupabaseClient]
 * is not configured in DI.
 */
class MockAuthRepository(
    initialHasUsername: Boolean = true,
) : AuthRepository {
    private var hasUsername: Boolean = initialHasUsername

    override suspend fun sendOtp(phoneNumber: String): Result<Unit> = runCatching {
        require(phoneNumber.isNotBlank()) { "Số điện thoại không được để trống" }
        delay(PREVIEW_DELAY_MS)
    }.onFailure(::rethrowCancellation)

    override suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
    ): Result<Unit> = runCatching {
        require(code.length == OTP_DIGITS && code.all { it.isDigit() }) {
            "Mã OTP phải gồm 6 chữ số"
        }
        delay(PREVIEW_DELAY_MS)
    }.onFailure(::rethrowCancellation)

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        require(idToken.isNotBlank()) { "Google ID Token không được để trống" }
        delay(PREVIEW_DELAY_MS)
    }.onFailure(::rethrowCancellation)

    override suspend fun checkHasUsername(): Result<Boolean> = Result.success(hasUsername)

    override suspend fun claimUsername(
        username: String,
        displayName: String?,
    ): Result<Unit> = runCatching {
        val canonical = username.trim().removePrefix("@").lowercase()
        require(canonical.matches(USERNAME_REGEX)) {
            "Username phải từ 3 đến 30 ký tự, chỉ gồm chữ thường (a-z), số (0-9) và dấu gạch dưới (_)"
        }
        delay(PREVIEW_DELAY_MS)
        hasUsername = true
    }.onFailure(::rethrowCancellation)

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }
}
