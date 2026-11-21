package com.uzuu.diuchat.feature.location.data.datasource.api.service

import com.uzuu.diuchat.feature.location.data.datasource.api.model.LiveLocationDto
import com.uzuu.diuchat.feature.location.data.datasource.api.response.LiveLocationResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException

class LocationSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    suspend fun upsertLiveSession(session: LiveLocationDto): Result<LiveLocationResponse> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.postgrest
                .from(LIVE_LOCATIONS_TABLE)
                .upsert(session)
            LiveLocationResponse(session = session)
        }.onFailure(::rethrowCancellation)
    }

    suspend fun fetchLiveSession(sessionId: String): Result<LiveLocationResponse> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            val dto =
                client.postgrest
                    .from(LIVE_LOCATIONS_TABLE)
                    .select {
                        filter {
                            eq("session_id", sessionId)
                        }
                    }.decodeSingleOrNull<LiveLocationDto>()
            LiveLocationResponse(session = dto)
        }.onFailure(::rethrowCancellation)
    }

    suspend fun stopLiveSession(sessionId: String): Result<Unit> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.postgrest
                .from(LIVE_LOCATIONS_TABLE)
                .update(
                    {
                        set("is_active", false)
                    },
                ) {
                    filter {
                        eq("session_id", sessionId)
                    }
                }
        }.map {}.onFailure(::rethrowCancellation)
    }

    suspend fun updateLiveCoordinates(session: LiveLocationDto): Result<Unit> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.postgrest
                .from(LIVE_LOCATIONS_TABLE)
                .update(
                    {
                        set("latitude", session.latitude)
                        set("longitude", session.longitude)
                        set("accuracy_meters", session.accuracyMeters)
                        set("address", session.address)
                        set("captured_at_iso", session.capturedAtIso)
                    },
                ) {
                    filter {
                        eq("session_id", session.sessionId)
                    }
                }
        }.map {}.onFailure(::rethrowCancellation)
    }

    private fun <T> missingClientFailure(): Result<T> = Result.failure(IllegalStateException("SupabaseClient chưa được khởi tạo."))

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    private companion object {
        private const val LIVE_LOCATIONS_TABLE = "live_locations"
    }
}
