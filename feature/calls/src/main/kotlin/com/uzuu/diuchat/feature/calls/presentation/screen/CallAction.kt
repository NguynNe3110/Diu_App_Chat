package com.uzuu.diuchat.feature.calls.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.calls.domain.model.CallSession

sealed class CallAction : BaseAction<CallUiState> {
    data class UpdateSession(val session: CallSession?) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState {
            val resolvedPeerId = when {
                session == null -> state.peerId
                session.isIncoming -> session.callerId
                else -> session.calleeId
            }
            val resolvedPeerName = when {
                session == null -> state.peerName
                session.isIncoming -> session.callerName
                else -> session.calleeName
            }
            return state.copy(
                callSession = session,
                peerId = resolvedPeerId,
                peerName = resolvedPeerName,
                isMuted = session?.isAudioMuted ?: state.isMuted,
                isSpeakerOn = session?.isSpeakerOn ?: state.isSpeakerOn,
                isVideoEnabled = session?.isVideoEnabled ?: state.isVideoEnabled,
                isFrontCamera = session?.isFrontCamera ?: state.isFrontCamera,
                formattedDuration = formatDuration(session?.durationSeconds ?: 0L),
            )
        }
    }

    data class SetPeerMetadata(
        val peerId: String,
        val peerName: String,
        val peerMoodHex: String,
    ) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState = state.copy(
            peerId = peerId,
            peerName = peerName,
            peerMoodHex = peerMoodHex,
        )
    }

    data class SetMuted(val muted: Boolean) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState = state.copy(isMuted = muted)
    }

    data class SetSpeaker(val speakerOn: Boolean) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState = state.copy(isSpeakerOn = speakerOn)
    }

    data class SetVideoEnabled(val enabled: Boolean) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState = state.copy(isVideoEnabled = enabled)
    }

    data class SetFrontCamera(val isFront: Boolean) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState = state.copy(isFrontCamera = isFront)
    }

    data class ToggleOverlay(val minimized: Boolean) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState = state.copy(isOverlayMinimized = minimized)
    }

    data class SetQuickReplyExpanded(val expanded: Boolean) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState = state.copy(isQuickReplyExpanded = expanded)
    }

    data class RecordQuickReply(val message: String) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState = state.copy(
            lastQuickReplyMessage = message,
            isQuickReplyExpanded = false,
        )
    }

    data class SetReminderScheduled(val scheduled: Boolean) : CallAction() {
        override fun reduce(state: CallUiState): CallUiState = state.copy(reminderScheduled = scheduled)
    }

    companion object {
        fun formatDuration(seconds: Long): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return "%02d:%02d".format(minutes, remainingSeconds)
        }
    }
}
