package com.uzuu.diuchat.feature.auth.data.repository

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

private const val PREVIEW_DELAY_MS = 150L
private const val OTP_DIGITS = 6

class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient? = null,
) : AuthRepository {
    override suspend fun sendOtp(phoneNumber: String): Result<Unit> = runCatching {
        val normalizedPhone = normalizePhone(phoneNumber)
        val client = supabaseClient
        if (client != null) {
            client.auth.signInWith(OTP) {
                phone = normalizedPhone
            }
        } else {
            delay(PREVIEW_DELAY_MS)
        }
    }.onFailure(::rethrowCancellation)

    override suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
    ): Result<Unit> = runCatching {
        require(code.length == OTP_DIGITS && code.all { it.isDigit() }) {
            "Mã OTP phải gồm 6 chữ số"
        }
        val client = supabaseClient
        if (client != null) {
            client.auth.verifyPhoneOtp(
                type = OtpType.Phone.SMS,
                phone = normalizePhone(phoneNumber),
                token = code,
            )
        } else {
            delay(PREVIEW_DELAY_MS)
        }
    }.onFailure(::rethrowCancellation)

    private fun normalizePhone(phoneNumber: String): String = if (phoneNumber.startsWith("+84")) {
        phoneNumber
    } else {
        "+84${phoneNumber.trimStart('0')}"
    }

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }
}
