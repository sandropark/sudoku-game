package com.sandro.new_sudoku

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Stack

data class SudokuState(
    val board: Array<IntArray> = Array(9) { IntArray(9) { 0 } },
    val isInitialCells: Array<BooleanArray> = Array(9) { BooleanArray(9) { false } },
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val isGameComplete: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val invalidCells: Set<Pair<Int, Int>> = emptySet()
)

class SudokuViewModel : ViewModel() {
    private val game = SudokuGame()
    
    private val _state = MutableStateFlow(SudokuState(
        board = game.getBoard(),
        isInitialCells = generateInitialCellsInfo()
    ))
    val state: StateFlow<SudokuState> = _state.asStateFlow()
    
    private val undoStack = Stack<Triple<Array<IntArray>, Int, Int>>()
    
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
            showError = false,
            errorMessage = ""
        )
    }
    
    // 전체 보드를 검사해서 잘못된 셀 좌표를 반환
    private fun calculateInvalidCells(): Set<Pair<Int, Int>> {
        val invalids = mutableSetOf<Pair<Int, Int>>()
        val board = game.getBoard()
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] != 0 && !game.isCellValid(row, col)) {
                    invalids.add(Pair(row, col))
                }
            }
        }
        return invalids
    }
    
    fun setCellValue(value: Int) {
        val currentState = _state.value
        val row = currentState.selectedRow
        val col = currentState.selectedCol
        if (row == -1 || col == -1) {
            // 셀을 선택하지 않은 경우 아무것도 하지 않음
            return
        }
        
        // 완전히 새로운 2차원 배열로 복사
        val src = game.getBoard()
        val currentBoard = src.map { it.copyOf() }.toTypedArray()
        undoStack.push(Triple(currentBoard, row, col))
        
        // 숫자를 항상 입력 (요구사항: 숫자는 항상 입력되어야 함)
        val success = game.setCell(row, col, value)
        if (!success) {
            return
        }
        
        _state.value = currentState.copy(
            board = game.getBoard().map { it.copyOf() }.toTypedArray(),
            showError = false,
            errorMessage = "",
            invalidCells = calculateInvalidCells(),
            isGameComplete = game.isGameComplete()
        )
    }
    
    fun clearCell() {
        val currentState = _state.value
        val row = currentState.selectedRow
        val col = currentState.selectedCol
        if (row == -1 || col == -1) {
            // 셀을 선택하지 않은 경우 아무것도 하지 않음
            return
        }
        
        // 완전히 새로운 2차원 배열로 복사
        val src = game.getBoard()
        val currentBoard = src.map { it.copyOf() }.toTypedArray()
        undoStack.push(Triple(currentBoard, row, col))
        
        // 숫자를 항상 지우기 (요구사항: 숫자는 항상 입력되어야 함)
        val success = game.setCell(row, col, 0)
        if (!success) {
            return
        }
        
        _state.value = currentState.copy(
            board = game.getBoard().map { it.copyOf() }.toTypedArray(),
            showError = false,
            errorMessage = "",
            invalidCells = calculateInvalidCells()
        )
    }
    
    fun newGame() {
        game.generateNewGame()
        _state.value = SudokuState(
            board = game.getBoard(),
            isInitialCells = generateInitialCellsInfo(),
            selectedRow = -1,
            selectedCol = -1,
            isGameComplete = false,
            showError = false,
            invalidCells = calculateInvalidCells()
        )
    }
    
    fun solveGame() {
        game.solveGame()
        _state.value = _state.value.copy(
            board = game.getBoard(),
            isGameComplete = true,
            invalidCells = calculateInvalidCells()
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
            showError = false,
            invalidCells = calculateInvalidCells()
        )
    }
    
    fun isInitialCell(row: Int, col: Int): Boolean {
        return game.isInitialCell(row, col)
    }
    
    fun clearError() {
        _state.value = _state.value.copy(
            showError = false,
            errorMessage = ""
        )
    }
    
    fun onUndo() {
        if (undoStack.isNotEmpty()) {
            val (prevBoard, prevRow, prevCol) = undoStack.pop()
            // 완전히 새로운 2차원 배열로 복사
            val restoreBoard = prevBoard.map { it.copyOf() }.toTypedArray()
            game.setBoard(restoreBoard)
            _state.value = _state.value.copy(
                board = game.getBoard().map { it.copyOf() }.toTypedArray(),
                selectedRow = prevRow,
                selectedCol = prevCol,
                showError = false,
                errorMessage = "",
                invalidCells = calculateInvalidCells() // undo 시에도 전체 검사
            )
        }
    }
} 