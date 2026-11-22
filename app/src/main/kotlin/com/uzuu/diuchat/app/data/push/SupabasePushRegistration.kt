package com.uzuu.diuchat.app.data.push

import com.google.firebase.messaging.FirebaseMessaging
import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Đồng bộ FCM token của bản cài app lên bảng `public.push_tokens` trên Supabase
 * và tuần tự hóa thao tác sync / logout bằng [Mutex] để không xảy ra race condition.
 */
class SupabasePushRegistration(
    private val client: SupabaseClient?,
    private val installationStore: PushInstallationStore,
) : PushRegistration {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var started = false

    fun start() {
        val supabase = client ?: return
        if (started) return
        started = true

        scope.launch {
            supabase.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    requestSync()
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun requestSync() {
        if (client == null) return
        scope.launch {
            try {
                mutex.withLock {
                    syncCurrentInstallation(client)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                // Không log exception/body vì response có thể chứa FCM token hoặc session.
                Timber.w(
                    "Đồng bộ push chưa thành công: %s",
                    error.javaClass.simpleName,
                )
            }
        }
    }

    private suspend fun syncCurrentInstallation(supabase: SupabaseClient) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return

        // Session có thể tồn tại trước khi bootstrap_profile hoàn tất.
        val profile =
            supabase.postgrest
                .from(TABLE_USER_PROFILES)
                .select {
                    filter { eq("id", userId) }
                }.decodeList<JsonObject>()

        if (profile.isEmpty()) return

        val installationId = installationStore.getOrCreate()
        val token = currentFcmToken()

        // Tránh tiếp tục nếu tài khoản đã thay đổi trong lúc lấy token.
        if (supabase.auth.currentUserOrNull()?.id != userId) return

        val existing =
            supabase.postgrest
                .from(TABLE_PUSH_TOKENS)
                .select {
                    filter {
                        eq(COL_USER_ID, userId)
                        eq(COL_INSTALLATION_ID, installationId)
                    }
                }.decodeList<JsonObject>()

        if (existing.isEmpty()) {
            val newRow =
                buildJsonObject {
                    put(COL_USER_ID, userId)
                    put(COL_TOKEN, token)
                    put(COL_PLATFORM, PLATFORM_ANDROID)
                    put(COL_INSTALLATION_ID, installationId)
                }
            supabase.postgrest.from(TABLE_PUSH_TOKENS).insert(newRow)
        } else {
            // Migration 09 chỉ cho UPDATE token, platform, installation_id — không sửa user_id.
            val updateValues =
                buildJsonObject {
                    put(COL_TOKEN, token)
                    put(COL_PLATFORM, PLATFORM_ANDROID)
                    put(COL_INSTALLATION_ID, installationId)
                }
            supabase.postgrest.from(TABLE_PUSH_TOKENS).update(updateValues) {
                filter {
                    eq(COL_USER_ID, userId)
                    eq(COL_INSTALLATION_ID, installationId)
                }
            }
        }
    }

    override suspend fun signOut() {
        val supabase = client ?: return
        mutex.withLock {
            val userId = supabase.auth.currentUserOrNull()?.id

            if (userId != null) {
                val installationId = installationStore.getOrCreate()
                android.util.Log.d(
                    "DEBUG-TESTING",
                    "Bản cài hiện tại: $installationId",
                )
                supabase.postgrest.from(TABLE_PUSH_TOKENS).delete {
                    filter {
                        eq(COL_USER_ID, userId)
                        eq(COL_INSTALLATION_ID, installationId)
                    }
                }
            }

            // Chỉ signOut sau khi DELETE đăng ký thiết bị thành công.
            supabase.auth.signOut()
        }
    }

    private suspend fun currentFcmToken(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging
            .getInstance()
            .token
            .addOnSuccessListener { token ->
                if (continuation.isActive) {
                    continuation.resume(token)
                }
            }.addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
    }

    private companion object {
        const val TABLE_USER_PROFILES = "user_profiles"
        const val TABLE_PUSH_TOKENS = "push_tokens"
        const val COL_USER_ID = "user_id"
        const val COL_TOKEN = "token"
        const val COL_PLATFORM = "platform"
        const val COL_INSTALLATION_ID = "installation_id"
        const val PLATFORM_ANDROID = "android"
    }
}
