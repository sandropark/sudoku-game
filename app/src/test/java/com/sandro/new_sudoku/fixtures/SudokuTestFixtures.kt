package com.sandro.new_sudoku.fixtures

import com.sandro.new_sudoku.SudokuState
import com.sandro.new_sudoku.ui.DifficultyLevel

/**
 * 스도쿠 테스트를 위한 고정된 테스트 데이터 제공
 * 랜덤 보드 대신 예측 가능한 테스트 환경 구성
 */
object SudokuTestFixtures {

    /**
     * 쉬운 난이도 테스트 퍼즐 (35개 셀 제거)
     * 해결 가능하고 유일한 해답을 가진 퍼즐
     */
    val EASY_PUZZLE = arrayOf(
        intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
        intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
        intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
        intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
        intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
        intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
        intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
        intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
        intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
    )

    /**
     * EASY_PUZZLE의 완성된 해답
     */
    val EASY_SOLUTION = arrayOf(
        intArrayOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
        intArrayOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
        intArrayOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
        intArrayOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
        intArrayOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
        intArrayOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
        intArrayOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
        intArrayOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
        intArrayOf(3, 4, 5, 2, 8, 6, 1, 7, 9)
    )

    /**
     * 중간 난이도 테스트 퍼즐 (50개 셀 제거)
     */
    val MEDIUM_PUZZLE = arrayOf(
        intArrayOf(0, 0, 0, 6, 0, 0, 4, 0, 0),
        intArrayOf(7, 0, 0, 0, 0, 3, 6, 0, 0),
        intArrayOf(0, 0, 0, 0, 9, 1, 0, 8, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 5, 0, 1, 8, 0, 0, 0, 3),
        intArrayOf(0, 0, 0, 3, 0, 6, 0, 4, 5),
        intArrayOf(0, 4, 0, 2, 0, 0, 0, 6, 0),
        intArrayOf(9, 0, 3, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 2, 0, 0, 0, 0, 1, 0, 0)
    )

    /**
     * 어려운 난이도 테스트 퍼즐 (65개 셀 제거)
     */
    val HARD_PUZZLE = arrayOf(
        intArrayOf(0, 0, 0, 0, 0, 6, 0, 0, 0),
        intArrayOf(0, 5, 9, 0, 0, 0, 0, 0, 8),
        intArrayOf(2, 0, 0, 0, 0, 8, 0, 0, 0),
        intArrayOf(0, 4, 5, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 0, 3, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 0, 6, 0, 0, 3, 0, 5, 4),
        intArrayOf(0, 0, 0, 3, 2, 5, 0, 0, 6),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
    )

    /**
     * 빈 보드 (모든 셀이 0)
     */
    val EMPTY_BOARD = Array(9) { IntArray(9) { 0 } }

    /**
     * 완성된 유효한 스도쿠 보드
     */
    val COMPLETE_BOARD = EASY_SOLUTION

    /**
     * 기본 테스트 상태 생성
     */
    fun createBasicTestState(): SudokuState {
        return SudokuState(
            board = EASY_PUZZLE.map { it.copyOf() }.toTypedArray(),
            isInitialCells = Array(9) { row ->
                BooleanArray(9) { col ->
                    EASY_PUZZLE[row][col] != 0
                }
            },
            selectedRow = -1,
            selectedCol = -1,
            isGameComplete = false,
            showError = false,
            errorMessage = "",
            invalidCells = emptySet(),
            isNoteMode = false,
            notes = Array(9) { Array(9) { emptySet<Int>() } },
            mistakeCount = 0,
            isGameOver = false,
            showGameOverDialog = false,
            showRestartOptionsDialog = false,
            shouldNavigateToMain = false,
            elapsedTimeSeconds = 0,
            isTimerRunning = false,
            showGameCompleteDialog = false,
            highlightedNumber = 0,
            highlightedCells = emptySet(),
            highlightedRows = emptySet(),
            highlightedCols = emptySet()
        )
    }

    /**
     * 셀이 선택된 상태 생성
     */
    fun createSelectedCellState(row: Int = 0, col: Int = 2): SudokuState {
        return createBasicTestState().copy(
            selectedRow = row,
            selectedCol = col
        )
    }

