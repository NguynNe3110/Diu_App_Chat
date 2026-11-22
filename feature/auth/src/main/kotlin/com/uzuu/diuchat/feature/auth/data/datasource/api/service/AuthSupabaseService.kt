package com.uzuu.diuchat.feature.auth.data.datasource.api.service

import com.uzuu.diuchat.feature.auth.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.auth.data.datasource.api.response.UserProfileResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

class AuthSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    suspend fun sendOtp(phoneNumber: String): Result<Unit> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.auth.signInWith(OTP) {
                phone = phoneNumber
            }
        }.onFailure(::rethrowCancellation)
    }

    suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
    ): Result<Unit> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.auth.verifyPhoneOtp(
                type = OtpType.Phone.SMS,
                phone = phoneNumber,
                token = code,
            )
        }.onFailure(::rethrowCancellation)
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }
        }.onFailure(::rethrowCancellation)
    }

    suspend fun fetchCurrentUserProfile(): Result<UserProfileResponse> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            val userId =
                client.auth.currentUserOrNull()?.id
                    ?: error("Chưa có phiên đăng nhập.")
            val profile =
                client.postgrest
                    .from(USER_PROFILES_TABLE)
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }.decodeSingleOrNull<UserProfileDto>()
            UserProfileResponse(profile = profile)
        }.onFailure(::rethrowCancellation)
    }

    suspend fun claimUsername(
        username: String,
        displayName: String?,
    ): Result<Unit> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            val user =
                client.auth.currentUserOrNull()
                    ?: error("Chưa có phiên đăng nhập.")
            val resolvedName =
                displayName?.takeIf { it.isNotBlank() }
                    ?: (user.userMetadata?.get("full_name") as? JsonPrimitive)?.contentOrNull
                    ?: (user.userMetadata?.get("name") as? JsonPrimitive)?.contentOrNull
                    ?: DEFAULT_DISPLAY_NAME
            client.postgrest.rpc(
                function = BOOTSTRAP_PROFILE_RPC,
                parameters =
                buildJsonObject {
                    put("p_username", username)
                    put("p_display_name", resolvedName)
                },
            )
        }.map {}.onFailure(::rethrowCancellation)
    }

    private fun <T> missingClientFailure(): Result<T> = Result.failure(IllegalStateException("SupabaseClient chưa được khởi tạo."))

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    private companion object {
        private const val USER_PROFILES_TABLE = "user_profiles"
        private const val BOOTSTRAP_PROFILE_RPC = "bootstrap_profile"
        private const val DEFAULT_DISPLAY_NAME = "Người thương"
    }
}
