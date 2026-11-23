package com.uzuu.diuchat.app.data.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.uzuu.diuchat.app.R
import com.uzuu.diuchat.feature.base.domain.media.AppSoundPlayer
import com.uzuu.diuchat.feature.base.domain.media.DiuSoundEffect
import timber.log.Timber

class AndroidAppSoundPlayer(
    private val context: Context,
) : AppSoundPlayer {
    private val appContext = context.applicationContext
    private var loopingPlayer: MediaPlayer? = null
    private var activeLoopEffect: DiuSoundEffect? = null

    @Synchronized
    override fun playOneShot(effect: DiuSoundEffect) {
        val rawRes = resolveRawRes(effect)
        runCatching {
            val player = createPlayer(rawRes, isLooping = false, usage = AudioAttributes.USAGE_NOTIFICATION_EVENT)
            player?.apply {
                setOnCompletionListener { mp ->
                    runCatching { mp.release() }
                }
                setOnErrorListener { mp, _, _ ->
                    runCatching { mp.release() }
                    true
                }
                start()
            }
        }.onFailure { error ->
            Timber.w(error, "Unable to play one-shot sound %s", effect)
        }
    }

    @Synchronized
    override fun startLooping(effect: DiuSoundEffect) {
        if (activeLoopEffect == effect && loopingPlayer?.isPlaying == true) return
        stopLooping()
        val rawRes = resolveRawRes(effect)
        val usage =
            if (effect == DiuSoundEffect.CALL_INCOMING_RINGTONE) {
                AudioAttributes.USAGE_NOTIFICATION_RINGTONE
            } else {
                AudioAttributes.USAGE_VOICE_COMMUNICATION
            }
        runCatching {
            val player = createPlayer(rawRes, isLooping = true, usage = usage)
            if (player != null) {
                loopingPlayer = player
                activeLoopEffect = effect
                player.start()
            }
        }.onFailure { error ->
            Timber.w(error, "Unable to start looping sound %s", effect)
            stopLooping()
        }
    }

    @Synchronized
    override fun stopLooping() {
        loopingPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
            runCatching { player.reset() }
            runCatching { player.release() }
        }
        loopingPlayer = null
        activeLoopEffect = null
    }

    private fun createPlayer(
        @RawRes rawRes: Int,
        isLooping: Boolean,
        usage: Int,
    ): MediaPlayer? {
        val afd = appContext.resources.openRawResourceFd(rawRes) ?: return null
        return afd.use { descriptor ->
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(usage)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                this.isLooping = isLooping
                prepare()
            }
        }
    }

    @RawRes
    private fun resolveRawRes(effect: DiuSoundEffect): Int = when (effect) {
        DiuSoundEffect.MESSAGE_SENT -> R.raw.sended
        DiuSoundEffect.MESSAGE_RECEIVED -> R.raw.recieved
        DiuSoundEffect.CALL_OUTGOING_RINGBACK -> R.raw.call_sended
        DiuSoundEffect.CALL_INCOMING_RINGTONE -> R.raw.recieved_call
    }
}
