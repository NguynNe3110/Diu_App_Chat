package com.uzuu.diuchat.feature.chat.data.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import com.uzuu.diuchat.feature.chat.domain.media.RecordedVoiceClip
import com.uzuu.diuchat.feature.chat.domain.media.VoiceAudioPlayer
import com.uzuu.diuchat.feature.chat.domain.media.VoiceAudioRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.UUID

class AndroidVoiceAudioRecorder(
    private val context: Context?,
) : VoiceAudioRecorder {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAtMs: Long = 0L
    private val sampledAmplitudes = mutableListOf<Float>()

    @Suppress("DEPRECATION", "TooGenericExceptionCaught")
    override fun startRecording(): Boolean {
        val appContext = context?.applicationContext ?: return false
        cancelRecording()

        return try {
            val voiceDir = File(appContext.cacheDir, "voice").apply { mkdirs() }
            val file = File(voiceDir, "voice_${UUID.randomUUID()}.m4a")
            val mediaRecorder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(appContext)
                } else {
                    MediaRecorder()
                }

            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(AUDIO_SAMPLE_RATE)
                setAudioEncodingBitRate(AUDIO_BIT_RATE)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            recorder = mediaRecorder
            outputFile = file
            startedAtMs = System.currentTimeMillis()
            sampledAmplitudes.clear()
            true
        } catch (error: Throwable) {
            Timber.w(error, "Failed to start voice recording")
            cancelRecording()
            false
        }
    }

    override fun readNormalizedAmplitude(): Float {
        val amp = runCatching { recorder?.maxAmplitude ?: 0 }.getOrDefault(0)
        val normalized = (amp.toFloat() / MAX_AMPLITUDE_VALUE).coerceIn(MIN_BAR_AMPLITUDE, 1.0f)
        sampledAmplitudes.add(normalized)
        return normalized
    }

    @Suppress("TooGenericExceptionCaught")
    override fun stopRecording(): RecordedVoiceClip? {
        val activeRecorder = recorder ?: return null
        val file = outputFile ?: return null
        val elapsedMs = (System.currentTimeMillis() - startedAtMs).coerceAtLeast(MIN_DURATION_MS)

        return try {
            activeRecorder.stop()
            val waveform = downsampleWaveform(sampledAmplitudes)
            RecordedVoiceClip(
                audioUri = Uri.fromFile(file).toString(),
                durationMs = elapsedMs,
                waveform = waveform,
            )
        } catch (error: Throwable) {
            Timber.w(error, "Failed to stop voice recording cleanly")
            runCatching { file.delete() }
            null
        } finally {
            runCatching { activeRecorder.reset() }
            runCatching { activeRecorder.release() }
            recorder = null
            outputFile = null
            startedAtMs = 0L
            sampledAmplitudes.clear()
        }
    }

    override fun cancelRecording() {
        recorder?.let { activeRecorder ->
            runCatching { activeRecorder.stop() }
            runCatching { activeRecorder.reset() }
            runCatching { activeRecorder.release() }
        }
        recorder = null
        outputFile?.let { file -> runCatching { file.delete() } }
        outputFile = null
        startedAtMs = 0L
        sampledAmplitudes.clear()
    }

    private fun downsampleWaveform(samples: List<Float>): List<Float> {
        if (samples.isEmpty()) return DEFAULT_WAVEFORM
        if (samples.size <= TARGET_WAVE_BARS) {
            return List(TARGET_WAVE_BARS) { index ->
                samples[index % samples.size]
            }
        }
        val bucketSize = samples.size.toFloat() / TARGET_WAVE_BARS.toFloat()
        return List(TARGET_WAVE_BARS) { idx ->
            val start = (idx * bucketSize).toInt().coerceIn(0, samples.lastIndex)
            val end = (((idx + 1) * bucketSize).toInt()).coerceIn(start + 1, samples.size)
            samples.subList(start, end).average().toFloat().coerceIn(MIN_BAR_AMPLITUDE, 1.0f)
        }
    }

    private companion object {
        const val AUDIO_SAMPLE_RATE = 44_100
        const val AUDIO_BIT_RATE = 64_000
        const val MAX_AMPLITUDE_VALUE = 18_000f
        const val MIN_BAR_AMPLITUDE = 0.2f
        const val MIN_DURATION_MS = 500L
        const val TARGET_WAVE_BARS = 22
        val DEFAULT_WAVEFORM = listOf(
            0.3f, 0.55f, 0.8f, 0.45f, 0.9f, 0.6f, 0.72f, 0.4f, 0.82f, 0.5f, 0.35f,
            0.65f, 0.88f, 0.48f, 0.7f, 0.58f, 0.84f, 0.38f, 0.68f, 0.52f, 0.76f, 0.42f,
        )
    }
}

class AndroidVoiceAudioPlayer(
    private val context: Context?,
) : VoiceAudioPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    @Suppress("TooGenericExceptionCaught")
    override fun play(
        messageId: String,
        audioUri: String,
        onProgress: (Float) -> Unit,
        onCompleted: () -> Unit,
        onError: () -> Unit,
    ) {
        stop()
        if (audioUri.isBlank()) {
            onError()
            return
        }

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                if (audioUri.startsWith("http://") || audioUri.startsWith("https://")) {
                    setDataSource(audioUri)
                } else if (context != null) {
                    setDataSource(context.applicationContext, Uri.parse(audioUri))
                } else {
                    setDataSource(audioUri)
                }
                setOnPreparedListener { prepared ->
                    prepared.start()
                    startProgressPolling(prepared, onProgress)
                }
                setOnCompletionListener {
                    stop()
                    onCompleted()
                }
                setOnErrorListener { _, _, _ ->
                    stop()
                    onError()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (error: Throwable) {
            Timber.w(error, "Failed to start audio playback for %s", messageId)
            stop()
            onError()
        }
    }

    private fun startProgressPolling(
        player: MediaPlayer,
        onProgress: (Float) -> Unit,
    ) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val progress = runCatching {
                    val duration = player.duration.coerceAtLeast(1)
                    (player.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                }.getOrDefault(0f)
                onProgress(progress)
                delay(PROGRESS_POLL_MS)
            }
        }
    }

    override fun stop() {
        progressJob?.cancel()
        progressJob = null
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
            runCatching { player.reset() }
            runCatching { player.release() }
        }
        mediaPlayer = null
    }

    override fun release() {
        stop()
    }

    private companion object {
        const val PROGRESS_POLL_MS = 120L
    }
}
