package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

class CallSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    fun isConfigured(): Boolean = supabaseClient != null

    fun currentUserId(): String? = runCatching { supabaseClient?.auth?.currentUserOrNull()?.id }.getOrNull()

    @Suppress("TooGenericExceptionCaught")
    suspend fun fetchCallLogs(): List<CallLog>? {
        val client = supabaseClient ?: return null
        val userId = client.auth.currentUserOrNull()?.id ?: return null

        return try {
            val rows = queryCallLogRows(client, userId)
            if (rows.isEmpty()) {
                emptyList()
            } else {
                mapRowsToCallLogs(client, userId, rows)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            Timber.w(error, "Failed to fetch call_logs from Supabase")
            null
        }
    }

    private suspend fun queryCallLogRows(
        client: SupabaseClient,
        userId: String,
    ): List<CallLogRow> = client.postgrest
        .from(TABLE_CALL_LOGS)
        .select {
            filter {
                or {
                    eq("caller_id", userId)
                    eq("callee_id", userId)
                }
            }
            order("started_at", Order.DESCENDING)
            limit(CALL_LOGS_LIMIT)
        }.decodeList()

    private suspend fun mapRowsToCallLogs(
        client: SupabaseClient,
        userId: String,
        rows: List<CallLogRow>,
    ): List<CallLog> {
        val peerIds = rows.map { if (it.callerId == userId) it.calleeId else it.callerId }.distinct()
        val profiles = fetchPeerProfiles(client, peerIds)
        return rows.map { row ->
            val isIncoming = row.calleeId == userId
            val peerId = if (isIncoming) row.callerId else row.calleeId
            val peerName = profiles[peerId]?.displayName ?: "Người dùng"
            CallLog(
                id = row.id,
                callId = row.id,
                peerId = peerId,
                peerName = peerName,
                type = if (row.callType.equals("video", ignoreCase = true)) CallType.VIDEO else CallType.AUDIO,
                endReason = mapStatusToEndReason(row.status),
                durationSeconds = row.durationSec.toLong(),
                timestampEpochMs = parseIsoMillis(row.startedAt),
                isIncoming = isIncoming,
            )
        }
    }

    private suspend fun fetchPeerProfiles(
        client: SupabaseClient,
        peerIds: List<String>,
    ): Map<String, ProfileRow> = if (peerIds.isEmpty()) {
        emptyMap()
    } else {
        runCatching {
            client.postgrest
                .from(TABLE_USER_PROFILES)
                .select {
                    filter { isIn("id", peerIds) }
                }.decodeList<ProfileRow>()
                .associateBy { it.id }
        }.getOrDefault(emptyMap())
    }

    private fun mapStatusToEndReason(status: String): CallEndReason = when (status.lowercase()) {
        "missed" -> CallEndReason.MISSED
        "rejected", "declined" -> CallEndReason.REJECTED
        "failed", "network_error" -> CallEndReason.NETWORK_ERROR
        "busy" -> CallEndReason.BUSY
        else -> CallEndReason.COMPLETED
    }

    private fun parseIsoMillis(iso: String?): Long = iso?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
    } ?: System.currentTimeMillis()

    @Serializable
    private data class CallLogRow(
        @SerialName("id")
        val id: String,
        @SerialName("caller_id")
        val callerId: String,
        @SerialName("callee_id")
        val calleeId: String,
        @SerialName("call_type")
        val callType: String = "audio",
        @SerialName("status")
        val status: String = "ended",
        @SerialName("duration_sec")
        val durationSec: Int = 0,
        @SerialName("started_at")
        val startedAt: String? = null,
    )

    @Serializable
    private data class ProfileRow(
        @SerialName("id")
        val id: String,
        @SerialName("display_name")
        val displayName: String,
    )

    private companion object {
        const val TABLE_CALL_LOGS = "call_logs"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val CALL_LOGS_LIMIT = 50L
    }
}
