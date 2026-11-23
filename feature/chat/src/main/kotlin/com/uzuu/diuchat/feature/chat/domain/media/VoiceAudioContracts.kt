package com.uzuu.diuchat.feature.chat.domain.media

data class RecordedVoiceClip(
    val audioUri: String,
    val durationMs: Long,
    val waveform: List<Float>,
)

interface VoiceAudioRecorder {
    fun startRecording(): Boolean

    fun readNormalizedAmplitude(): Float

    fun stopRecording(): RecordedVoiceClip?

    fun cancelRecording()
}

interface VoiceAudioPlayer {
    fun play(
        messageId: String,
        audioUri: String,
        onProgress: (Float) -> Unit,
        onCompleted: () -> Unit,
        onError: () -> Unit,
    )

    fun stop()

    fun release()
}
