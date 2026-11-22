package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import timber.log.Timber
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

class CallSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    fun isConfigured(): Boolean = supabaseClient != null

    fun currentUserId(): String? = runCatching { supabaseClient?.auth?.currentUserOrNull()?.id }.getOrNull()

    fun observeRealtimeUpdates(): Flow<Unit> {
        val client = supabaseClient ?: return emptyFlow()
        return channelFlow {
            val dbChannel = client.channel(CHANNEL_CALLS_DB_UPDATES)
            val callLogChanges =
                dbChannel
                    .postgresChangeFlow<PostgresAction>(schema = SCHEMA_PUBLIC) {
                        table = TABLE_CALL_LOGS
                    }.map { }
            val profileChanges =
                dbChannel
                    .postgresChangeFlow<PostgresAction>(schema = SCHEMA_PUBLIC) {
                        table = TABLE_USER_PROFILES
                    }.map { }

            val broadcastChannel =
                client.realtime.subscriptions["realtime:$CHANNEL_GLOBAL_SYNC"]
                    ?: client.channel(CHANNEL_GLOBAL_SYNC)
            val profileBroadcasts =
                broadcastChannel.broadcastFlow<JsonObject>(event = EVENT_PROFILE_UPDATED).map { }

            launch {
                merge(callLogChanges, profileChanges, profileBroadcasts).collect { send(Unit) }
            }

            runCatching { dbChannel.subscribe() }.onFailure(::rethrowCancellation)
            if (broadcastChannel.status.value != RealtimeChannel.Status.SUBSCRIBED) {
                runCatching { broadcastChannel.subscribe() }.onFailure(::rethrowCancellation)
            }

            awaitClose {
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    removeChannelSafely(client, dbChannel)
                }
            }
        }
    }

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
            val peerProfile = profiles[peerId]
            val peerName = peerProfile?.displayName ?: "Người dùng"
            val safeAvatar = peerProfile?.avatarUrl?.takeUnless { url ->
                url.contains("googleusercontent.com", ignoreCase = true)
            }
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
                peerMoodHex = moodToHex(peerProfile?.currentMood),
                peerAvatarUrl = safeAvatar,
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
        @SerialName("current_mood")
        val currentMood: String? = null,
        @SerialName("avatar_url")
        val avatarUrl: String? = null,
    )

    private fun moodToHex(mood: String?): String = when (mood?.lowercase()) {
        "thuong" -> "#F7A3A9"
        "vui" -> "#FCD36D"
        "nho" -> "#C4B5FD"
        "tram" -> "#94A3B8"
        "binhyen" -> "#7ED9B4"
        else -> "#F7A3A9"
    }

    private suspend fun removeChannelSafely(
        client: SupabaseClient,
        channel: RealtimeChannel,
    ) {
        withContext(NonCancellable) {
            runCatching { channel.unsubscribe() }
            runCatching { client.realtime.removeChannel(channel) }
        }
    }

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    private companion object {
        const val TABLE_CALL_LOGS = "call_logs"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val SCHEMA_PUBLIC = "public"
        const val CHANNEL_CALLS_DB_UPDATES = "calls_db_updates"
        const val CHANNEL_GLOBAL_SYNC = "diu_global_sync"
        const val EVENT_PROFILE_UPDATED = "profile_updated"
        const val CALL_LOGS_LIMIT = 50L
    }
}
