package com.sandro.new_sudoku

import androidx.lifecycle.ViewModel
import com.sandro.new_sudoku.ui.DifficultyLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val notes: Array<Array<Set<Int>>> = Array(9) { Array(9) { emptySet() } },
    val mistakeCount: Int = 0,
    val isGameOver: Boolean = false,
    val showGameOverDialog: Boolean = false
)

class SudokuViewModel : ViewModel() {
    private val game = SudokuGame()

    private val _state: MutableStateFlow<SudokuState> = MutableStateFlow(
        SudokuState(
            board = game.getBoard(),
            isInitialCells = Array(9) { row ->
                BooleanArray(9) { col ->
                    game.getInitialBoard()[row][col] != 0
                }
            }
        )
    )
    val state: StateFlow<SudokuState> get() = _state

    // Undo 스택 크기 제한 (메모리 누수 방지)
    private val undoStack = Stack<Quadruple<Array<IntArray>, Array<Array<Set<Int>>>, Int, Int>>()
    private val MAX_UNDO_STACK_SIZE = 50

    // Quadruple 클래스 정의
    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    init {
        // 반드시 SudokuGame 생성 직후의 board와 initialCells를 사용
        val board = game.getBoard()
        val isInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }
        _state.value = SudokuState(
            board = board,
            isInitialCells = isInitialCells
        )
    }

    fun selectCell(row: Int, col: Int) {
        _state.value = _state.value.copy(
            selectedRow = row,
            selectedCol = col
        )
    }

    // 전체 보드를 검사해서 잘못된 셀 좌표를 반환 (캐싱 적용)
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

    // 현재 상태를 undo 스택에 저장 (크기 제한 적용)
    private fun saveCurrentStateToUndoStack(row: Int, col: Int) {
        // 스택 크기 제한 확인
        if (undoStack.size >= MAX_UNDO_STACK_SIZE) {
            undoStack.removeAt(0) // 가장 오래된 항목 제거
        }

        val currentState = _state.value
        val currentBoard = game.getBoard().map { it.copyOf() }.toTypedArray()
        val currentNotes = currentState.notes.map { it.copyOf() }.toTypedArray()
        undoStack.push(Quadruple(currentBoard, currentNotes, row, col))
    }

    // 보드의 깊은 복사본 생성 (최적화된 버전)
    private fun createDeepCopyBoard(): Array<IntArray> {
        return game.getBoard().map { it.copyOf() }.toTypedArray()
    }

    // 노트의 깊은 복사본 생성 (최적화된 버전)
    private fun createDeepCopyNotes(notes: Array<Array<Set<Int>>>): Array<Array<Set<Int>>> {
        return Array(9) { r -> Array(9) { c -> notes[r][c].toSet() } }
    }

    // 상태 업데이트 헬퍼 메서드 (최적화된 버전)
    private fun updateState(
        board: Array<IntArray>? = null,
        notes: Array<Array<Set<Int>>>? = null,
        selectedRow: Int? = null,
        selectedCol: Int? = null,
        isGameComplete: Boolean? = null,
        showError: Boolean? = null,
        errorMessage: String? = null,
        mistakeCount: Int? = null,
        isGameOver: Boolean? = null,
        showGameOverDialog: Boolean? = null,
        recalculateInvalidCells: Boolean = true
    ) {
        val currentState = _state.value
        val newInvalidCells =
            if (recalculateInvalidCells) calculateInvalidCells() else currentState.invalidCells

        _state.value = currentState.copy(
            board = board ?: createDeepCopyBoard(),
            notes = notes ?: currentState.notes,
            selectedRow = selectedRow ?: currentState.selectedRow,
            selectedCol = selectedCol ?: currentState.selectedCol,
            isGameComplete = isGameComplete ?: game.isGameComplete(),
            showError = showError ?: currentState.showError,
            errorMessage = errorMessage ?: currentState.errorMessage,
            invalidCells = newInvalidCells,
            mistakeCount = mistakeCount ?: currentState.mistakeCount,
            isGameOver = isGameOver ?: currentState.isGameOver,
            showGameOverDialog = showGameOverDialog ?: currentState.showGameOverDialog
        )
    }

    fun setCellValue(value: Int) {
        val currentState = _state.value
        val row = currentState.selectedRow
        val col = currentState.selectedCol

        if (!isValidCellSelection(row, col) || isInitialCell(row, col) || currentState.isGameOver) {
            return
        }

        saveCurrentStateToUndoStack(row, col)

        // 값 입력 전에 유효성 검사
        val isValidMove = game.isValidMove(row, col, value)

        val success = game.setCell(row, col, value)
        if (!success) {
            return
        }

        // 노트가 있는 경우 노트를 지우고 숫자만 표시
        val newNotes = createDeepCopyNotes(currentState.notes)
        newNotes[row][col] = emptySet()

        // 실수 카운트 업데이트
        var newMistakeCount = currentState.mistakeCount
        var showGameOverDialog = currentState.showGameOverDialog

        if (!isValidMove && value != 0) { // 잘못된 값이고 0이 아닌 경우 (지우기가 아닌 경우)
            newMistakeCount += 1
            if (newMistakeCount >= 3) {
                showGameOverDialog = true
            }
        }

        updateState(
            board = game.getBoard(),
            notes = newNotes,
            mistakeCount = newMistakeCount,
            showGameOverDialog = showGameOverDialog
        )
    }

    fun clearCell() {
        val currentState = _state.value
        val row = currentState.selectedRow
        val col = currentState.selectedCol

        // 초기 셀은 지우기 기능으로 삭제할 수 없음
        if (!isValidCellSelection(row, col) || isInitialCell(row, col)) {
            return
        }

        saveCurrentStateToUndoStack(row, col)

        val success = game.setCell(row, col, 0)
        if (!success) {
            return
        }

        val newNotes = createDeepCopyNotes(currentState.notes)
        newNotes[row][col] = emptySet()

        updateState(board = game.getBoard(), notes = newNotes)
    }

    fun newGame() {
        game.generateNewGame()
        val newBoard = game.getBoard()
        val newInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }

        // 새 게임 시작 시 undo 스택 초기화
        undoStack.clear()

        _state.value = SudokuState(
            board = newBoard,
            isInitialCells = newInitialCells,
            selectedRow = -1,
            selectedCol = -1,
            isGameComplete = false,
            showError = false,
            invalidCells = calculateInvalidCells(),
            mistakeCount = 0,
            isGameOver = false,
            showGameOverDialog = false
        )
    }

    fun newGameWithDifficulty(difficulty: DifficultyLevel) {
        game.generateNewGameWithDifficulty(difficulty)
        val newBoard = game.getBoard()
        val newInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }

        // 새 게임 시작 시 undo 스택 초기화
        undoStack.clear()

        _state.value = SudokuState(
            board = newBoard,
            isInitialCells = newInitialCells,
            selectedRow = -1,
            selectedCol = -1,
            isGameComplete = false,
            showError = false,
            invalidCells = calculateInvalidCells(),
            mistakeCount = 0,
            isGameOver = false,
            showGameOverDialog = false
        )
    }

    fun solveGame() {
        game.solveGame()
        updateState(isGameComplete = true)
    }

    fun clearBoard() {
        game.clearBoard()
        val newBoard = game.getBoard()
        val newInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }

        // 보드 초기화 시 undo 스택 초기화
        undoStack.clear()

        _state.value = _state.value.copy(
            board = newBoard,
            isInitialCells = newInitialCells,
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
        updateState(showError = false, recalculateInvalidCells = false)
    }

    fun onUndo() {
        if (undoStack.isNotEmpty()) {
            val (prevBoard, prevNotes, prevRow, prevCol) = undoStack.pop()
            val restoreBoard = prevBoard.map { it.copyOf() }.toTypedArray()
            game.setBoard(restoreBoard)
            val notesCopy = createDeepCopyNotes(prevNotes)

            updateState(
                board = createDeepCopyBoard(),
                notes = notesCopy,
                selectedRow = prevRow,
                selectedCol = prevCol,
                showError = false
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

        // 초기 셀에는 노트 추가 불가
        if (!isValidCellSelection(row, col) || !currentState.isNoteMode || isInitialCell(
                row,
                col
            )
        ) {
            return
        }

        // undo 스택에 현재 상태 저장
        saveCurrentStateToUndoStack(row, col)

        // 현재 셀의 후보 숫자 토글
        val currentNotes = currentState.notes[row][col].toMutableSet()
        if (currentNotes.contains(number)) {
            currentNotes.remove(number)
        } else {
            currentNotes.add(number)
        }

        // 새로운 notes 배열 생성
        val newNotes = createDeepCopyNotes(currentState.notes)
        newNotes[row][col] = currentNotes.toSet()

        // 일반 숫자를 지우고 노트만 표시
        val newBoard = currentState.board.map { it.copyOf() }.toTypedArray()
        newBoard[row][col] = 0
        game.setCell(row, col, 0)

        updateState(board = newBoard, notes = newNotes)
    }

    fun removeNoteNumber(number: Int) {
        val currentState = _state.value
        val row = currentState.selectedRow
        val col = currentState.selectedCol

        if (!isValidCellSelection(row, col)) {
            return
        }

        val currentNotes = currentState.notes[row][col].toMutableSet()
        currentNotes.remove(number)

        val newNotes = createDeepCopyNotes(currentState.notes)
        newNotes[row][col] = currentNotes.toSet()

        updateState(notes = newNotes, recalculateInvalidCells = false)
    }

    // 유틸리티 메서드들
    private fun isValidCellSelection(row: Int, col: Int): Boolean {
        return row != -1 && col != -1
    }

    fun getCellValue(row: Int, col: Int): Int {
        return game.getCell(row, col)
    }

    // 게임 종료 팝업에서 계속하기 선택
    fun continueGameAfterMistakes() {
        _state.value = _state.value.copy(
            showGameOverDialog = false,
            mistakeCount = 0 // 실수 카운트 초기화
        )
    }

    // 게임 종료 팝업에서 새 게임 선택
    fun startNewGameAfterMistakes() {
        game.generateNewGame()
        val newBoard = game.getBoard()
        val newInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }

        // 새 게임 시작 시 undo 스택 초기화
        undoStack.clear()

        _state.value = SudokuState(
            board = newBoard,
            isInitialCells = newInitialCells,
            selectedRow = -1,
            selectedCol = -1,
            isGameComplete = false,
            showError = false,
            invalidCells = calculateInvalidCells(),
            mistakeCount = 0,
            isGameOver = false,
            showGameOverDialog = false
        )
    }

    // ViewModel 정리 시 메모리 해제
    override fun onCleared() {
        super.onCleared()
        undoStack.clear()
    }
} 