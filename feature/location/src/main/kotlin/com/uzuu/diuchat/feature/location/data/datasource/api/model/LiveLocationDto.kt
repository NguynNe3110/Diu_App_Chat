package com.uzuu.diuchat.feature.location.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveLocationDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("started_at_iso") val startedAtIso: String,
    @SerialName("expires_at_iso") val expiresAtIso: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("accuracy_meters") val accuracyMeters: Float? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("captured_at_iso") val capturedAtIso: String? = null,
)
