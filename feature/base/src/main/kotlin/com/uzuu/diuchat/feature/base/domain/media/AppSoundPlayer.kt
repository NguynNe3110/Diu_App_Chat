package com.uzuu.diuchat.feature.base.domain.media

enum class DiuSoundEffect {
    MESSAGE_SENT,
    MESSAGE_RECEIVED,
    CALL_OUTGOING_RINGBACK,
    CALL_INCOMING_RINGTONE,
}

interface AppSoundPlayer {
    fun playOneShot(effect: DiuSoundEffect)

    fun startLooping(effect: DiuSoundEffect)

    fun stopLooping()
}
