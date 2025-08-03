package com.sandro.new_sudoku

import com.sandro.new_sudoku.ui.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * GameStateRepository의 기본 데이터 구조 테스트
 * 실제 저장/로드 테스트는 Android Instrumented 테스트에서 수행
 */
class GameStateRepositoryTest {

    @Test
    fun `SerializableGameState 변환 테스트`() {
        // Given
        val sudokuState = SudokuState(
            board = Array(9) { IntArray(9) { 0 } },
            isInitialCells = Array(9) { BooleanArray(9) { false } },
            selectedRow = 1,
            selectedCol = 2,
            isGameComplete = false,
            notes = Array(9) { Array(9) { emptySet<Int>() } },
            mistakeCount = 1,
            isGameOver = false,
            elapsedTimeSeconds = 120,
            isTimerRunning = true,
            highlightedNumber = 5,
            completedNumbers = setOf(1, 2)
        )
        val difficulty = DifficultyLevel.EASY
        val initialBoard = Array(9) { IntArray(9) { 0 } }
        val solution = Array(9) { IntArray(9) { 1 } }

        // When
        val serializable = sudokuState.toSerializable(difficulty, initialBoard, solution)
        val converted = serializable.toSudokuState()

        // Then
        assertEquals(sudokuState.selectedRow, converted.selectedRow)
        assertEquals(sudokuState.selectedCol, converted.selectedCol)
        assertEquals(sudokuState.isGameComplete, converted.isGameComplete)
        assertEquals(sudokuState.mistakeCount, converted.mistakeCount)
        assertEquals(sudokuState.isGameOver, converted.isGameOver)
        assertEquals(sudokuState.elapsedTimeSeconds, converted.elapsedTimeSeconds)
        assertEquals(sudokuState.isTimerRunning, converted.isTimerRunning)
        assertEquals(sudokuState.highlightedNumber, converted.highlightedNumber)
        assertEquals(sudokuState.completedNumbers, converted.completedNumbers)
    }

    @Test
    fun `SerializableGameState 기본 데이터 검증`() {
        // Given
        val gameState = SerializableGameState(
            board = List(9) { List(9) { 0 } },
            isInitialCells = List(9) { List(9) { false } },
            selectedRow = 1,
            selectedCol = 2,
            isGameComplete = false,
            notes = List(9) { List(9) { emptySet<Int>() } },
            mistakeCount = 1,
            isGameOver = false,
            elapsedTimeSeconds = 120,
            isTimerRunning = true,
            highlightedNumber = 5,
            completedNumbers = setOf(1, 2),
            difficulty = DifficultyLevel.EASY,
            initialBoard = List(9) { List(9) { 0 } },
            solution = List(9) { List(9) { 1 } }
        )

        // When & Then
        assertEquals(9, gameState.board.size)
        assertEquals(9, gameState.board[0].size)
        assertEquals(1, gameState.selectedRow)
        assertEquals(2, gameState.selectedCol)
        assertEquals(false, gameState.isGameComplete)
        assertEquals(1, gameState.mistakeCount)
        assertEquals(false, gameState.isGameOver)
        assertEquals(120, gameState.elapsedTimeSeconds)
        assertEquals(true, gameState.isTimerRunning)
        assertEquals(5, gameState.highlightedNumber)
        assertEquals(setOf(1, 2), gameState.completedNumbers)
        assertEquals(DifficultyLevel.EASY, gameState.difficulty)
    }
}