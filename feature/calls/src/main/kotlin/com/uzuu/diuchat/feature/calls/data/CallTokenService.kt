package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

@Serializable
data class LiveKitTokenPayload(
    @SerialName("token")
    val token: String = "",
    @SerialName("url")
    val url: String = "",
    @SerialName("wsUrl")
    val wsUrl: String = "",
    @SerialName("serverUrl")
    val serverUrl: String = "",
) {
    fun resolvedUrl(): String = url.ifBlank { wsUrl }.ifBlank { serverUrl }
}

class CallTokenService(
    private val supabaseClient: SupabaseClient?,
    private val okHttpClient: OkHttpClient = OkHttpClient(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    @Suppress("TooGenericExceptionCaught")
    suspend fun fetchToken(roomName: String): Result<LiveKitTokenPayload> = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.GRADLE_SUPABASE_URL.trimEnd('/')
        val anonKey = BuildConfig.GRADLE_SUPABASE_ANON_KEY
        val accessToken = supabaseClient?.auth?.currentAccessTokenOrNull()

        when {
            baseUrl.isBlank() || anonKey.isBlank() -> {
                Result.failure(IllegalStateException("Thiếu cấu hình Supabase URL/AnonKey"))
            }

            accessToken == null -> {
                Result.failure(IllegalStateException("Chưa có phiên đăng nhập Supabase"))
            }

            else -> {
                executeTokenRequest(baseUrl, anonKey, accessToken, roomName)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun executeTokenRequest(
        baseUrl: String,
        anonKey: String,
        accessToken: String,
        roomName: String,
    ): Result<LiveKitTokenPayload> = try {
        val payloadJson = buildJsonObject {
            put("roomName", roomName)
            put("room_name", roomName)
        }.toString()

        val request = Request.Builder()
            .url("$baseUrl/functions/v1/$FUNCTION_NAME")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("apikey", anonKey)
            .addHeader("Content-Type", "application/json")
            .post(payloadJson.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val rawBody = response.body.string()
            if (!response.isSuccessful) {
                error("Edge Function $FUNCTION_NAME lỗi ${response.code}: $rawBody")
            }
            val parsed = json.decodeFromString<LiveKitTokenPayload>(rawBody)
            if (parsed.token.isBlank() || parsed.resolvedUrl().isBlank()) {
                error("Edge Function trả thiếu token hoặc LiveKit URL")
            }
            Result.success(parsed)
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        Timber.w(error, "Failed to fetch LiveKit token for room %s", roomName)
        Result.failure(error)
    }

    private companion object {
        const val FUNCTION_NAME = "livekit-token"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
