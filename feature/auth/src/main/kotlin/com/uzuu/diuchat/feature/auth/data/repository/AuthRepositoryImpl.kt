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
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

private const val PREVIEW_DELAY_MS = 150L
private const val OTP_DIGITS = 6
private val USERNAME_REGEX = Regex("^[a-z0-9_]{3,30}$")

@Serializable
private data class ProfileUsernameDto(
    val username: String? = null,
)

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

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        require(idToken.isNotBlank()) { "Google ID Token không được để trống" }
        val client = supabaseClient
        if (client != null) {
            client.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }
        } else {
            delay(PREVIEW_DELAY_MS)
        }
    }.onFailure(::rethrowCancellation)

    override suspend fun checkHasUsername(): Result<Boolean> = runCatching {
        val client = supabaseClient ?: return@runCatching true
        val userId = client.auth.currentUserOrNull()?.id ?: return@runCatching false
        val profile = client.postgrest.from("user_profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingleOrNull<ProfileUsernameDto>()
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
        val client = supabaseClient
        if (client != null) {
            val user = client.auth.currentUserOrNull()
            val resolvedName = displayName?.takeIf { it.isNotBlank() }
                ?: (user?.userMetadata?.get("full_name") as? JsonPrimitive)?.contentOrNull
                ?: (user?.userMetadata?.get("name") as? JsonPrimitive)?.contentOrNull
                ?: "Người thương"
            val resolvedAvatar = (user?.userMetadata?.get("avatar_url") as? JsonPrimitive)?.contentOrNull

            client.postgrest.rpc(
                function = "bootstrap_profile",
                parameters = buildJsonObject {
                    put("p_username", canonical)
                    put("p_display_name", resolvedName)
                    resolvedAvatar?.let { put("p_avatar_url", it) }
                },
            )
        } else {
            delay(PREVIEW_DELAY_MS)
        }
        Unit
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
