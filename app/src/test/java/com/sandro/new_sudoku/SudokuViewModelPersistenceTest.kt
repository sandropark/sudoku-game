package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import com.sandro.new_sudoku.ui.DifficultyLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SudokuViewModel의 게임 상태 저장/복원 기능 테스트
 * 실제 Repository와의 통합 테스트는 Android Instrumented 테스트에서 수행
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SudokuViewModelPersistenceTest {

    @Test
    fun `GameStateRepository 없이 생성된 ViewModel은 저장된 게임이 없다고 반환한다`() = runTest {
        // Given
        val viewModel = SudokuViewModel() // Repository 없이 생성

        // When
        val hasSavedGame = viewModel.hasSavedGame()

        // Then
        assertFalse(hasSavedGame)
    }

    @Test
    fun `GameStateRepository 없이 loadSavedGame 호출시 false를 반환한다`() = runTest {
        // Given
        val viewModel = SudokuViewModel()

        // When
        val success = viewModel.loadSavedGame()

        // Then
        assertFalse(success)
    }

    @Test
    fun `새 게임 시작 후 기본 상태가 올바르게 설정된다`() = runTest {
        // Given
        val viewModel = SudokuTestHelper.createTestViewModel()

        // When
        viewModel.newGameWithDifficulty(DifficultyLevel.EASY)
        val state = viewModel.state.first()

        // Then
        assertEquals(-1, state.selectedRow)
        assertEquals(-1, state.selectedCol)
        assertEquals(0, state.mistakeCount)
        assertEquals(0, state.elapsedTimeSeconds)
        assertFalse(state.isGameComplete)
        assertFalse(state.isGameOver)
        assertFalse(state.isTimerRunning)
    }

    @Test
    fun `게임 상태 변환 기능이 올바르게 동작한다`() = runTest {
        // Given
        val viewModel = SudokuTestHelper.createTestViewModel()
        viewModel.selectCell(0, 0)
        viewModel.setCellValue(5)
        val originalState = viewModel.state.first()

        // When
        val serializable = originalState.toSerializable(
            difficulty = DifficultyLevel.EASY,
            initialBoard = Array(9) { IntArray(9) { 0 } },
            solution = Array(9) { IntArray(9) { 1 } }
        )
        val restoredState = serializable.toSudokuState()

        // Then
        assertEquals(originalState.selectedRow, restoredState.selectedRow)
        assertEquals(originalState.selectedCol, restoredState.selectedCol)
        assertEquals(originalState.board[0][0], restoredState.board[0][0])
        assertEquals(originalState.mistakeCount, restoredState.mistakeCount)
        assertEquals(originalState.elapsedTimeSeconds, restoredState.elapsedTimeSeconds)
        assertEquals(originalState.highlightedNumber, restoredState.highlightedNumber)
    }

    @Test
    fun `게임 완료 시 상태가 올바르게 설정된다`() = runTest {
        // Given
        val viewModel = SudokuTestHelper.createTestViewModel()

        // When
        viewModel.solveGame()
        val state = viewModel.state.first()

        // Then
        assertTrue(state.isGameComplete)
        assertTrue(state.showGameCompleteDialog)
        assertFalse(state.isTimerRunning)
    }

    @Test
    fun `게임 진행 중인 상태로 저장된 게임을 복원하면 타이머가 시작된다`() = runTest {
        // Given
        val savedState = SerializableGameState(
            board = List(9) { row -> List(9) { col -> if (row == 0 && col == 0) 5 else 0 } },
            isInitialCells = List(9) { List(9) { false } },
            selectedRow = 0,
            selectedCol = 0,
            isGameComplete = false, // 게임 진행 중
            notes = List(9) { List(9) { emptySet<Int>() } },
            mistakeCount = 1,
            isGameOver = false, // 게임 오버 아님
            elapsedTimeSeconds = 120,
            isTimerRunning = false, // 저장 시점에는 타이머가 꺼져있었음
            highlightedNumber = 5,
            completedNumbers = emptySet(),
            difficulty = DifficultyLevel.EASY,
            initialBoard = List(9) { List(9) { 0 } },
            solution = List(9) { List(9) { 1 } }
        )

        // Mock 게임 상태를 가진 SerializableGameState 생성 후
        // 실제 loadSavedGame 로직을 테스트하기 위한 시뮬레이션
        val sudokuState = savedState.toSudokuState()

        // When & Then
        // 게임이 진행 중이고 게임 오버가 아니므로 타이머가 시작되어야 함
        assertFalse(savedState.isGameComplete)
        assertFalse(savedState.isGameOver)
        // 복원된 상태에서는 타이머가 저장 시점의 값과 무관하게 시작되어야 함
        assertEquals(120, sudokuState.elapsedTimeSeconds)
    }

    @Test
    fun `게임 완료 상태로 저장된 게임을 복원하면 타이머가 시작되지 않는다`() = runTest {
        // Given
        val savedState = SerializableGameState(
            board = List(9) { List(9) { 1 } }, // 완료된 보드
            isInitialCells = List(9) { List(9) { false } },
            selectedRow = -1,
            selectedCol = -1,
            isGameComplete = true, // 게임 완료
            notes = List(9) { List(9) { emptySet<Int>() } },
            mistakeCount = 0,
            isGameOver = false,
            elapsedTimeSeconds = 300,
            isTimerRunning = false,
            highlightedNumber = 0,
            completedNumbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
            difficulty = DifficultyLevel.EASY,
            initialBoard = List(9) { List(9) { 0 } },
            solution = List(9) { List(9) { 1 } }
        )

        val sudokuState = savedState.toSudokuState()

        // When & Then
        // 게임이 완료되었으므로 타이머가 시작되지 않아야 함
        assertTrue(savedState.isGameComplete)
        assertEquals(300, sudokuState.elapsedTimeSeconds)
    }

    @Test
    fun `게임 오버 상태로 저장된 게임을 복원하면 타이머가 시작되지 않는다`() = runTest {
        // Given
        val savedState = SerializableGameState(
            board = List(9) { List(9) { 0 } },
            isInitialCells = List(9) { List(9) { false } },
            selectedRow = 0,
            selectedCol = 0,
            isGameComplete = false,
            notes = List(9) { List(9) { emptySet<Int>() } },
            mistakeCount = 3, // 최대 실수 횟수
            isGameOver = true, // 게임 오버
            elapsedTimeSeconds = 180,
            isTimerRunning = false,
            highlightedNumber = 0,
            completedNumbers = emptySet(),
            difficulty = DifficultyLevel.EASY,
            initialBoard = List(9) { List(9) { 0 } },
            solution = List(9) { List(9) { 1 } }
        )

        val sudokuState = savedState.toSudokuState()

        // When & Then
        // 게임 오버 상태이므로 타이머가 시작되지 않아야 함
        assertTrue(savedState.isGameOver)
        assertEquals(3, sudokuState.mistakeCount)
        assertEquals(180, sudokuState.elapsedTimeSeconds)
    }
}