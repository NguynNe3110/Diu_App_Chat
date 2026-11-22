package com.uzuu.diuchat.app.data.push

import android.content.Context
import java.util.UUID

/**
 * Tạo và lưu bền vững mã định danh cho từng bản cài app (`installation_id`).
 * Giữ nguyên UUID khi đăng xuất để phân biệt thiết bị/bản cài với `user_id`.
 */
class PushInstallationStore(
    context: Context,
) {
    private val preferences =
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun getOrCreate(): String {
        preferences.getString(KEY_INSTALLATION_ID, null)?.let { existing ->
            return existing
        }

        val id = UUID.randomUUID().toString()
        check(
            preferences
                .edit()
                .putString(KEY_INSTALLATION_ID, id)
                .commit(),
        ) {
            "Không lưu được mã bản cài app"
        }

        return id
    }

    private companion object {
        const val PREFS_NAME = "diu_push_installation"
        const val KEY_INSTALLATION_ID = "installation_id"
    }
}
