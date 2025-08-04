package com.sandro.new_sudoku.viewmodel

import androidx.lifecycle.ViewModel
import com.sandro.new_sudoku.SudokuGame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ValidationState(
    val invalidCells: Set<Pair<Int, Int>> = emptySet(),
    val mistakeCount: Int = 0,
    val isGameComplete: Boolean = false,
    val isGameOver: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val showGameOverDialog: Boolean = false,
    val showGameCompleteDialog: Boolean = false,
    val showRestartOptionsDialog: Boolean = false,
    val shouldNavigateToMain: Boolean = false
)

class ValidationViewModel : ViewModel() {

    private val _state = MutableStateFlow(ValidationState())
    val state: StateFlow<ValidationState> = _state

    fun validateBoard(board: Array<IntArray>): Set<Pair<Int, Int>> {
        val invalidCells = mutableSetOf<Pair<Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                val value = board[row][col]
                if (value != 0 && !isValidPlacement(board, row, col, value)) {
                    invalidCells.add(Pair(row, col))
                }
            }
        }

        _state.value = _state.value.copy(invalidCells = invalidCells)
        return invalidCells
    }

    fun checkGameComplete(board: Array<IntArray>, game: SudokuGame): Boolean {
        val isComplete = isGameCompleteInternal(board)
        _state.value = _state.value.copy(
            isGameComplete = isComplete,
            showGameCompleteDialog = isComplete
        )
        return isComplete
    }

    private fun isGameCompleteInternal(board: Array<IntArray>): Boolean {
        // 모든 셀이 채워져 있고 유효한지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                val value = board[row][col]
                if (value == 0 || !isValidPlacement(board, row, col, value)) {
                    return false
                }
            }
        }
        return true
    }

    fun handleMistake(): Boolean {
        val newMistakeCount = _state.value.mistakeCount + 1
        val isGameOver = newMistakeCount >= 3

        _state.value = _state.value.copy(
            mistakeCount = newMistakeCount,
            isGameOver = isGameOver,
            showGameOverDialog = isGameOver,
            showError = true,
            errorMessage = if (isGameOver) "게임 오버! 실수가 3번 발생했습니다." else "실수! 올바르지 않은 숫자입니다."
        )

        return isGameOver
    }

    fun continueGameAfterMistakes() {
        _state.value = _state.value.copy(
            mistakeCount = 2,
            isGameOver = false,
            showGameOverDialog = false
        )
    }

    fun requestNewGameOptions() {
        _state.value = _state.value.copy(
            showRestartOptionsDialog = true,
            showGameOverDialog = false
        )
    }

    fun cancelRestartOptions() {
        _state.value = _state.value.copy(
            showRestartOptionsDialog = false
        )
    }

    fun requestNavigationToMain() {
        _state.value = _state.value.copy(
            shouldNavigateToMain = true
        )
    }

    fun resetNavigationState() {
        _state.value = _state.value.copy(
            shouldNavigateToMain = false
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(
            showError = false,
            errorMessage = ""
        )
    }

    fun closeGameCompleteDialog() {
        _state.value = _state.value.copy(
            showGameCompleteDialog = false
        )
    }

    fun resetValidationState() {
        _state.value = ValidationState()
    }

    fun setMistakeCount(count: Int) {
        _state.value = _state.value.copy(mistakeCount = count)
    }

    private fun isValidPlacement(board: Array<IntArray>, row: Int, col: Int, value: Int): Boolean {
        // 임시로 값을 0으로 설정하여 현재 셀을 제외하고 검사
        val originalValue = board[row][col]
        board[row][col] = 0

        val isValid = isValidValue(board, row, col, value)

        // 원래 값으로 복원
        board[row][col] = originalValue

        return isValid
    }

    private fun isValidValue(board: Array<IntArray>, row: Int, col: Int, value: Int): Boolean {
        // 행 검사
        for (c in 0..8) {
            if (board[row][c] == value) {
                return false
            }
        }

        // 열 검사
        for (r in 0..8) {
            if (board[r][col] == value) {
                return false
            }
        }

        // 3x3 박스 검사
        val boxStartRow = (row / 3) * 3
        val boxStartCol = (col / 3) * 3
        for (r in boxStartRow until boxStartRow + 3) {
            for (c in boxStartCol until boxStartCol + 3) {
                if (board[r][c] == value) {
                    return false
                }
            }
        }

        return true
    }
}