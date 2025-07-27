package com.sandro.new_sudoku

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberToggleTest {

    @Test
    fun `빈 셀에 숫자 입력 후 같은 숫자 다시 입력하면 지워져야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 빈 셀 찾기 (초기 셀이 아닌 셀)
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

        assertTrue("편집 가능한 빈 셀을 찾을 수 있어야 함", emptyRow != -1)

        // 유효한 값 찾기
        val validValue = (1..9).firstOrNull { value ->
            isValidMove(state.board, emptyRow, emptyCol, value)
        }

        assertNotNull("유효한 값을 찾을 수 있어야 함", validValue)

        // 1. 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)

        // 2. 숫자 입력
        viewModel.setCellValue(validValue!!)

        var updatedState = viewModel.state.value
        assertEquals("숫자가 입력되어야 함", validValue, updatedState.board[emptyRow][emptyCol])

        // 3. 같은 숫자 다시 입력 (토글 - 지워짐)
        viewModel.setCellValue(validValue)

        updatedState = viewModel.state.value
        assertEquals("같은 숫자를 다시 누르면 지워져야 함", 0, updatedState.board[emptyRow][emptyCol])
    }

    @Test
    fun `다른 숫자를 입력하면 기존 숫자가 교체되어야 한다`() = runTest {
        val viewModel = SudokuViewModel()

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

        assertTrue("편집 가능한 빈 셀을 찾을 수 있어야 함", emptyRow != -1)

        // 유효한 값 2개 찾기
        val validValues = (1..9).filter { value ->
            isValidMove(state.board, emptyRow, emptyCol, value)
        }.take(2)

        assertTrue("최소 2개의 유효한 값을 찾을 수 있어야 함", validValues.size >= 2)

        val firstValue = validValues[0]
        val secondValue = validValues[1]

        // 1. 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)

        // 2. 첫 번째 숫자 입력
        viewModel.setCellValue(firstValue)

        var updatedState = viewModel.state.value
        assertEquals("첫 번째 숫자가 입력되어야 함", firstValue, updatedState.board[emptyRow][emptyCol])

        // 3. 다른 숫자 입력 (교체)
        viewModel.setCellValue(secondValue)

        updatedState = viewModel.state.value
        assertEquals("다른 숫자로 교체되어야 함", secondValue, updatedState.board[emptyRow][emptyCol])
    }

    @Test
    fun `초기 셀에서는 같은 숫자를 눌러도 지워지지 않아야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 초기 셀 찾기
        val state = viewModel.state.value
        var initialRow = -1
        var initialCol = -1
        var initialValue = -1

        for (row in 0..8) {
            for (col in 0..8) {
                if (viewModel.isInitialCell(row, col) && state.board[row][col] != 0) {
                    initialRow = row
                    initialCol = col
                    initialValue = state.board[row][col]
                    break
                }
            }
            if (initialRow != -1) break
        }

        assertTrue("초기 셀을 찾을 수 있어야 함", initialRow != -1)

        // 1. 초기 셀 선택
        viewModel.selectCell(initialRow, initialCol)

        // 2. 같은 값 입력 시도 (아무 일도 일어나지 않아야 함)
        viewModel.setCellValue(initialValue)

        val updatedState = viewModel.state.value
        assertEquals(
            "초기 셀의 값은 변경되지 않아야 함",
            initialValue,
            updatedState.board[initialRow][initialCol]
        )
        assertTrue("초기 셀 속성이 유지되어야 함", viewModel.isInitialCell(initialRow, initialCol))
    }

    @Test
    fun `빈 셀에 0을 입력해도 변화가 없어야 한다`() = runTest {
        val viewModel = SudokuViewModel()

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

        assertTrue("편집 가능한 빈 셀을 찾을 수 있어야 함", emptyRow != -1)

        // 1. 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)

        // 2. 0 입력 (빈 셀에 0 입력)
        viewModel.setCellValue(0)

        val updatedState = viewModel.state.value
        assertEquals("빈 셀에 0을 입력해도 여전히 0이어야 함", 0, updatedState.board[emptyRow][emptyCol])
    }

    @Test
    fun `채워진 셀에 0을 입력하면 지워져야 한다`() = runTest {
        val viewModel = SudokuViewModel()

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

        assertTrue("편집 가능한 빈 셀을 찾을 수 있어야 함", emptyRow != -1)

        // 유효한 값 찾기
        val validValue = (1..9).firstOrNull { value ->
            isValidMove(state.board, emptyRow, emptyCol, value)
        }

        assertNotNull("유효한 값을 찾을 수 있어야 함", validValue)

        // 1. 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)

        // 2. 숫자 입력
        viewModel.setCellValue(validValue!!)

        var updatedState = viewModel.state.value
        assertEquals("숫자가 입력되어야 함", validValue, updatedState.board[emptyRow][emptyCol])

        // 3. 0 입력 (지우기)
        viewModel.setCellValue(0)

        updatedState = viewModel.state.value
        assertEquals("0을 입력하면 지워져야 함", 0, updatedState.board[emptyRow][emptyCol])
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