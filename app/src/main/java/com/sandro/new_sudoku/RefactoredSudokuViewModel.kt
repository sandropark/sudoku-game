package com.sandro.new_sudoku

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandro.new_sudoku.ui.DifficultyLevel
import com.sandro.new_sudoku.viewmodel.GameStateViewModel
import com.sandro.new_sudoku.viewmodel.NoteViewModel
import com.sandro.new_sudoku.viewmodel.TimerViewModel
import com.sandro.new_sudoku.viewmodel.ValidationViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Stack

class RefactoredSudokuViewModel(
    private val gameStateRepository: GameStateRepository? = null
) : ViewModel() {

    private val game = SudokuGame()
    var isTestMode = false

    // 개별 ViewModel들
    internal val gameStateViewModel = GameStateViewModel(game)
    internal val timerViewModel = TimerViewModel()
    internal val noteViewModel = NoteViewModel()
    internal val validationViewModel = ValidationViewModel()

    // Undo 관련
    private val undoStack = Stack<Quadruple<Array<IntArray>, Array<Array<Set<Int>>>, Int, Int>>()
    private val MAX_UNDO_STACK_SIZE = 50

    // 초기 상태 저장용
    private var initialBoard: Array<IntArray>? = null
    private var initialCells: Array<BooleanArray>? = null
    private var currentDifficulty: DifficultyLevel = DifficultyLevel.EASY

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    // 통합된 상태
    val state: StateFlow<SudokuState> = combine(
        gameStateViewModel.state,
        timerViewModel.state,
        noteViewModel.state,
        validationViewModel.state
    ) { gameState, timerState, noteState, validationState ->
        SudokuState(
            board = gameState.board,
            isInitialCells = gameState.isInitialCells,
            selectedRow = gameState.selectedRow,
            selectedCol = gameState.selectedCol,
            highlightedNumber = gameState.highlightedNumber,
            highlightedCells = gameState.highlightedCells,
            highlightedRows = gameState.highlightedRows,
            highlightedCols = gameState.highlightedCols,
            completedNumbers = gameState.completedNumbers,
            elapsedTimeSeconds = timerState.elapsedTimeSeconds,
            isTimerRunning = timerState.isTimerRunning,
            isNoteMode = noteState.isNoteMode,
            notes = noteState.notes,
            invalidCells = validationState.invalidCells,
            mistakeCount = validationState.mistakeCount,
            isGameComplete = validationState.isGameComplete,
            isGameOver = validationState.isGameOver,
            showError = validationState.showError,
            errorMessage = validationState.errorMessage,
            showGameOverDialog = validationState.showGameOverDialog,
            showGameCompleteDialog = validationState.showGameCompleteDialog,
            showRestartOptionsDialog = validationState.showRestartOptionsDialog,
            shouldNavigateToMain = validationState.shouldNavigateToMain
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = createInitialState()
    )

    init {
        timerViewModel.isTestMode = isTestMode

        val board = game.getBoard()
        val isInitialCells = Array(9) { row ->
            BooleanArray(9) { col ->
                game.isInitialCell(row, col)
            }
        }

        saveInitialState(board, isInitialCells)
        validationViewModel.validateBoard(board)
    }

    private fun createInitialState(): SudokuState {
        val board = game.getBoard()
        val isInitialCells = Array(9) { row ->
            BooleanArray(9) { col ->
                game.isInitialCell(row, col)
            }
        }

        return SudokuState(
            board = board,
            isInitialCells = isInitialCells
        )
    }

    // ===== 셀 선택 및 값 설정 =====
    fun selectCell(row: Int, col: Int) {
        gameStateViewModel.selectCell(row, col)

        val currentState = state.value
        if (!isTestMode && !currentState.isTimerRunning && currentState.elapsedTimeSeconds == 0) {
            timerViewModel.startTimer()
        }
    }

    fun setCellValue(value: Int) {
        val currentState = state.value
        if (currentState.selectedRow == -1 || currentState.selectedCol == -1) {
            return
        }

        val row = currentState.selectedRow
        val col = currentState.selectedCol

        // 초기 셀인지 확인
        if (gameStateViewModel.isInitialCell(row, col)) {
            return
        }

        // Undo 스택에 현재 상태 저장
        saveCurrentStateToUndoStack(row, col)

        // 현재 셀 값 확인
        val currentValue = gameStateViewModel.getCellValue(row, col)

        if (currentState.isNoteMode) {
            // 노트 모드
            if (currentValue != 0) return // 숫자가 있으면 노트 불가

            val currentNotes = noteViewModel.getCellNotes(row, col)
            if (currentNotes.contains(value)) {
                noteViewModel.removeNoteNumber(row, col, value)
            } else {
                noteViewModel.addNoteNumber(row, col, value)
            }
        } else {
            // 일반 모드
            if (currentValue == value) {
                // 같은 숫자면 제거
                gameStateViewModel.clearCell()
                noteViewModel.clearCellNotes(row, col)
            } else {
                // 다른 숫자면 설정
                gameStateViewModel.setCellValue(value)
                noteViewModel.clearCellNotes(row, col)
                noteViewModel.removeNotesForRelatedCells(row, col, value, currentState.board)

                // 유효성 검사
                val newBoard = game.getBoard()
                val invalidCells = validationViewModel.validateBoard(newBoard)

                if (invalidCells.contains(Pair(row, col))) {
                    val isGameOver = validationViewModel.handleMistake()
                    if (isGameOver) {
                        timerViewModel.stopTimer()
                    }
                } else {
                    validationViewModel.clearError()

                    // 게임 완료 체크
                    if (validationViewModel.checkGameComplete(newBoard, game)) {
                        timerViewModel.stopTimer()
                    }
                }
            }
        }
    }

    fun clearCell() {
        val currentState = state.value
        if (currentState.selectedRow == -1 || currentState.selectedCol == -1) {
            return
        }

        val row = currentState.selectedRow
        val col = currentState.selectedCol

        if (gameStateViewModel.isInitialCell(row, col)) {
            return
        }

        saveCurrentStateToUndoStack(row, col)
        gameStateViewModel.clearCell()
        noteViewModel.clearCellNotes(row, col)

        val newBoard = game.getBoard()
        validationViewModel.validateBoard(newBoard)
        validationViewModel.clearError()
    }

    // ===== 게임 관리 =====
    fun newGame() {
        timerViewModel.resetTimer()
        validationViewModel.resetValidationState()
        noteViewModel.resetNotes()
        undoStack.clear()

        gameStateViewModel.newGame()

        val newBoard = game.getBoard()
        val newInitialCells = Array(9) { row ->
            BooleanArray(9) { col ->
                game.isInitialCell(row, col)
            }
        }

        saveInitialState(newBoard, newInitialCells)
        validationViewModel.validateBoard(newBoard)
    }

    fun newGameWithDifficulty(difficulty: DifficultyLevel) {
        currentDifficulty = difficulty
        game.generateNewGameWithDifficulty(difficulty)

        timerViewModel.resetTimer()
        validationViewModel.resetValidationState()
        noteViewModel.resetNotes()
        undoStack.clear()

        val newBoard = game.getBoard()
        val newInitialCells = Array(9) { row ->
            BooleanArray(9) { col ->
                game.isInitialCell(row, col)
            }
        }

        saveInitialState(newBoard, newInitialCells)
        gameStateViewModel.resetToInitialState(newBoard, newInitialCells)
        validationViewModel.validateBoard(newBoard)
    }

    fun solveGame() {
        game.solveGame()
        gameStateViewModel.updateBoardFromGame()
        timerViewModel.stopTimer()

        val solvedBoard = game.getBoard()
        validationViewModel.checkGameComplete(solvedBoard, game)
    }

    // ===== 노트 모드 =====
    fun toggleNoteMode() {
        noteViewModel.toggleNoteMode()
    }

    fun addNoteNumber(number: Int) {
        val currentState = state.value
        if (currentState.selectedRow == -1 || currentState.selectedCol == -1) {
            return
        }

        val row = currentState.selectedRow
        val col = currentState.selectedCol

        if (gameStateViewModel.isInitialCell(row, col) ||
            gameStateViewModel.getCellValue(row, col) != 0
        ) {
            return
        }

        noteViewModel.addNoteNumber(row, col, number)
    }

    fun removeNoteNumber(number: Int) {
        val currentState = state.value
        if (currentState.selectedRow == -1 || currentState.selectedCol == -1) {
            return
        }

        noteViewModel.removeNoteNumber(currentState.selectedRow, currentState.selectedCol, number)
    }

    // ===== Undo 기능 =====
    fun onUndo() {
        if (undoStack.isEmpty()) return

        val (previousBoard, previousNotes, _, _) = undoStack.pop()

        // 보드 복원
        for (i in 0..8) {
            for (j in 0..8) {
                game.setCell(i, j, previousBoard[i][j])
            }
        }

        gameStateViewModel.updateBoardFromGame()
        noteViewModel.setNotesFromState(previousNotes)

        val restoredBoard = game.getBoard()
        validationViewModel.validateBoard(restoredBoard)
        validationViewModel.clearError()
    }

    // ===== 실수 처리 =====
    fun continueGameAfterMistakes() {
        validationViewModel.continueGameAfterMistakes()
        timerViewModel.resumeTimer()
    }

    fun startNewGameAfterMistakes() {
        validationViewModel.requestNewGameOptions()
    }

    fun requestNewGameOptions() {
        validationViewModel.requestNewGameOptions()
    }

    fun retryCurrentGame() {
        initialBoard?.let { board ->
            initialCells?.let { cells ->
                timerViewModel.resetTimer()
                validationViewModel.resetValidationState()
                noteViewModel.resetNotes()
                undoStack.clear()

                gameStateViewModel.resetToInitialState(board, cells)
                validationViewModel.validateBoard(board)
            }
        }
        validationViewModel.cancelRestartOptions()
    }

    fun changeDifficultyAndRestart() {
        validationViewModel.requestNavigationToMain()
        validationViewModel.cancelRestartOptions()
    }

    fun cancelRestartOptions() {
        validationViewModel.cancelRestartOptions()
    }

    // ===== 유틸리티 메서드들 =====
    fun isInitialCell(row: Int, col: Int): Boolean {
        return gameStateViewModel.isInitialCell(row, col)
    }

    fun clearError() {
        validationViewModel.clearError()
    }

    fun getCellValue(row: Int, col: Int): Int {
        return gameStateViewModel.getCellValue(row, col)
    }

    fun resetNavigationState() {
        validationViewModel.resetNavigationState()
    }

    fun startNewGameFromComplete() {
        timerViewModel.resetTimer()
        validationViewModel.resetValidationState()
        noteViewModel.resetNotes()
        undoStack.clear()

        gameStateViewModel.newGame()

        val newBoard = game.getBoard()
        val newInitialCells = Array(9) { row ->
            BooleanArray(9) { col ->
                game.isInitialCell(row, col)
            }
        }

        saveInitialState(newBoard, newInitialCells)
        validationViewModel.validateBoard(newBoard)
        validationViewModel.closeGameCompleteDialog()
    }

    // ===== Private 메서드들 =====
    private fun saveCurrentStateToUndoStack(row: Int, col: Int) {
        if (undoStack.size >= MAX_UNDO_STACK_SIZE) {
            undoStack.removeAt(0)
        }

        val currentState = state.value
        val boardCopy = createDeepCopyBoard(currentState.board)
        val notesCopy = createDeepCopyNotes(currentState.notes)

        undoStack.push(Quadruple(boardCopy, notesCopy, row, col))
    }

    private fun createDeepCopyBoard(board: Array<IntArray>): Array<IntArray> {
        return Array(9) { row -> board[row].clone() }
    }

    private fun createDeepCopyNotes(notes: Array<Array<Set<Int>>>): Array<Array<Set<Int>>> {
        return Array(9) { i ->
            Array(9) { j ->
                notes[i][j].toSet()
            }
        }
    }

    private fun saveInitialState(board: Array<IntArray>, cells: Array<BooleanArray>) {
        initialBoard = board.map { it.clone() }.toTypedArray()
        initialCells = cells.map { it.clone() }.toTypedArray()
    }

    fun getInitialBoard(): Array<IntArray>? {
        return initialBoard?.map { it.clone() }?.toTypedArray()
    }

    // ===== 상태 저장/복원 (GameStateRepository와 연동) =====
    fun saveGameState() {
        gameStateRepository?.let { repository ->
            viewModelScope.launch {
                val currentState = state.value
                val serializableState = SerializableGameState(
                    board = currentState.board.map { it.toList() },
                    isInitialCells = currentState.isInitialCells.map { it.toList() },
                    selectedRow = currentState.selectedRow,
                    selectedCol = currentState.selectedCol,
                    isGameComplete = currentState.isGameComplete,
                    notes = currentState.notes.map { row -> row.map { it.toSet() } },
                    mistakeCount = currentState.mistakeCount,
                    isGameOver = currentState.isGameOver,
                    elapsedTimeSeconds = currentState.elapsedTimeSeconds,
                    isTimerRunning = currentState.isTimerRunning,
                    highlightedNumber = currentState.highlightedNumber,
                    completedNumbers = currentState.completedNumbers,
                    difficulty = currentDifficulty,
                    initialBoard = initialBoard?.map { it.toList() } ?: emptyList(),
                    solution = game.getSolution().map { it.toList() }
                )
                repository.saveGameState(serializableState)
            }
        }
    }

    fun loadGameState() {
        gameStateRepository?.let { repository ->
            viewModelScope.launch {
                repository.loadGameState()?.let { savedState ->
                    val boardArray = savedState.board.map { it.toIntArray() }.toTypedArray()
                    val initialCellsArray =
                        savedState.isInitialCells.map { it.toBooleanArray() }.toTypedArray()
                    val notesArray = savedState.notes.map { row ->
                        row.map { it.toSet() }.toTypedArray()
                    }.toTypedArray()

                    // 게임 엔진 상태 복원
                    for (i in 0..8) {
                        for (j in 0..8) {
                            game.setCell(i, j, boardArray[i][j])
                        }
                    }

                    // ViewModel들 상태 복원
                    gameStateViewModel.resetToInitialState(boardArray, initialCellsArray)
                    timerViewModel.resetTimer()
                    // 타이머 시간 복원은 별도로 처리 필요
                    noteViewModel.setNotesFromState(notesArray)
                    validationViewModel.setMistakeCount(savedState.mistakeCount)

                    // 초기 상태 저장
                    saveInitialState(boardArray, initialCellsArray)
                    currentDifficulty = savedState.difficulty

                    // 유효성 검사
                    validationViewModel.validateBoard(boardArray)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerViewModel.stopTimer()
    }
}