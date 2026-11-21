package com.uzuu.diuchat.feature.location.presentation

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.location.domain.usecase.ObserveLiveLocationUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.StopLiveLocationUseCase
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LiveLocationViewModel(
    private val observeLiveLocationUseCase: ObserveLiveLocationUseCase,
    private val stopLiveLocationUseCase: StopLiveLocationUseCase,
) : BaseViewModel<LiveLocationUiState, LiveLocationAction>(LiveLocationUiState()) {

    private var tickerJob: Job? = null

    fun observeSession(sessionId: String) {
        sendAction(LiveLocationAction.SetLoading(true))
        observeLiveLocationUseCase(sessionId)
            .onEach { session ->
                sendAction(LiveLocationAction.UpdateSession(session))
                if (session != null && session.isActive && !session.isExpired) {
                    startCountdownTicker(session.remainingSeconds)
                } else {
                    tickerJob?.cancel()
                    sendAction(LiveLocationAction.UpdateRemainingTime("00:00"))
                }
            }
            .launchIn(viewModelScope)
    }

    fun stopSharing(sessionId: String) {
        sendAction(LiveLocationAction.SetLoading(true))
        viewModelScope.launch {
            stopLiveLocationUseCase(sessionId)
                .onSuccess {
                    tickerJob?.cancel()
                    sendAction(LiveLocationAction.SharingStopped)
                }
                .onFailure { error ->
                    sendAction(LiveLocationAction.SetError(error.message ?: "Không thể dừng chia sẻ vị trí."))
                }
        }
    }

    private fun startCountdownTicker(initialRemainingSeconds: Long) {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            var remaining = initialRemainingSeconds
            while (remaining > 0) {
                val minutes = remaining / SECONDS_PER_MINUTE
                val seconds = remaining % SECONDS_PER_MINUTE
                val formatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                sendAction(LiveLocationAction.UpdateRemainingTime(formatted))
                delay(TICKER_DELAY_MS)
                remaining--
            }
            sendAction(LiveLocationAction.UpdateRemainingTime("00:00"))
            sendAction(LiveLocationAction.SharingStopped)
        }
    }

    companion object {
        private const val SECONDS_PER_MINUTE = 60L
        private const val TICKER_DELAY_MS = 1000L
    }
}
