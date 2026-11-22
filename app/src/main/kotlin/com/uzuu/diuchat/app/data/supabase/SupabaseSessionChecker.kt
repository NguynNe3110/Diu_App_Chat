package com.uzuu.diuchat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.first

class SupabaseSessionChecker(
    private val supabaseClient: SupabaseClient?,
) {
    suspend fun isAuthenticated(): Boolean {
        val client = supabaseClient ?: return false
        val status = runCatching {
            client.auth.sessionStatus.first { it !is SessionStatus.Initializing }
        }.getOrNull()
        return status is SessionStatus.Authenticated
    }
}
