package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MistakeCountTest {

    @Test
    fun `초기 실수 카운트는 0이어야 한다`() = runTest {
        val viewModel = SudokuViewModel()
        val state = viewModel.state.value

        assertEquals(0, state.mistakeCount)
        assertFalse(state.isGameOver)
    }

    @Test
    fun `잘못된 값 입력시 실수 카운트가 증가해야 한다`() = runTest {
        val viewModel = SudokuTestHelper.createTimerTestViewModel()

        val state = viewModel.state.value

        // 잘못된 값을 입력할 수 있는 빈 셀 동적으로 찾기
        var testRow = -1
        var testCol = -1
        var invalidValue: Int? = null

        // 빈 셀들을 순회하며 잘못된 값을 입력할 수 있는 셀 찾기
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] == 0 && !viewModel.isInitialCell(row, col)) {
                    // 이 셀에 잘못된 값을 입력할 수 있는지 확인

                    // 같은 행에서 기존 값 찾기
                    val rowValues = state.board[row].filter { it != 0 }
                    if (rowValues.isNotEmpty()) {
                        testRow = row
                        testCol = col
                        invalidValue = rowValues.first()
                        break
                    }

                    // 같은 열에서 기존 값 찾기
                    val colValues = (0..8).map { state.board[it][col] }.filter { it != 0 }
                    if (colValues.isNotEmpty()) {
                        testRow = row
                        testCol = col
                        invalidValue = colValues.first()
                        break
                    }

                    // 같은 3x3 박스에서 기존 값 찾기
                    val boxStartRow = (row / 3) * 3
                    val boxStartCol = (col / 3) * 3
                    val boxValues = mutableListOf<Int>()
                    for (r in boxStartRow until boxStartRow + 3) {
                        for (c in boxStartCol until boxStartCol + 3) {
                            if (state.board[r][c] != 0) {
                                boxValues.add(state.board[r][c])
                            }
                        }
                    }
                    if (boxValues.isNotEmpty()) {
                        testRow = row
                        testCol = col
                        invalidValue = boxValues.first()
                        break
                    }
                }
            }
            if (testRow != -1) break
        }

        // 테스트할 수 있는 상황이 없으면 테스트 건너뛰기
        if (testRow == -1 || invalidValue == null) {
            return@runTest
        }

        // 잘못된 값 입력
        viewModel.selectCell(testRow, testCol)
        viewModel.setCellValue(invalidValue)

        val updatedState = viewModel.state.value
        assertEquals(1, updatedState.mistakeCount)
        assertFalse(updatedState.isGameOver)
    }

    @Test
    fun `올바른 값 입력시 실수 카운트가 증가하지 않아야 한다`() = runTest {
        val viewModel = SudokuTestHelper.createTimerTestViewModel()

        // 빈 셀 찾기
        val state = viewModel.state.value
        var emptyRow = -1
        var emptyCol = -1

        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("빈 셀을 찾을 수 있어야 함", emptyRow != -1)

        // 올바른 값 찾기 (해당 행/열/박스에 없는 숫자)
        var validValue = -1
        for (value in 1..9) {
            if (viewModel.getCellValue(emptyRow, emptyCol) == 0) { // 빈 셀인지 확인
                // 임시로 값을 설정해서 유효한지 확인
                viewModel.selectCell(emptyRow, emptyCol)
                if (isValidMove(state.board, emptyRow, emptyCol, value)) {
                    validValue = value
                    break
                }
            }
        }

        if (validValue != -1) {
            val initialMistakeCount = viewModel.state.value.mistakeCount
            viewModel.setCellValue(validValue)

            val updatedState = viewModel.state.value
            assertEquals(initialMistakeCount, updatedState.mistakeCount) // 실수 카운트 변경 없음
        }
    }

    @Test
    fun `실수 3번시 게임이 종료되어야 한다`() = runTest {
        val viewModel = SudokuTestHelper.createTimerTestViewModel()

        // 3번의 잘못된 입력을 시뮬레이션
        repeat(3) { mistake ->
            val state = viewModel.state.value

            // 잘못된 값을 입력할 수 있는 빈 셀 동적으로 찾기
            var testRow = -1
            var testCol = -1
            var invalidValue: Int? = null

            for (row in 0..8) {
                for (col in 0..8) {
                    if (state.board[row][col] == 0 && !viewModel.isInitialCell(row, col)) {
                        // 같은 행에서 기존 값 찾기
                        val rowValues = state.board[row].filter { it != 0 }
                        if (rowValues.isNotEmpty()) {
                            testRow = row
                            testCol = col
                            invalidValue = rowValues.first()
                            break
                        }

                        // 같은 열에서 기존 값 찾기
                        val colValues = (0..8).map { state.board[it][col] }.filter { it != 0 }
                        if (colValues.isNotEmpty()) {
                            testRow = row
                            testCol = col
                            invalidValue = colValues.first()
                            break
                        }

                        // 같은 3x3 박스에서 기존 값 찾기
                        val boxStartRow = (row / 3) * 3
                        val boxStartCol = (col / 3) * 3
                        val boxValues = mutableListOf<Int>()
                        for (r in boxStartRow until boxStartRow + 3) {
                            for (c in boxStartCol until boxStartCol + 3) {
                                if (state.board[r][c] != 0) {
                                    boxValues.add(state.board[r][c])
                                }
                            }
                        }
                        if (boxValues.isNotEmpty()) {
                            testRow = row
                            testCol = col
                            invalidValue = boxValues.first()
                            break
                        }
                    }
                }
                if (testRow != -1) break
            }

            if (testRow != -1 && invalidValue != null) {
                viewModel.selectCell(testRow, testCol)
                viewModel.setCellValue(invalidValue)

                val currentState = viewModel.state.value
                assertEquals(mistake + 1, currentState.mistakeCount)

                if (mistake == 2) { // 3번째 실수
                    assertTrue("3번째 실수 후 게임 종료 팝업이 표시되어야 함", currentState.showGameOverDialog)
                }
            }
        }
    }

    @Test
    fun `새 게임 시작시 실수 카운트가 초기화되어야 한다`() = runTest {
        val viewModel = SudokuTestHelper.createTimerTestViewModel()

        val state = viewModel.state.value

        // 잘못된 값을 입력할 수 있는 빈 셀 동적으로 찾기
        var testRow = -1
        var testCol = -1
        var invalidValue: Int? = null

        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] == 0 && !viewModel.isInitialCell(row, col)) {
                    // 같은 행에서 기존 값 찾기
                    val rowValues = state.board[row].filter { it != 0 }
                    if (rowValues.isNotEmpty()) {
                        testRow = row
                        testCol = col
                        invalidValue = rowValues.first()
                        break
                    }

                    // 같은 열에서 기존 값 찾기
                    val colValues = (0..8).map { state.board[it][col] }.filter { it != 0 }
                    if (colValues.isNotEmpty()) {
                        testRow = row
                        testCol = col
                        invalidValue = colValues.first()
                        break
                    }

                    // 같은 3x3 박스에서 기존 값 찾기
                    val boxStartRow = (row / 3) * 3
                    val boxStartCol = (col / 3) * 3
                    val boxValues = mutableListOf<Int>()
                    for (r in boxStartRow until boxStartRow + 3) {
                        for (c in boxStartCol until boxStartCol + 3) {
                            if (state.board[r][c] != 0) {
                                boxValues.add(state.board[r][c])
                            }
                        }
                    }
                    if (boxValues.isNotEmpty()) {
                        testRow = row
                        testCol = col
                        invalidValue = boxValues.first()
                        break
                    }
                }
            }
            if (testRow != -1) break
        }

        if (testRow != -1 && invalidValue != null) {
            viewModel.selectCell(testRow, testCol)
            viewModel.setCellValue(invalidValue)

            // 실수 카운트가 증가했는지 확인
            assertTrue("실수 카운트가 0보다 커야 함", viewModel.state.value.mistakeCount > 0)

            // 새 게임 시작
            viewModel.newGame()

            val newGameState = viewModel.state.value
            assertEquals("새 게임에서 실수 카운트가 0이어야 함", 0, newGameState.mistakeCount)
            assertFalse("새 게임에서 게임 종료 상태가 아니어야 함", newGameState.isGameOver)
        }
    }

    // 헬퍼 메서드: 유효한 움직임인지 확인
    private fun isValidMove(board: Array<IntArray>, row: Int, col: Int, value: Int): Boolean {
        // 행 검사
        for (c in 0..8) {
            if (c != col && board[row][c] == value) return false
        }

        // 열 검사
        for (r in 0..8) {
            if (r != row && board[r][col] == value) return false
        }

        // 3x3 박스 검사
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != row || c != col) && board[r][c] == value) return false
            }
        }

        return true
    }
} 