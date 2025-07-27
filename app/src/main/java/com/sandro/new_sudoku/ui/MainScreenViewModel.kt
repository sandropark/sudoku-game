package com.sandro.new_sudoku.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class DifficultyLevel(val displayName: String, val cellsToRemove: Int) {
    EASY("하", 35),
    MEDIUM("중", 50),
    HARD("상", 65)
}

data class MainScreenState(
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.EASY,
    val hasGameInProgress: Boolean = false,
    val shouldNavigateToGame: Boolean = false,
    val showDifficultyPopup: Boolean = false
)

class MainScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state

    fun selectDifficulty(difficulty: DifficultyLevel) {
        _state.value = _state.value.copy(selectedDifficulty = difficulty)
    }

    fun startNewGame() {
        _state.value = _state.value.copy(
            shouldNavigateToGame = true,
            hasGameInProgress = true,
            showDifficultyPopup = false
        )
    }

    fun continueGame() {
        if (_state.value.hasGameInProgress) {
            _state.value = _state.value.copy(shouldNavigateToGame = true)
        }
    }

    fun markGameInProgress() {
        _state.value = _state.value.copy(hasGameInProgress = true)
    }

    fun onNavigationCompleted() {
        _state.value = _state.value.copy(shouldNavigateToGame = false)
    }

    fun resetGameProgress() {
        _state.value = _state.value.copy(hasGameInProgress = false)
    }

    fun showDifficultyPopup() {
        _state.value = _state.value.copy(showDifficultyPopup = true)
    }

    fun hideDifficultyPopup() {
        _state.value = _state.value.copy(showDifficultyPopup = false)
    }

    fun selectDifficultyAndStartGame(difficulty: DifficultyLevel) {
        _state.value = _state.value.copy(
            selectedDifficulty = difficulty,
            shouldNavigateToGame = true,
            hasGameInProgress = true,
            showDifficultyPopup = false
        )
    }
} 