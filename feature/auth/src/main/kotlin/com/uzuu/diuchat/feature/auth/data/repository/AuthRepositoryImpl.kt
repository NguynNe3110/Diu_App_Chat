package com.uzuu.diuchat.feature.auth.data.repository

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

private const val OTP_DIGITS = 6
private const val MIN_PHONE_DIGITS = 9
private val USERNAME_REGEX = Regex("^[a-z0-9_]{3,30}$")

@Serializable
private data class ProfileUsernameDto(
    val username: String? = null,
)

class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient,
) : AuthRepository {
    override suspend fun sendOtp(phoneNumber: String): Result<Unit> = runCatching {
        val normalizedPhone = normalizePhone(phoneNumber)
        supabaseClient.auth.signInWith(OTP) {
            phone = normalizedPhone
        }
    }.onFailure(::rethrowCancellation)

    override suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
    ): Result<Unit> = runCatching {
        val normalizedPhone = normalizePhone(phoneNumber)
        require(code.length == OTP_DIGITS && code.all { it.isDigit() }) {
            "Mã OTP phải gồm 6 chữ số"
        }
        supabaseClient.auth.verifyPhoneOtp(
            type = OtpType.Phone.SMS,
            phone = normalizedPhone,
            token = code,
        )
    }.onFailure(::rethrowCancellation)

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        require(idToken.isNotBlank()) { "Google ID Token không được để trống" }
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
    }.onFailure(::rethrowCancellation)

    override suspend fun checkHasUsername(): Result<Boolean> = runCatching {
        val userId =
            supabaseClient.auth.currentUserOrNull()?.id
                ?: error("Chưa có phiên đăng nhập.")
        val profile =
            supabaseClient.postgrest
                .from("user_profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingleOrNull<ProfileUsernameDto>()
        !profile?.username.isNullOrBlank()
    }.onFailure(::rethrowCancellation)

    override suspend fun claimUsername(
        username: String,
        displayName: String?,
    ): Result<Unit> = runCatching {
        val canonical = username.trim().removePrefix("@").lowercase()
        require(canonical.matches(USERNAME_REGEX)) {
            "Username phải từ 3 đến 30 ký tự, chỉ gồm chữ thường (a-z), số (0-9) và dấu gạch dưới (_)"
        }
        val user =
            supabaseClient.auth.currentUserOrNull()
                ?: error("Chưa có phiên đăng nhập.")
        val resolvedName =
            displayName?.takeIf { it.isNotBlank() }
                ?: (user.userMetadata?.get("full_name") as? JsonPrimitive)?.contentOrNull
                ?: (user.userMetadata?.get("name") as? JsonPrimitive)?.contentOrNull
                ?: "Người thương"
        val resolvedAvatar = (user.userMetadata?.get("avatar_url") as? JsonPrimitive)?.contentOrNull

        supabaseClient.postgrest.rpc(
            function = "bootstrap_profile",
            parameters =
            buildJsonObject {
                put("p_username", canonical)
                put("p_display_name", resolvedName)
                resolvedAvatar?.let { put("p_avatar_url", it) }
            },
        )
        Unit
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
