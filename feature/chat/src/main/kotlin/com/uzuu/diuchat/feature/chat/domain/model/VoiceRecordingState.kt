package com.uzuu.diuchat.feature.chat.domain.model

sealed interface VoiceRecordingState {
    data object Idle : VoiceRecordingState

    data object RequestingPermission : VoiceRecordingState

    data class Recording(
        val elapsedMs: Long = 0L,
        val amplitudes: List<Float> = emptyList(),
    ) : VoiceRecordingState

    data class Preview(
        val audioUri: String,
        val durationMs: Long,
        val waveform: List<Float>,
        val isPlaying: Boolean = false,
        val currentPositionMs: Long = 0L,
    ) : VoiceRecordingState

    data class Uploading(
        val audioUri: String,
    ) : VoiceRecordingState
}
