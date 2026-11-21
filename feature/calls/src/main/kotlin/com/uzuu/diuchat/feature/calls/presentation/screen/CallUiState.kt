package com.uzuu.diuchat.feature.calls.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.calls.domain.model.CallSession

data class CallUiState(
    val callSession: CallSession? = null,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = false,
    val isFrontCamera: Boolean = true,
    val formattedDuration: String = "00:00",
    val isOverlayMinimized: Boolean = false,
    val connectionQuality: String = "HD",
) : BaseState
