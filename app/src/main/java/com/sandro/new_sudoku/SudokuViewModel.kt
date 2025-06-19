package com.sandro.new_sudoku

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SudokuState(
    val board: Array<IntArray> = Array(9) { IntArray(9) { 0 } },
    val isInitialCells: Array<BooleanArray> = Array(9) { BooleanArray(9) { false } },
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val isGameComplete: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String = ""
)

class SudokuViewModel : ViewModel() {
    private val game = SudokuGame()
    
    private val _state = MutableStateFlow(SudokuState(
        board = game.getBoard(),
        isInitialCells = generateInitialCellsInfo()
    ))
    val state: StateFlow<SudokuState> = _state.asStateFlow()
    
    private fun generateInitialCellsInfo(): Array<BooleanArray> {
        return Array(9) { row ->
            BooleanArray(9) { col ->
                game.isInitialCell(row, col)
            }
        }
    }
    
    fun selectCell(row: Int, col: Int) {
        _state.value = _state.value.copy(
            selectedRow = row,
            selectedCol = col,
            showError = false
        )
    }
    
    fun setCellValue(value: Int) {
        val currentState = _state.value
        val row = currentState.selectedRow
        val col = currentState.selectedCol
        
        if (row == -1 || col == -1) return
        
        if (game.isInitialCell(row, col)) {
            _state.value = currentState.copy(
                showError = true,
                errorMessage = "초기 숫자는 변경할 수 없습니다"
            )
            return
        }
        
        val success = game.setCell(row, col, value)
        if (!success) {
            _state.value = currentState.copy(
                showError = true,
                errorMessage = "이 숫자는 여기에 놓을 수 없습니다"
            )
            return
        }
        
        _state.value = currentState.copy(
            board = game.getBoard(),
            showError = false,
            errorMessage = "",
            isGameComplete = game.isGameComplete()
        )
    }
    
    fun clearCell() {
        setCellValue(0)
    }
    
    fun newGame() {
        game.generateNewGame()
        _state.value = SudokuState(
            board = game.getBoard(),
            isInitialCells = generateInitialCellsInfo(),
            selectedRow = -1,
            selectedCol = -1,
            isGameComplete = false,
            showError = false
        )
    }
    
    fun solveGame() {
        game.solveGame()
        _state.value = _state.value.copy(
            board = game.getBoard(),
            isGameComplete = true
        )
    }
    
    fun clearBoard() {
        game.clearBoard()
        _state.value = _state.value.copy(
            board = game.getBoard(),
            isInitialCells = generateInitialCellsInfo(),
            selectedRow = -1,
            selectedCol = -1,
            isGameComplete = false,
            showError = false
        )
    }
    
    fun isInitialCell(row: Int, col: Int): Boolean {
        return game.isInitialCell(row, col)
    }
} 