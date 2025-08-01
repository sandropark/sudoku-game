package com.sandro.new_sudoku

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandro.new_sudoku.ui.DifficultyLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
    val showGameOverDialog: Boolean = false,
    val showRestartOptionsDialog: Boolean = false,
    val shouldNavigateToMain: Boolean = false,
    val elapsedTimeSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val showGameCompleteDialog: Boolean = false, // 게임 완료 다이얼로그 표시 여부
    val highlightedNumber: Int = 0, // 하이라이트된 숫자 (0이면 없음)
    val highlightedCells: Set<Pair<Int, Int>> = emptySet(), // 하이라이트된 셀들
    val highlightedRows: Set<Int> = emptySet(), // 하이라이트된 행들
    val highlightedCols: Set<Int> = emptySet(), // 하이라이트된 열들
    val completedNumbers: Set<Int> = emptySet() // 보드에 9개 모두 입력된 완성된 숫자들
)

class SudokuViewModel : ViewModel() {
    private val game = SudokuGame()
    var isTestMode = false // 테스트 모드 플래그

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

    // 초기 보드 상태 저장
    private var initialBoard: Array<IntArray>? = null
    private var initialCells: Array<BooleanArray>? = null

    // 타이머 관련
    private var timerJob: Job? = null

    init {
        // 반드시 SudokuGame 생성 직후의 board와 initialCells를 사용
        val board = game.getBoard()
        val isInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }

        // 초기 상태 저장 (재시도용)
        saveInitialState(board, isInitialCells)

