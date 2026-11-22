package com.uzuu.diuchat.feature.base.domain.repository

/**
 * Điều phối đồng bộ FCM push token theo từng bản cài đặt (installation)
 * và gỡ đăng ký token an toàn trước khi đăng xuất.
 */
interface PushRegistration {
    fun requestSync()

    suspend fun signOut()
}

object NoOpPushRegistration : PushRegistration {
    override fun requestSync() {
        // No-op fallback for tests or environments without Firebase/Supabase
    }

    override suspend fun signOut() {
        // No-op fallback
    }
}
