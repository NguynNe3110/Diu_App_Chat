package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.HasRecord
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecordOrNull
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import timber.log.Timber
import java.time.Instant
import java.util.Collections
import kotlin.coroutines.cancellation.CancellationException

@Suppress("TooManyFunctions")
class CallSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    fun isConfigured(): Boolean = supabaseClient != null

    fun currentUserId(): String? = runCatching { supabaseClient?.auth?.currentUserOrNull()?.id }.getOrNull()

    fun currentUserDisplayName(): String? = runCatching {
        val meta = supabaseClient?.auth?.currentUserOrNull()?.userMetadata
        (meta?.get("full_name") as? JsonPrimitive)?.contentOrNull
            ?: (meta?.get("name") as? JsonPrimitive)?.contentOrNull
            ?: (meta?.get("display_name") as? JsonPrimitive)?.contentOrNull
    }.getOrNull()

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

    /**
     * Lắng nghe cuộc gọi đến theo thời gian thực qua 2 nguồn song song (chống trùng theo `callId`):
     * 1. Postgres INSERT trên `public.call_logs` (khi `callee_id = currentUserId` và `ended_at == null`).
     * 2. Broadcast event `incoming_call` trên kênh dùng chung `diu_global_sync`.
     */
    fun observeIncomingCalls(): Flow<CallSession> {
        val client = supabaseClient ?: return emptyFlow()
        return channelFlow {
            val seenCallIds = Collections.synchronizedSet(mutableSetOf<String>())
            val dbChannel = client.channel(CHANNEL_INCOMING_CALLS_DB)
            val dbIncoming =
                dbChannel
                    .postgresChangeFlow<PostgresAction.Insert>(schema = SCHEMA_PUBLIC) {
                        table = TABLE_CALL_LOGS
                    }.mapNotNull { action ->
                        mapDbInsertToSession(client, action, seenCallIds)
                    }

            val globalChannel =
                client.realtime.subscriptions["realtime:$CHANNEL_GLOBAL_SYNC"]
                    ?: client.channel(CHANNEL_GLOBAL_SYNC)
            val broadcastIncoming =
                globalChannel
                    .broadcastFlow<IncomingCallBroadcast>(event = EVENT_INCOMING_CALL)
                    .mapNotNull { payload ->
                        mapBroadcastToSession(client, payload, seenCallIds)
                    }

            launch {
                merge(dbIncoming, broadcastIncoming).collect { session ->
                    send(session)
                }
            }

            runCatching { dbChannel.subscribe() }.onFailure(::rethrowCancellation)
            if (globalChannel.status.value != RealtimeChannel.Status.SUBSCRIBED) {
                runCatching { globalChannel.subscribe() }.onFailure(::rethrowCancellation)
            }

            awaitClose {
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    removeChannelSafely(client, dbChannel)
                }
            }
        }
    }

    private suspend fun mapDbInsertToSession(
        client: SupabaseClient,
        action: PostgresAction.Insert,
        seenCallIds: MutableSet<String>,
    ): CallSession? {
        val myId = currentUserId() ?: return null
        val row = runCatching { (action as HasRecord).decodeRecordOrNull<CallLogRow>() }.getOrNull()
        val isValidIncoming = row != null &&
            row.calleeId == myId &&
            row.callerId != myId &&
            row.endedAt == null &&
            seenCallIds.add(row.id)
        if (!isValidIncoming || row == null) return null

        val callerProfile = fetchPeerProfiles(client, listOf(row.callerId))[row.callerId]
        return CallSession(
            callId = row.id,
            callerId = row.callerId,
            callerName = callerProfile?.displayName?.takeIf { it.isNotBlank() } ?: "Người thương",
            calleeId = myId,
            calleeName = currentUserDisplayName() ?: "Tôi",
            status = CallStatus.RINGING,
            type = if (row.isVideo) CallType.VIDEO else CallType.AUDIO,
            isIncoming = true,
            isSpeakerOn = row.isVideo,
            isVideoEnabled = row.isVideo,
            startedAtEpochMs = parseIsoMillis(row.startedAt),
            conversationId = row.conversationId.orEmpty(),
            peerMoodHex = moodToHex(callerProfile?.currentMood),
        )
    }

    private suspend fun mapBroadcastToSession(
        client: SupabaseClient,
        payload: IncomingCallBroadcast,
        seenCallIds: MutableSet<String>,
    ): CallSession? {
        val myId = currentUserId() ?: return null
        val isValidIncoming = payload.calleeId == myId &&
            payload.callerId != myId &&
            seenCallIds.add(payload.callId)
        if (!isValidIncoming) return null

        val callerProfile =
            if (payload.callerName.isBlank()) {
                fetchPeerProfiles(client, listOf(payload.callerId))[payload.callerId]
            } else {
                null
            }
        val resolvedCallerName =
            payload.callerName.takeIf { it.isNotBlank() }
                ?: callerProfile?.displayName?.takeIf { it.isNotBlank() }
                ?: "Người thương"
        return CallSession(
            callId = payload.callId,
            callerId = payload.callerId,
            callerName = resolvedCallerName,
            calleeId = myId,
            calleeName = currentUserDisplayName() ?: "Tôi",
            status = CallStatus.RINGING,
            type = if (payload.isVideo) CallType.VIDEO else CallType.AUDIO,
            isIncoming = true,
            isSpeakerOn = payload.isVideo,
            isVideoEnabled = payload.isVideo,
            startedAtEpochMs = System.currentTimeMillis(),
            conversationId = payload.conversationId,
            peerMoodHex = payload.callerMoodHex.ifBlank { moodToHex(callerProfile?.currentMood) },
        )
    }

    /**
     * Tạo dòng mới trên `public.call_logs` và đồng thời phát tín hiệu `incoming_call`
     * sang máy người nhận. Trả về `conversationId` đã được giải quyết (nếu trước đó để trống).
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun createCallLog(
        callId: String,
        calleeId: String,
        isVideo: Boolean,
        conversationId: String = "",
    ): String {
        val client = supabaseClient ?: return conversationId
        val callerId = currentUserId() ?: return conversationId
        val resolvedCalleeId = resolveCalleeId(client, calleeId, conversationId, callerId)
        val resolvedConversationId = resolveConversationId(client, conversationId, callerId, resolvedCalleeId)
        val callerProfile = fetchPeerProfiles(client, listOf(callerId))[callerId]
        val callerName =
            callerProfile?.displayName?.takeIf { it.isNotBlank() }
                ?: currentUserDisplayName()
                ?: "Người thương"
        val callerMoodHex = moodToHex(callerProfile?.currentMood)

        if (resolvedConversationId.isNotBlank() && resolvedCalleeId.isNotBlank()) {
            insertCallLogRow(
                client = client,
                params = NewCallLogParams(
                    callId = callId,
                    conversationId = resolvedConversationId,
                    callerId = callerId,
                    calleeId = resolvedCalleeId,
                    isVideo = isVideo,
                ),
            )
        }

        if (resolvedCalleeId.isNotBlank()) {
            broadcastIncomingCall(
                client = client,
                payload = IncomingCallBroadcast(
                    callId = callId,
                    conversationId = resolvedConversationId,
                    callerId = callerId,
                    callerName = callerName,
                    callerMoodHex = callerMoodHex,
                    calleeId = resolvedCalleeId,
                    isVideo = isVideo,
                ),
            )
        }
        return resolvedConversationId
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun updateCallLog(
        callId: String,
        endReason: CallEndReason,
        durationSeconds: Long,
        declineMessage: String? = null,
    ) {
        val client = supabaseClient ?: return
        val statusString = mapEndReasonToDbStatus(endReason)
        try {
            client.postgrest
                .from(TABLE_CALL_LOGS)
                .update(
                    buildJsonObject {
                        put("status", statusString)
                        put("duration_seconds", durationSeconds.coerceAtLeast(0L).toInt())
                        put("ended_at", Instant.now().toString())
                        if (!declineMessage.isNullOrBlank()) {
                            put("decline_message", declineMessage)
                        }
                    },
                ) {
                    filter {
                        eq("id", callId)
                    }
                }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            Timber.w(error, "Failed to update call_logs for call %s", callId)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun markCallAccepted(callId: String) {
        val client = supabaseClient ?: return
        runCatching {
            client.postgrest
                .from(TABLE_CALL_LOGS)
                .update(
                    buildJsonObject {
                        put("status", "active")
                    },
                ) {
                    filter {
                        eq("id", callId)
                    }
                }
        }.onFailure { error ->
            rethrowCancellation(error)
            // Bỏ qua an toàn nếu enum `call_status` trên DB chỉ chứa các trạng thái kết thúc
            Timber.d("markCallAccepted skipped on DB enum: %s", error.javaClass.simpleName)
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

    private data class NewCallLogParams(
        val callId: String,
        val conversationId: String,
        val callerId: String,
        val calleeId: String,
        val isVideo: Boolean,
    )

    private suspend fun insertCallLogRow(
        client: SupabaseClient,
        params: NewCallLogParams,
    ) {
        val primaryInsert = runCatching {
            client.postgrest.from(TABLE_CALL_LOGS).insert(
                buildJsonObject {
                    put("id", params.callId)
                    put("conversation_id", params.conversationId)
                    put("caller_id", params.callerId)
                    put("callee_id", params.calleeId)
                    put("is_video", params.isVideo)
                    put("status", "ringing")
                    put("duration_seconds", 0)
                    put("started_at", Instant.now().toString())
                },
            )
        }
        primaryInsert.onFailure { error ->
            rethrowCancellation(error)
            // Fallback nếu enum public.call_status chưa bổ sung 'ringing' (mặc định ban đầu: completed/missed/rejected/busy)
            runCatching {
                client.postgrest.from(TABLE_CALL_LOGS).insert(
                    buildJsonObject {
                        put("id", params.callId)
                        put("conversation_id", params.conversationId)
                        put("caller_id", params.callerId)
                        put("callee_id", params.calleeId)
                        put("is_video", params.isVideo)
                        put("status", "missed")
                        put("duration_seconds", 0)
                        put("started_at", Instant.now().toString())
                    },
                )
            }.onFailure { fallbackError ->
                rethrowCancellation(fallbackError)
                Timber.w(fallbackError, "Failed to insert call_logs row for %s", params.callId)
            }
        }
    }

    private suspend fun broadcastIncomingCall(
        client: SupabaseClient,
        payload: IncomingCallBroadcast,
    ) {
        runCatching {
            val globalChannel =
                client.realtime.subscriptions["realtime:$CHANNEL_GLOBAL_SYNC"]
                    ?: client.channel(CHANNEL_GLOBAL_SYNC)
            if (globalChannel.status.value != RealtimeChannel.Status.SUBSCRIBED) {
                runCatching { globalChannel.subscribe(blockUntilSubscribed = true) }.onFailure(::rethrowCancellation)
            }
            globalChannel.broadcast(
                event = EVENT_INCOMING_CALL,
                message = payload,
            )
        }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to broadcast incoming_call for %s", payload.callId)
        }
    }

    private suspend fun resolveCalleeId(
        client: SupabaseClient,
        calleeId: String,
        conversationId: String,
        callerId: String,
    ): String {
        if (calleeId.isNotBlank()) return calleeId
        if (conversationId.isBlank()) return ""
        return runCatching {
            val members = client.postgrest
                .from(TABLE_CONVERSATION_MEMBERS)
                .select {
                    filter {
                        eq("conversation_id", conversationId)
                    }
                }.decodeList<ConversationMemberRow>()
            members.firstOrNull { it.userId != callerId }?.userId.orEmpty()
        }.onFailure(::rethrowCancellation).getOrDefault("")
    }

    private suspend fun resolveConversationId(
        client: SupabaseClient,
        conversationId: String,
        callerId: String,
        calleeId: String,
    ): String {
        if (conversationId.isNotBlank()) return conversationId
        if (calleeId.isBlank()) return ""
        return runCatching {
            val result = client.postgrest
                .rpc(
                    function = RPC_GET_OR_CREATE_DIRECT,
                    parameters = buildJsonObject {
                        put("p_user_a", callerId)
                        put("p_user_b", calleeId)
                    },
                )
            result.data.trim('"')
        }.onFailure(::rethrowCancellation).getOrDefault("")
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
                type = if (row.isVideo) CallType.VIDEO else CallType.AUDIO,
                endReason = mapStatusToEndReason(row.status),
                durationSeconds = row.durationSeconds.toLong(),
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
        "missed", "timeout" -> CallEndReason.MISSED
        "rejected", "declined" -> CallEndReason.REJECTED
        "failed", "network_error" -> CallEndReason.NETWORK_ERROR
        "busy" -> CallEndReason.BUSY
        else -> CallEndReason.COMPLETED
    }

    private fun mapEndReasonToDbStatus(reason: CallEndReason): String = when (reason) {
        CallEndReason.COMPLETED -> "completed"
        CallEndReason.MISSED, CallEndReason.CANCELLED, CallEndReason.NETWORK_ERROR -> "missed"
        CallEndReason.REJECTED -> "rejected"
        CallEndReason.BUSY -> "busy"
    }

    private fun parseIsoMillis(iso: String?): Long = iso?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
    } ?: System.currentTimeMillis()

    @Serializable
    private data class CallLogRow(
        @SerialName("id")
        val id: String,
        @SerialName("conversation_id")
        val conversationId: String? = null,
        @SerialName("caller_id")
        val callerId: String,
        @SerialName("callee_id")
        val calleeId: String,
        @SerialName("is_video")
        val isVideo: Boolean = false,
        @SerialName("status")
        val status: String = "completed",
        @SerialName("duration_seconds")
        val durationSeconds: Int = 0,
        @SerialName("decline_message")
        val declineMessage: String? = null,
        @SerialName("started_at")
        val startedAt: String? = null,
        @SerialName("ended_at")
        val endedAt: String? = null,
    )

    @Serializable
    private data class IncomingCallBroadcast(
        @SerialName("call_id")
        val callId: String,
        @SerialName("conversation_id")
        val conversationId: String = "",
        @SerialName("caller_id")
        val callerId: String,
        @SerialName("caller_name")
        val callerName: String = "",
        @SerialName("caller_mood_hex")
        val callerMoodHex: String = "#F7A3A9",
        @SerialName("callee_id")
        val calleeId: String,
        @SerialName("is_video")
        val isVideo: Boolean = false,
    )

    @Serializable
    private data class ConversationMemberRow(
        @SerialName("user_id")
        val userId: String,
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
        "vui" -> "#F4CA84"
        "nho" -> "#C5B3EA"
        "tram" -> "#95C4E7"
        "binhyen" -> "#A2D7AC"
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
        const val TABLE_CONVERSATION_MEMBERS = "conversation_members"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val RPC_GET_OR_CREATE_DIRECT = "get_or_create_direct_conversation"
        const val SCHEMA_PUBLIC = "public"
        const val CHANNEL_CALLS_DB_UPDATES = "calls_db_updates"
        const val CHANNEL_INCOMING_CALLS_DB = "calls_incoming_db"
        const val CHANNEL_GLOBAL_SYNC = "diu_global_sync"
        const val EVENT_PROFILE_UPDATED = "profile_updated"
        const val EVENT_INCOMING_CALL = "incoming_call"
        const val CALL_LOGS_LIMIT = 50L
    }
}