        _state.value = SudokuState(
            board = board,
            isInitialCells = isInitialCells
        )
    }

    fun selectCell(row: Int, col: Int) {
        if (row in 0..8 && col in 0..8) {
            val currentBoard = _state.value.board
            val selectedNumber = currentBoard[row][col]

            // 선택된 셀의 숫자와 같은 숫자들을 찾아서 하이라이트
            val highlightedCells = calculateHighlightedCells(selectedNumber)

            updateState(
                selectedRow = row,
                selectedCol = col,
                showError = false,
                highlightedNumber = selectedNumber,
                highlightedCells = highlightedCells,
                highlightedRows = setOf(row),
                highlightedCols = setOf(col)
            )
        }

        // 처음 게임 화면 진입 시 타이머 자동 시작 (한 번만, 테스트 모드가 아닐 때만)
        if (!isTestMode && !_state.value.isTimerRunning && _state.value.elapsedTimeSeconds == 0) {
            startTimer()
        }
    }

    // 특정 숫자와 같은 숫자를 가진 모든 셀을 찾아서 하이라이트 셋으로 반환
    private fun calculateHighlightedCells(
        targetNumber: Int,
        board: Array<IntArray>? = null
    ): Set<Pair<Int, Int>> {
        return if (targetNumber != 0) {
            buildSet {
                val currentBoard = board ?: _state.value.board
                for (r in 0..8) {
                    for (c in 0..8) {
                        if (currentBoard[r][c] == targetNumber) {
                            add(Pair(r, c))
                        }
                    }
                }
            }
        } else {
            emptySet()
        }
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

    private fun calculateCompletedNumbers(): Set<Int> {
        val board = game.getBoard()
        val numberCounts = mutableMapOf<Int, Int>()

        // 각 숫자의 개수 세기
        for (row in 0..8) {
            for (col in 0..8) {
                val value = board[row][col]
                if (value != 0) {
                    numberCounts[value] = numberCounts.getOrDefault(value, 0) + 1
                }
            }
        }

        // 9개가 모두 채워진 숫자들 반환
        return numberCounts.filter { it.value == 9 }.keys.toSet()
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

    /**
     * 중앙화된 셀 변경 이벤트 처리
     * 모든 셀 변경 시 필요한 상태 업데이트를 한 곳에서 처리
     */
    private fun handleCellChange(
        row: Int,
        col: Int,
        newValue: Int,
        newCellNotes: Set<Int>? = null,
        updateMistakeCount: Boolean = false,
        isValidMove: Boolean = true
    ) {
        val currentState = _state.value

        // 1. 보드 업데이트
        val success = game.setCell(row, col, newValue)
        if (!success) return

        val updatedBoard = game.getBoard()

        // 2. 노트 업데이트
        val newNotes = createDeepCopyNotes(currentState.notes)

        // 현재 셀의 노트 업데이트
        newNotes[row][col] = newCellNotes ?: emptySet()

        // 숫자 입력 시 관련 셀들의 노트에서 해당 숫자 제거
        val notesAfterRemoval = if (newValue != 0) {
            removeNotesForRelatedCells(row, col, newValue, newNotes)
        } else {
            newNotes
        }

        // 3. 하이라이트 상태 업데이트 (항상 수행)
        val highlightedCells = calculateHighlightedCells(newValue, updatedBoard)

        // 4. 실수 카운트 업데이트
        val newMistakeCount = if (updateMistakeCount && !isValidMove && newValue != 0) {
            currentState.mistakeCount + 1
        } else {
            currentState.mistakeCount
        }

        // 5. 게임 오버 확인 (실수 3번 시 게임 오버)
        val showGameOverDialog = newMistakeCount >= 3 && !currentState.isGameOver

        // 6. 게임 완료 확인
        val gameComplete = game.isGameComplete()

        // 7. 모든 상태를 한 번에 업데이트
        updateState(
            board = updatedBoard,
            notes = notesAfterRemoval,
            mistakeCount = newMistakeCount,
            showGameOverDialog = showGameOverDialog,
            isGameComplete = gameComplete,
            showGameCompleteDialog = if (gameComplete && !currentState.isGameComplete) true else null,
            highlightedNumber = newValue,
            highlightedCells = highlightedCells
        )
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
        showRestartOptionsDialog: Boolean? = null,
        shouldNavigateToMain: Boolean? = null,
        elapsedTimeSeconds: Int? = null,
        isTimerRunning: Boolean? = null,
        showGameCompleteDialog: Boolean? = null,
        highlightedNumber: Int? = null,
        highlightedCells: Set<Pair<Int, Int>>? = null,
        highlightedRows: Set<Int>? = null,
        highlightedCols: Set<Int>? = null,
        completedNumbers: Set<Int>? = null,
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
            showGameOverDialog = showGameOverDialog ?: currentState.showGameOverDialog,
            showRestartOptionsDialog = showRestartOptionsDialog
                ?: currentState.showRestartOptionsDialog,
            shouldNavigateToMain = shouldNavigateToMain ?: currentState.shouldNavigateToMain,
            elapsedTimeSeconds = elapsedTimeSeconds ?: currentState.elapsedTimeSeconds,
            isTimerRunning = isTimerRunning ?: currentState.isTimerRunning,
            showGameCompleteDialog = showGameCompleteDialog ?: currentState.showGameCompleteDialog,
            highlightedNumber = highlightedNumber ?: currentState.highlightedNumber,
            highlightedCells = highlightedCells ?: currentState.highlightedCells,
            highlightedRows = highlightedRows ?: currentState.highlightedRows,
            highlightedCols = highlightedCols ?: currentState.highlightedCols,
            completedNumbers = completedNumbers ?: calculateCompletedNumbers()
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

        // 토글 기능: 현재 값과 입력하려는 값이 같으면 지우기 (0으로 설정)
        val currentCellValue = game.getCell(row, col)
        val finalValue = if (currentCellValue == value && value != 0) {
            0 // 같은 값이면 지우기
        } else {
            value // 다른 값이면 그대로 설정
        }

        // 값 입력 전에 유효성 검사
        val isValidMove = game.isValidMove(row, col, finalValue)

        // 토글로 지우는 경우는 실수로 계산하지 않음
        val isToggleClear = (currentCellValue == value && value != 0)
        val shouldUpdateMistakeCount = !isToggleClear

        // 게임 완료시 타이머 정지
        if (finalValue != 0) {
            // 임시로 값을 설정해서 게임 완료 확인
            val tempSuccess = game.setCell(row, col, finalValue)
            if (tempSuccess && game.isGameComplete()) {
                stopTimer()
            }
            // 원래 값으로 되돌림 (handleCellChange에서 다시 설정할 것)
            game.setCell(row, col, currentCellValue)
        }

        // 실수 3번 시 타이머 정지
        if (shouldUpdateMistakeCount && !isValidMove && finalValue != 0) {
            val newMistakeCount = currentState.mistakeCount + 1
            if (newMistakeCount >= 3) {
                stopTimer()
            }
        }

        // 중앙화된 셀 변경 처리
        handleCellChange(
            row = row,
            col = col,
            newValue = finalValue,
            newCellNotes = emptySet(), // 숫자 입력 시 노트는 항상 지움
            updateMistakeCount = shouldUpdateMistakeCount,
            isValidMove = isValidMove
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

        // 중앙화된 셀 변경 처리
        handleCellChange(
            row = row,
            col = col,
            newValue = 0, // 셀 지우기
            newCellNotes = emptySet(), // 노트도 함께 지움
            updateMistakeCount = false, // 지우기는 실수가 아님
            isValidMove = true // 지우기는 항상 유효한 동작
        )
    }

    fun newGame() {
        // 기존 타이머 정리
        stopTimer()

        game.generateNewGame()
        val newBoard = game.getBoard()
        val newInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }

        // 초기 상태 저장 (재시도용)
        saveInitialState(newBoard, newInitialCells)

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
            showGameOverDialog = false,
            showRestartOptionsDialog = false,
            shouldNavigateToMain = false,
            elapsedTimeSeconds = 0,
            isTimerRunning = false
        )
    }

    fun newGameWithDifficulty(difficulty: DifficultyLevel) {
        game.generateNewGameWithDifficulty(difficulty)
        val newBoard = game.getBoard()
        val newInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }

        // 초기 상태 저장 (재시도용)
        saveInitialState(newBoard, newInitialCells)

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
            showGameOverDialog = false,
            showRestartOptionsDialog = false,
            shouldNavigateToMain = false
        )
    }

    fun solveGame() {
        game.solveGame()
        stopTimer() // 게임 완료 시 타이머 정지
        updateState(isGameComplete = true, showGameCompleteDialog = true)
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

        // 일반 숫자가 있는 경우 숫자를 지우고 노트만 표시
        val currentCellValue = currentState.board[row][col]
        val finalValue = if (currentCellValue != 0) 0 else currentCellValue

        // 중앙화된 셀 변경 처리
        handleCellChange(
            row = row,
            col = col,
            newValue = finalValue,
            newCellNotes = currentNotes.toSet(),
            updateMistakeCount = false, // 노트 입력은 실수가 아님
            isValidMove = true // 노트 입력은 항상 유효
        )
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

    // 숫자 입력 시 관련된 셀들(같은 행, 열, 3x3 박스)의 노트에서 해당 숫자를 제거
    private fun removeNotesForRelatedCells(
        inputRow: Int,
        inputCol: Int,
        value: Int,
        notes: Array<Array<Set<Int>>>
    ): Array<Array<Set<Int>>> {
        if (value == 0) return notes // 0으로 지우는 경우 노트 제거 안 함

        val newNotes = createDeepCopyNotes(notes)

        // 같은 행의 모든 셀에서 해당 숫자를 노트에서 제거
        for (col in 0..8) {
            if (col != inputCol && newNotes[inputRow][col].contains(value)) {
                newNotes[inputRow][col] = newNotes[inputRow][col] - value
            }
        }

        // 같은 열의 모든 셀에서 해당 숫자를 노트에서 제거
        for (row in 0..8) {
            if (row != inputRow && newNotes[row][inputCol].contains(value)) {
                newNotes[row][inputCol] = newNotes[row][inputCol] - value
            }
        }

        // 같은 3x3 박스의 모든 셀에서 해당 숫자를 노트에서 제거
        val boxRow = (inputRow / 3) * 3
        val boxCol = (inputCol / 3) * 3
        for (row in boxRow until boxRow + 3) {
            for (col in boxCol until boxCol + 3) {
                if ((row != inputRow || col != inputCol) && newNotes[row][col].contains(value)) {
                    newNotes[row][col] = newNotes[row][col] - value
                }
            }
        }

        return newNotes
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
            mistakeCount = 2 // 실수 카운트를 2로 설정 (페널티)
        )
        // 계속하기 시 타이머 재시작
        startTimer()
    }

    // 게임 종료 팝업에서 새 게임 선택
    fun startNewGameAfterMistakes() {
        game.generateNewGame()
        val newBoard = game.getBoard()
        val newInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }

        // 초기 상태 저장 (재시도용)
        saveInitialState(newBoard, newInitialCells)

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
            showGameOverDialog = false,
            showRestartOptionsDialog = false,
            shouldNavigateToMain = false
        )
    }

    // 재시작 옵션 요청 (게임 오버 후 새 게임 버튼 클릭)
    fun requestNewGameOptions() {
        _state.value = _state.value.copy(
            showGameOverDialog = false,
            showRestartOptionsDialog = true
        )
    }

    // 현재 게임 재시도 (같은 보드로 처음부터)
    fun retryCurrentGame() {
        if (initialBoard != null && initialCells != null) {
            // 초기 상태로 복원
            val restoredBoard = initialBoard!!.map { it.clone() }.toTypedArray()
            val restoredCells = initialCells!!.map { it.clone() }.toTypedArray()

            // 게임 보드 설정
            game.setBoard(restoredBoard)

            // 상태 초기화
            undoStack.clear()

            _state.value = SudokuState(
                board = restoredBoard,
                isInitialCells = restoredCells,
                selectedRow = -1,
                selectedCol = -1,
                isGameComplete = false,
                showError = false,
                invalidCells = calculateInvalidCells(),
                mistakeCount = 0,
                isGameOver = false,
                showGameOverDialog = false,
                showRestartOptionsDialog = false,
                shouldNavigateToMain = false,
                elapsedTimeSeconds = 0,
                isTimerRunning = false
            )

            // 재시도 시 타이머 리셋 후 자동 시작
            resetTimer()
            startTimer()
        }
    }

    // 난이도 변경 후 재시작
    fun changeDifficultyAndRestart() {
        _state.value = _state.value.copy(
            showRestartOptionsDialog = false,
            shouldNavigateToMain = true
        )
    }

    // 재시작 옵션 취소
    fun cancelRestartOptions() {
        _state.value = _state.value.copy(
            showRestartOptionsDialog = false,
            showGameOverDialog = true
        )
    }

    // 초기 보드 상태 저장
    private fun saveInitialState(board: Array<IntArray>, cells: Array<BooleanArray>) {
        initialBoard = board.map { it.clone() }.toTypedArray()
        initialCells = cells.map { it.clone() }.toTypedArray()
    }

    // 초기 보드 상태 조회 (테스트용)
    fun getInitialBoard(): Array<IntArray>? {
        return initialBoard?.map { it.clone() }?.toTypedArray()
    }

    // 네비게이션 상태 초기화
    fun resetNavigationState() {
        _state.value = _state.value.copy(shouldNavigateToMain = false)
    }

    // 게임 완료 다이얼로그에서 새 게임 시작
    fun startNewGameFromComplete() {
        // 기존 타이머 정리
        stopTimer()

        game.generateNewGame()
        val newBoard = game.getBoard()
        val newInitialCells =
            Array(9) { row -> BooleanArray(9) { col -> game.isInitialCell(row, col) } }

        // 초기 상태 저장 (재시도용)
        saveInitialState(newBoard, newInitialCells)

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
            showGameOverDialog = false,
            showRestartOptionsDialog = false,
            shouldNavigateToMain = false,
            elapsedTimeSeconds = 0,
            isTimerRunning = false,
            showGameCompleteDialog = false
        )
    }

    // 게임 완료 다이얼로그에서 메인으로 이동
    fun goToMainFromComplete() {
        _state.value = _state.value.copy(
            showGameCompleteDialog = false,
            shouldNavigateToMain = true
        )
    }

    // 게임 완료 다이얼로그 닫기
    fun closeGameCompleteDialog() {
        _state.value = _state.value.copy(
            showGameCompleteDialog = false
        )
    }

    fun useHint() {
        val currentState = _state.value

        // 셀이 선택되지 않은 경우 아무것도 하지 않음
        if (currentState.selectedRow == -1 || currentState.selectedCol == -1) {
            return
        }

        // 게임이 완료된 경우 아무것도 하지 않음
        if (currentState.isGameComplete) {
            return
        }

        val row = currentState.selectedRow
        val col = currentState.selectedCol

        // 현재 셀의 정답 가져오기
        val correctValue = game.getHint(row, col)

        // 현재 값과 정답이 같으면 아무것도 하지 않음
        if (currentState.board[row][col] == correctValue) {
            return
        }

        // Undo를 위한 현재 상태 저장
        saveCurrentStateToUndoStack(row, col)

        // 새로운 보드 생성 (정답으로 셀 업데이트)
        val newBoard = Array(9) { r ->
            IntArray(9) { c ->
                if (r == row && c == col) {
                    correctValue
                } else {
                    currentState.board[r][c]
                }
            }
        }

        // 노트에서 해당 셀의 노트 제거
        val newNotes = Array(9) { r ->
            Array(9) { c ->
                if (r == row && c == col) {
                    emptySet<Int>()
                } else {
                    currentState.notes[r][c]
                }
            }
        }

        // 힌트 사용 시에도 관련 셀들의 노트에서 해당 숫자 제거
        val notesAfterRemoval = removeNotesForRelatedCells(row, col, correctValue, newNotes)

        // 게임 보드 업데이트
        game.setCell(row, col, correctValue)

        // 완성된 숫자들 계산
        val completedNumbers = calculateCompletedNumbers()

        // 하이라이트 업데이트
        val highlightedCells = calculateHighlightedCells(correctValue)

        // 게임 완료 확인
        val isGameComplete = game.isGameComplete()

        // 상태 업데이트
        updateState(
            board = newBoard,
            notes = notesAfterRemoval,
            isGameComplete = isGameComplete,
            completedNumbers = completedNumbers,
            highlightedNumber = correctValue,
            highlightedCells = highlightedCells,
            showGameCompleteDialog = isGameComplete,
            recalculateInvalidCells = true
        )

        // 게임 완료 시 타이머 정지
        if (isGameComplete) {
            stopTimer()
        }
    }

    // 테스트용: 정답을 모두 입력하는 메서드
    fun fillCorrectAnswers() {
        val currentState = _state.value

        // 이미 게임이 완료된 경우 아무것도 하지 않음
        if (currentState.isGameComplete) {
            return
        }

        // 현재 게임을 해결하여 정답 보드 얻기
        game.solveGame()
        val solution = game.getBoard()

        // 빈 셀들만 정답으로 채우기 (초기 셀은 건드리지 않음)
        val newBoard = Array(9) { row ->
            IntArray(9) { col ->
                if (game.isInitialCell(row, col)) {
                    // 초기 셀은 그대로 유지
                    currentState.board[row][col]
                } else {
                    // 빈 셀은 정답으로 채우기
                    solution[row][col]
                }
            }
        }

        // 타이머 정지
        stopTimer()

        // 상태 업데이트: 게임 완료, 다이얼로그 표시
        _state.value = _state.value.copy(
            board = newBoard,
            isGameComplete = true,
            selectedRow = -1,
            selectedCol = -1,
            showError = false,
            invalidCells = emptySet(),
            showGameCompleteDialog = true,
            notes = Array(9) { Array(9) { emptySet() } } // 노트 초기화
        )
    }

    // ===== 타이머 관련 메서드들 =====

    fun startTimer() {
        if (!_state.value.isTimerRunning) {
            _state.value = _state.value.copy(isTimerRunning = true)

            // 테스트 모드에서는 실제 타이머를 시작하지 않음
            if (isTestMode) {
                return
            }

            try {
                timerJob = viewModelScope.launch {
                    while (_state.value.isTimerRunning) {
                        delay(1000) // 1초마다 업데이트
                        if (_state.value.isTimerRunning) {
                            _state.value = _state.value.copy(
                                elapsedTimeSeconds = _state.value.elapsedTimeSeconds + 1
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // 테스트 환경에서는 타이머가 실행되지 않을 수 있음
                // 상태는 isTimerRunning = true로 유지 (테스트에서 확인용)
            }
        }
    }

    fun stopTimer() {
        _state.value = _state.value.copy(isTimerRunning = false)
        timerJob?.cancel()
        timerJob = null
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

    fun resetTimer() {
        stopTimer()
        _state.value = _state.value.copy(
            elapsedTimeSeconds = 0,
            isTimerRunning = false
        )
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    // 테스트용 메서드
    fun updateTimerForTest(seconds: Int) {
        _state.value = _state.value.copy(elapsedTimeSeconds = seconds)
    }

    // 셀 선택 해제
    fun clearSelection() {
        _state.value = _state.value.copy(
            selectedRow = -1,
            selectedCol = -1,
            highlightedNumber = 0,
            highlightedCells = emptySet(),
            highlightedRows = emptySet(),
            highlightedCols = emptySet()
        )
    }

    // ViewModel 정리 시 메모리 해제
    override fun onCleared() {
        super.onCleared()
        undoStack.clear()
        initialBoard = null
        initialCells = null
        timerJob?.cancel()
    }
} 