    /**
     * 에러 상태 생성 (잘못된 셀이 있는 상태)
     */
    fun createErrorState(): SudokuState {
        return createBasicTestState().copy(
            invalidCells = setOf(Pair(0, 2), Pair(1, 1)),
            showError = true,
            errorMessage = "스도쿠 규칙 위반"
        )
    }

    /**
     * 게임 완료 상태 생성
     */
    fun createGameCompleteState(): SudokuState {
        return SudokuState(
            board = COMPLETE_BOARD.map { it.copyOf() }.toTypedArray(),
            isInitialCells = Array(9) { row ->
                BooleanArray(9) { col ->
                    EASY_PUZZLE[row][col] != 0
                }
            },
            selectedRow = -1,
            selectedCol = -1,
            isGameComplete = true,
            showError = false,
            errorMessage = "",
            invalidCells = emptySet(),
            isNoteMode = false,
            notes = Array(9) { Array(9) { emptySet<Int>() } },
            mistakeCount = 0,
            isGameOver = false,
            showGameOverDialog = false,
            showRestartOptionsDialog = false,
            shouldNavigateToMain = false,
            elapsedTimeSeconds = 120, // 2분
            isTimerRunning = false,
            showGameCompleteDialog = true,
            highlightedNumber = 0,
            highlightedCells = emptySet(),
            highlightedRows = emptySet(),
            highlightedCols = emptySet()
        )
    }

    /**
     * 노트가 있는 상태 생성
     */
    fun createStateWithNotes(): SudokuState {
        val notes = Array(9) { Array(9) { emptySet<Int>() } }
        notes[0][2] = setOf(1, 4, 6) // 첫 번째 빈 셀에 노트
        notes[1][1] = setOf(2, 7) // 두 번째 빈 셀에 노트

        return createBasicTestState().copy(
            notes = notes,
            isNoteMode = true
        )
    }

    /**
     * 실수가 있는 상태 생성 (실수 2번)
     */
    fun createStateWithMistakes(): SudokuState {
        return createBasicTestState().copy(
            mistakeCount = 2,
            invalidCells = setOf(Pair(0, 2))
        )
    }

    /**
     * 게임 오버 상태 생성 (실수 3번)
     */
    fun createGameOverState(): SudokuState {
        return createBasicTestState().copy(
            mistakeCount = 3,
            isGameOver = true,
            showGameOverDialog = true,
            isTimerRunning = false
        )
    }

    /**
     * 타이머가 실행 중인 상태 생성
     */
    fun createTimerRunningState(): SudokuState {
        return createBasicTestState().copy(
            isTimerRunning = true,
            elapsedTimeSeconds = 300 // 5분
        )
    }

    /**
     * 난이도별 퍼즐 가져오기
     */
    fun getPuzzleByDifficulty(difficulty: DifficultyLevel): Array<IntArray> {
        return when (difficulty) {
            DifficultyLevel.EASY -> EASY_PUZZLE.map { it.copyOf() }.toTypedArray()
            DifficultyLevel.MEDIUM -> MEDIUM_PUZZLE.map { it.copyOf() }.toTypedArray()
            DifficultyLevel.HARD -> HARD_PUZZLE.map { it.copyOf() }.toTypedArray()
        }
    }

    /**
     * 테스트용 유효한 이동 데이터
     * (위치, 값, 유효성)
     */
    val VALID_MOVES = listOf(
        Triple(Pair(0, 2), 4, true),  // EASY_PUZZLE[0][2]에 4는 유효
        Triple(Pair(0, 3), 6, false), // EASY_PUZZLE[0][3]에 6는 이미 같은 행에 존재
        Triple(Pair(1, 1), 7, true),  // EASY_PUZZLE[1][1]에 7은 유효
        Triple(Pair(2, 0), 1, true),  // EASY_PUZZLE[2][0]에 1은 유효
    )

    /**
     * 테스트용 에러 시나리오 데이터
     */
    val ERROR_SCENARIOS = listOf(
        Pair(Pair(0, 2), 5), // 같은 행에 5가 이미 존재
        Pair(Pair(1, 1), 6), // 같은 열에 6이 이미 존재
        Pair(Pair(2, 2), 8)  // 같은 박스에 8이 이미 존재
    )
}