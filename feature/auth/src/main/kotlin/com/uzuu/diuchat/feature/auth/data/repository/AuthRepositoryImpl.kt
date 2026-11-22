package com.uzuu.diuchat.feature.auth.data.repository

import com.uzuu.diuchat.feature.auth.data.datasource.api.service.AuthSupabaseService
import com.uzuu.diuchat.feature.auth.data.mapper.AuthMapper
import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import com.uzuu.diuchat.feature.base.domain.repository.NoOpPushRegistration
import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import kotlinx.coroutines.CancellationException

private const val OTP_DIGITS = 6
private const val MIN_PHONE_DIGITS = 9
private val USERNAME_REGEX = Regex("^[a-z0-9_]{3,30}$")

class AuthRepositoryImpl(
    private val supabaseService: AuthSupabaseService,
    private val authMapper: AuthMapper,
    private val pushRegistration: PushRegistration = NoOpPushRegistration,
) : AuthRepository {
    override suspend fun sendOtp(phoneNumber: String): Result<Unit> = runCatching {
        val normalizedPhone = normalizePhone(phoneNumber)
        supabaseService.sendOtp(normalizedPhone).getOrThrow()
    }.onFailure(::rethrowCancellation)

    override suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
    ): Result<Unit> = runCatching {
        val normalizedPhone = normalizePhone(phoneNumber)
        require(code.length == OTP_DIGITS && code.all { it.isDigit() }) {
            "Mã OTP phải gồm 6 chữ số"
        }
        supabaseService.verifyOtp(normalizedPhone, code).getOrThrow()
    }.onFailure(::rethrowCancellation)

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        require(idToken.isNotBlank()) { "Google ID Token không được để trống" }
        supabaseService.signInWithGoogle(idToken).getOrThrow()
    }.onFailure(::rethrowCancellation)

    override suspend fun checkHasUsername(): Result<Boolean> = runCatching {
        val response = supabaseService.fetchCurrentUserProfile().getOrThrow()
        val hasUsername = authMapper.hasUsername(response.profile)
        if (hasUsername) {
            pushRegistration.requestSync()
        }
        hasUsername
    }.onFailure(::rethrowCancellation)

    override suspend fun claimUsername(
        username: String,
        displayName: String?,
    ): Result<Unit> = runCatching {
        val canonical = username.trim().removePrefix("@").lowercase()
        require(canonical.matches(USERNAME_REGEX)) {
            "Username phải từ 3 đến 30 ký tự, chỉ gồm chữ thường (a-z), số (0-9) và dấu gạch dưới (_)"
        }
        supabaseService.claimUsername(canonical, displayName).getOrThrow()
        pushRegistration.requestSync()
    }.onFailure(::rethrowCancellation)

    private fun normalizePhone(phoneNumber: String): String {
        val trimmed = phoneNumber.trim()
        val normalized =
            if (trimmed.startsWith("+84")) {
                trimmed
            } else {
                "+84${trimmed.trimStart('0')}"
            }
        val digitsAfterPrefix = normalized.removePrefix("+84")
        require(digitsAfterPrefix.length >= MIN_PHONE_DIGITS && digitsAfterPrefix.all { it.isDigit() }) {
            "Số điện thoại không hợp lệ"
        }
        return normalized
    }

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }
}
