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
    val invalidCells: Set<Pair<Int, Int>> = emptySet(),
    val isNoteMode: Boolean = false,
    val notes: Array<Array<Set<Int>>> = Array(9) { Array(9) { emptySet() } }
)

class SudokuViewModel : ViewModel() {
    private val game = SudokuGame()
    
    private val _state = MutableStateFlow(SudokuState(
        board = game.getBoard(),
        isInitialCells = generateInitialCellsInfo()
    ))
    val state: StateFlow<SudokuState> = _state.asStateFlow()
    
    private val undoStack = Stack<Quadruple<Array<IntArray>, Array<Array<Set<Int>>>, Int, Int>>()
    
    // Quadruple 클래스 정의
    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
    
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
        undoStack.push(Quadruple(currentBoard, currentState.notes.map { it.copyOf() }.toTypedArray(), row, col))
        
        // 숫자를 항상 입력 (요구사항: 숫자는 항상 입력되어야 함)
        val success = game.setCell(row, col, value)
        if (!success) {
            return
        }
        
        // 셀에 숫자를 입력하면 해당 셀의 후보 숫자도 지우기
        val newNotes = currentState.notes.map { it.copyOf() }.toTypedArray()
        newNotes[row][col] = emptySet()
        
        _state.value = currentState.copy(
            board = game.getBoard().map { it.copyOf() }.toTypedArray(),
            showError = false,
            errorMessage = "",
            invalidCells = calculateInvalidCells(),
            isGameComplete = game.isGameComplete(),
            notes = newNotes
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
        undoStack.push(Quadruple(currentBoard, currentState.notes.map { it.copyOf() }.toTypedArray(), row, col))
        
        // 숫자를 항상 지우기 (요구사항: 숫자는 항상 입력되어야 함)
        val success = game.setCell(row, col, 0)
        if (!success) {
            return
        }
        
        // 셀을 지우면 해당 셀의 후보 숫자도 지우기
        val newNotes = currentState.notes.map { it.copyOf() }.toTypedArray()
        newNotes[row][col] = emptySet()
        
        _state.value = currentState.copy(
            board = game.getBoard().map { it.copyOf() }.toTypedArray(),
            showError = false,
            errorMessage = "",
            invalidCells = calculateInvalidCells(),
            notes = newNotes
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
            val (prevBoard, prevNotes, prevRow, prevCol) = undoStack.pop()
            // 완전히 새로운 2차원 배열로 복사
            val restoreBoard = prevBoard.map { it.copyOf() }.toTypedArray()
            game.setBoard(restoreBoard)
            _state.value = _state.value.copy(
                board = game.getBoard().map { it.copyOf() }.toTypedArray(),
                selectedRow = prevRow,
                selectedCol = prevCol,
                showError = false,
                errorMessage = "",
                invalidCells = calculateInvalidCells(),
                notes = prevNotes.map { row -> row.map { it.toSet() }.toTypedArray() }.toTypedArray()
            )
        }
    }
    
    fun toggleNoteMode() {
        _state.value = _state.value.copy(
            isNoteMode = !_state.value.isNoteMode
        )
    }
    
    fun addNoteNumber(number: Int) {
        val currentState = _state.value
        val row = currentState.selectedRow
        val col = currentState.selectedCol
        
        if (row == -1 || col == -1 || !currentState.isNoteMode) {
            return
        }
        
        // undo 스택에 현재 상태 저장
        undoStack.push(Quadruple(
            currentState.board.map { it.copyOf() }.toTypedArray(),
            currentState.notes.map { it.copyOf() }.toTypedArray(),
            row,
            col
        ))
        
        // 현재 셀의 후보 숫자 가져오기
        val currentNotes = currentState.notes[row][col].toMutableSet()
        
        // 이미 있는 숫자면 제거, 없으면 추가 (토글 방식)
        if (currentNotes.contains(number)) {
            currentNotes.remove(number)
        } else {
            currentNotes.add(number)
        }
        
        // 새로운 notes 배열 생성
        val newNotes = currentState.notes.map { it.copyOf() }.toTypedArray()
        newNotes[row][col] = currentNotes
        
        _state.value = currentState.copy(
            notes = newNotes
        )
    }
    
    fun removeNoteNumber(number: Int) {
        val currentState = _state.value
        val row = currentState.selectedRow
        val col = currentState.selectedCol
        
        if (row == -1 || col == -1) {
            return
        }
        
        // 현재 셀의 후보 숫자 가져오기
        val currentNotes = currentState.notes[row][col].toMutableSet()
        currentNotes.remove(number)
        
        // 새로운 notes 배열 생성
        val newNotes = currentState.notes.map { it.copyOf() }.toTypedArray()
        newNotes[row][col] = currentNotes
        
        _state.value = currentState.copy(
            notes = newNotes
        )
    }
} 