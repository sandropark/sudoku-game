package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SudokuViewModel의 핵심 기능 테스트
 * - 초기화, 셀 선택, 값 설정, 지우기 등 기본 기능
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SudokuViewModelCoreTest {

    @Test
    fun `초기 상태가 올바르게 설정되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val initialState = viewModel.state.first()

        // 초기 상태 확인
        assertEquals(-1, initialState.selectedRow)
        assertEquals(-1, initialState.selectedCol)
        assertFalse(initialState.isGameComplete)
        assertFalse(initialState.showError)
        assertEquals("", initialState.errorMessage)

        // 보드가 9x9 크기인지 확인
        assertEquals(9, initialState.board.size)
        assertEquals(9, initialState.board[0].size)
    }

    @Test
    fun `셀 선택이 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTimerTestViewModel()

        // 셀 선택
        viewModel.selectCell(3, 4)
        val state = viewModel.state.first()

        assertEquals(3, state.selectedRow)
        assertEquals(4, state.selectedCol)
        assertFalse(state.showError) // 에러 메시지가 사라짐
    }

    @Test
    fun `유효한 셀 값 설정이 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("편집 가능한 빈 셀이 존재해야 함", emptyCell != null)

        val (row, col) = emptyCell!!
        val validValue = SudokuTestHelper.findValidValue(state.board, row, col)
        assertTrue("유효한 값이 존재해야 함", validValue != null)

        viewModel.selectCell(row, col)
        viewModel.setCellValue(validValue!!)
        advanceUntilIdle()

        assertEquals(validValue, viewModel.state.value.board[row][col])

        viewModel.clearCell()
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.board[row][col])
    }

    @Test
    fun `셀 지우기가 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀이 존재해야 함", emptyCell != null)

        val (row, col) = emptyCell!!

        // 셀에 값 설정 후 지우기
        viewModel.selectCell(row, col)
        viewModel.setCellValue(4)
        viewModel.clearCell()

        val finalState = viewModel.state.first()
        assertEquals(0, finalState.board[row][col]) // 지워짐
    }

    @Test
    fun `초기 셀 확인이 올바르게 작동하는지 테스트`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 모든 셀에 대해 초기 셀 여부 확인
        for (row in 0..8) {
            for (col in 0..8) {
                val isInitial = viewModel.isInitialCell(row, col)
                val cellValue = viewModel.state.value.board[row][col]

                if (cellValue != 0) {
                    assertTrue("($row, $col)는 초기 셀이어야 함", isInitial)
                } else {
                    assertFalse("($row, $col)는 초기 셀이 아니어야 함", isInitial)
                }
            }
        }
    }

    @Test
    fun `셀이 선택되지 않은 상태에서 값 설정 시도 시 아무 일도 일어나지 않는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 초기 셀 찾기
        val state = viewModel.state.first()
        var initialRow = -1
        var initialCol = -1
        var initialValue = 0
        for (row in 0..8) {
            for (col in 0..8) {
                if (viewModel.isInitialCell(row, col)) {
                    initialRow = row
                    initialCol = col
                    initialValue = state.board[row][col]
                    break
                }
            }
            if (initialRow != -1) break
        }

        // 셀 선택 없이 값 설정 시도
        viewModel.setCellValue(5)

        val finalState = viewModel.state.first()
        // 초기값은 변경되지 않아야 함
        assertEquals(initialValue, finalState.board[initialRow][initialCol])
    }

    @Test
    fun `ViewModel 상태 일관성 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val initialState = viewModel.state.first()

        // 셀 선택
        viewModel.selectCell(0, 0)
        val afterSelection = viewModel.state.first()

        assertNotEquals("셀 선택 후 상태가 변경되어야 함", initialState, afterSelection)
        assertEquals("선택된 행이 올바르게 설정되어야 함", 0, afterSelection.selectedRow)
        assertEquals("선택된 열이 올바르게 설정되어야 함", 0, afterSelection.selectedCol)
        assertFalse("에러 상태가 초기화되어야 함", afterSelection.showError)
    }

    @Test
    fun `잘못된 셀 선택 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val validSelections = listOf(
            Pair(0, 0), Pair(4, 4), Pair(8, 8), Pair(0, 8), Pair(8, 0)
        )

        for ((row, col) in validSelections) {
            viewModel.selectCell(row, col)
            val state = viewModel.state.first()
            assertEquals("유효한 선택이 반영되어야 함", row, state.selectedRow)
            assertEquals("유효한 선택이 반영되어야 함", col, state.selectedCol)
        }
    }

    @Test
    fun `초기 상태 테스트`() {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val initialState = viewModel.state.value

        assertTrue("초기 상태가 null이 아니어야 함", initialState != null)
        assertTrue("보드가 null이 아니어야 함", initialState.board != null)
        assertTrue("초기 셀 정보가 null이 아니어야 함", initialState.isInitialCells != null)
        assertEquals("선택된 행이 -1이어야 함", -1, initialState.selectedRow)
        assertEquals("선택된 열이 -1이어야 함", -1, initialState.selectedCol)
        assertFalse("게임이 완료되지 않아야 함", initialState.isGameComplete)
        assertTrue("에러 상태가 비어있어야 함", initialState.invalidCells.isEmpty())
    }

    @Test
    fun `지우기 기능 종합 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        viewModel.newGame()
        advanceUntilIdle()

        val state = viewModel.state.value
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        if (emptyCell == null) return@runTest

        val (row, col) = emptyCell
        val validValue = SudokuTestHelper.findValidValue(state.board, row, col)
        if (validValue == null) return@runTest

        viewModel.selectCell(row, col)
        viewModel.setCellValue(validValue)
        advanceUntilIdle()
        assertEquals(validValue, viewModel.state.value.board[row][col])

        viewModel.clearCell()
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.board[row][col])
    }

    @Test
    fun `동시 작업 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val initialBoard = viewModel.state.first().board
        val emptyCell = SudokuTestHelper.findEmptyCell(initialBoard, viewModel)
        if (emptyCell == null) return@runTest

        val (row, col) = emptyCell

        // 빠른 연속 셀 선택
        repeat(5) {
            viewModel.selectCell(row, col)
            val state = viewModel.state.first()
            assertEquals("선택된 행이 올바르게 설정되어야 함", row, state.selectedRow)
            assertEquals("선택된 열이 올바르게 설정되어야 함", col, state.selectedCol)
        }

        // 빠른 연속 값 설정
        viewModel.selectCell(row, col)
        val validValue = SudokuTestHelper.findValidValue(initialBoard, row, col)
        if (validValue != null) {
            viewModel.setCellValue(validValue)
            val state = viewModel.state.first()
            assertEquals("설정된 값이 올바르게 저장되어야 함", validValue, state.board[row][col])
        }
    }
}