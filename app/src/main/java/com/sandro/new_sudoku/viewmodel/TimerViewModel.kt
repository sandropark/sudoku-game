package com.sandro.new_sudoku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TimerState(
    val elapsedTimeSeconds: Int = 0,
    val isTimerRunning: Boolean = false
)

class TimerViewModel : ViewModel() {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state

    private var timerJob: Job? = null
    var isTestMode = false

    fun startTimer() {
        if (isTestMode) return

        if (_state.value.isTimerRunning) return

        _state.value = _state.value.copy(isTimerRunning = true)

        timerJob = viewModelScope.launch {
            while (_state.value.isTimerRunning) {
                delay(1000)
                if (_state.value.isTimerRunning) {
                    _state.value = _state.value.copy(
                        elapsedTimeSeconds = _state.value.elapsedTimeSeconds + 1
                    )
                }
            }
        }
    }

    fun stopTimer() {
        _state.value = _state.value.copy(isTimerRunning = false)
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        stopTimer()
        _state.value = TimerState()
    }

    fun pauseTimer() {
        _state.value = _state.value.copy(isTimerRunning = false)
        timerJob?.cancel()
        timerJob = null
    }

    fun resumeTimer() {
        if (!_state.value.isTimerRunning) {
            startTimer()
        }
    }

    fun formatElapsedTime(): String {
        val seconds = _state.value.elapsedTimeSeconds
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}