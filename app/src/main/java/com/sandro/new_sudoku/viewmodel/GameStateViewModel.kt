package com.sandro.new_sudoku.viewmodel

import androidx.lifecycle.ViewModel
import com.sandro.new_sudoku.SudokuGame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class GameState(
    val board: Array<IntArray> = Array(9) { IntArray(9) { 0 } },
    val isInitialCells: Array<BooleanArray> = Array(9) { BooleanArray(9) { false } },
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val highlightedNumber: Int = 0,
    val highlightedCells: Set<Pair<Int, Int>> = emptySet(),
    val highlightedRows: Set<Int> = emptySet(),
    val highlightedCols: Set<Int> = emptySet(),
    val completedNumbers: Set<Int> = emptySet()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!board.contentDeepEquals(other.board)) return false
        if (!isInitialCells.contentDeepEquals(other.isInitialCells)) return false
        if (selectedRow != other.selectedRow) return false
        if (selectedCol != other.selectedCol) return false
        if (highlightedNumber != other.highlightedNumber) return false
        if (highlightedCells != other.highlightedCells) return false
        if (highlightedRows != other.highlightedRows) return false
        if (highlightedCols != other.highlightedCols) return false
        if (completedNumbers != other.completedNumbers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + isInitialCells.contentDeepHashCode()
        result = 31 * result + selectedRow
        result = 31 * result + selectedCol
        result = 31 * result + highlightedNumber
        result = 31 * result + highlightedCells.hashCode()
        result = 31 * result + highlightedRows.hashCode()
        result = 31 * result + highlightedCols.hashCode()
        result = 31 * result + completedNumbers.hashCode()
        return result
    }
}

class GameStateViewModel(
    private val game: SudokuGame
) : ViewModel() {

    private val _state = MutableStateFlow(
        GameState(
            board = game.getBoard(),
            isInitialCells = Array(9) { row ->
                BooleanArray(9) { col ->
                    game.isInitialCell(row, col)
                }
            }
        )
    )
    val state: StateFlow<GameState> = _state

    fun selectCell(row: Int, col: Int) {
        if (row in 0..8 && col in 0..8) {
            val currentBoard = _state.value.board
            val selectedNumber = currentBoard[row][col]

            val highlightedCells = calculateHighlightedCells(selectedNumber)

            _state.value = _state.value.copy(
                selectedRow = row,
                selectedCol = col,
                highlightedNumber = selectedNumber,
                highlightedCells = highlightedCells,
                highlightedRows = if (selectedNumber != 0) setOf(row) else emptySet(),
                highlightedCols = if (selectedNumber != 0) setOf(col) else emptySet()
            )
        }
    }

    fun setCellValue(value: Int): Boolean {
        val currentState = _state.value
        if (currentState.selectedRow == -1 || currentState.selectedCol == -1) {
            return false
        }

        val row = currentState.selectedRow
        val col = currentState.selectedCol

        if (!game.setCell(row, col, value)) {
            return false
        }

        val newBoard = game.getBoard()
        val highlightedCells = calculateHighlightedCells(value)
        val completedNumbers = calculateCompletedNumbers(newBoard)

        _state.value = currentState.copy(
            board = newBoard,
            highlightedNumber = value,
            highlightedCells = highlightedCells,
            highlightedRows = if (value != 0) setOf(row) else emptySet(),
            highlightedCols = if (value != 0) setOf(col) else emptySet(),
            completedNumbers = completedNumbers
        )

        return true
    }

    fun clearCell(): Boolean {
        val currentState = _state.value
        if (currentState.selectedRow == -1 || currentState.selectedCol == -1) {
            return false
        }

        val row = currentState.selectedRow
        val col = currentState.selectedCol

        if (!game.setCell(row, col, 0)) {
            return false
        }

        val newBoard = game.getBoard()
        val completedNumbers = calculateCompletedNumbers(newBoard)

        _state.value = currentState.copy(
            board = newBoard,
            highlightedNumber = 0,
            highlightedCells = emptySet(),
            highlightedRows = emptySet(),
            highlightedCols = emptySet(),
            completedNumbers = completedNumbers
        )

        return true
    }

    fun newGame() {
        game.generateNewGame()
        val newBoard = game.getBoard()
        val newInitialCells = Array(9) { row ->
            BooleanArray(9) { col ->
                game.isInitialCell(row, col)
            }
        }

        _state.value = GameState(
            board = newBoard,
            isInitialCells = newInitialCells,
            completedNumbers = calculateCompletedNumbers(newBoard)
        )
    }

    fun resetToInitialState(initialBoard: Array<IntArray>, initialCells: Array<BooleanArray>) {
        _state.value = GameState(
            board = initialBoard.map { it.clone() }.toTypedArray(),
            isInitialCells = initialCells.map { it.clone() }.toTypedArray(),
            completedNumbers = calculateCompletedNumbers(initialBoard)
        )

        // 게임 보드도 초기 상태로 복원
        for (i in 0..8) {
            for (j in 0..8) {
                game.setCell(i, j, initialBoard[i][j])
            }
        }
    }

    fun isInitialCell(row: Int, col: Int): Boolean {
        return _state.value.isInitialCells[row][col]
    }

    fun getCellValue(row: Int, col: Int): Int {
        return _state.value.board[row][col]
    }

    private fun calculateHighlightedCells(selectedNumber: Int): Set<Pair<Int, Int>> {
        if (selectedNumber == 0) return emptySet()

        val highlightedCells = mutableSetOf<Pair<Int, Int>>()
        val currentBoard = _state.value.board

        for (i in 0..8) {
            for (j in 0..8) {
                if (currentBoard[i][j] == selectedNumber) {
                    highlightedCells.add(Pair(i, j))
                }
            }
        }
        return highlightedCells
    }

    private fun calculateCompletedNumbers(board: Array<IntArray>): Set<Int> {
        val completedNumbers = mutableSetOf<Int>()

        for (number in 1..9) {
            var count = 0
            for (i in 0..8) {
                for (j in 0..8) {
                    if (board[i][j] == number) {
                        count++
                    }
                }
            }
            if (count == 9) {
                completedNumbers.add(number)
            }
        }

        return completedNumbers
    }

    fun updateBoardFromGame() {
        val newBoard = game.getBoard()
        val completedNumbers = calculateCompletedNumbers(newBoard)

        _state.value = _state.value.copy(
            board = newBoard,
            completedNumbers = completedNumbers
        )
    }
}