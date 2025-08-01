package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 노트 하이라이트 기능 테스트
 * 셀의 숫자 선택 시 같은 노트의 숫자도 하이라이트 되는 기능을 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteHighlightTest {

    @Test
    fun testHighlightedNumber_initiallyZero() = runTest {
        // Given: 초기 상태
        val viewModel = SudokuTestHelper.createTestViewModel()
        val state = viewModel.state.first()

        // Then: highlightedNumber는 0이어야 함
        assertEquals(0, state.highlightedNumber)
    }

    @Test
    fun testHighlightedNumber_whenCellWithNumberSelected() = runTest {
        // Given: 보드에 숫자 5가 있는 셀
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 찾기
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)
        val (row, col) = emptyCell!!

        viewModel.selectCell(row, col)
        viewModel.setCellValue(5)

        // When: 해당 셀을 다시 선택
        viewModel.selectCell(row, col)

        // Then: highlightedNumber는 5여야 함
        val finalState = viewModel.state.first()
        assertEquals(5, finalState.highlightedNumber)
    }

    @Test
    fun testHighlightedNumber_whenEmptyCellSelected() = runTest {
        // Given: 빈 셀
        val viewModel = SudokuTestHelper.createTestViewModel()
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)
        val (row, col) = emptyCell!!

        // When: 빈 셀을 선택
        viewModel.selectCell(row, col)

        // Then: highlightedNumber는 0이어야 함
        val finalState = viewModel.state.first()
        assertEquals(0, finalState.highlightedNumber)
    }

    @Test
    fun testHighlightedNumber_whenCellWithNotesSelected() = runTest {
        // Given: 노트가 있는 셀
        val viewModel = SudokuTestHelper.createTestViewModel()
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)
        val (row, col) = emptyCell!!

        viewModel.selectCell(row, col)
        viewModel.toggleNoteMode()
        viewModel.addNoteNumber(3)
        viewModel.addNoteNumber(7)

        // When: 해당 셀을 선택
        viewModel.selectCell(row, col)

        // Then: highlightedNumber는 0이어야 함 (노트가 있는 셀은 하이라이트 숫자가 없음)
        val finalState = viewModel.state.first()
        assertEquals(0, finalState.highlightedNumber)
    }

    @Test
    fun testHighlightedNumber_whenNumberInputted() = runTest {
        // Given: 빈 셀 선택
        val viewModel = SudokuTestHelper.createTestViewModel()
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)
        val (row, col) = emptyCell!!

        viewModel.selectCell(row, col)

        // When: 숫자 8을 입력
        viewModel.setCellValue(8)

        // Then: highlightedNumber는 8이어야 함
        val finalState = viewModel.state.first()
        assertEquals(8, finalState.highlightedNumber)
    }

    @Test
    fun testHighlightedNumber_whenNumberRemoved() = runTest {
        // Given: 숫자가 있는 셀
        val viewModel = SudokuTestHelper.createTestViewModel()
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)
        val (row, col) = emptyCell!!

        viewModel.selectCell(row, col)
        viewModel.setCellValue(4)

        // When: 숫자를 제거 (같은 숫자 다시 입력)
        viewModel.setCellValue(4)

        // Then: highlightedNumber는 0이어야 함
        val finalState = viewModel.state.first()
        assertEquals(0, finalState.highlightedNumber)
    }

    @Test
    fun testHighlightedNumber_whenDifferentCellSelected() = runTest {
        // Given: 첫 번째 셀에 숫자 6 입력
        val viewModel = SudokuTestHelper.createTestViewModel()
        val state = viewModel.state.first()
        val emptyCell1 = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell1 != null)
        val (row1, col1) = emptyCell1!!

        viewModel.selectCell(row1, col1)
        viewModel.setCellValue(6)

        // When: 다른 빈 셀을 선택
        val updatedState = viewModel.state.first()
        val emptyCell2 = SudokuTestHelper.findEmptyCell(updatedState.board, viewModel)
        assertTrue("두 번째 빈 셀을 찾을 수 있어야 함", emptyCell2 != null)
        val (row2, col2) = emptyCell2!!

        viewModel.selectCell(row2, col2)

        // Then: highlightedNumber는 0이어야 함
        val finalState = viewModel.state.first()
        assertEquals(0, finalState.highlightedNumber)
    }

    @Test
    fun testHighlightedNumber_whenSameNumberCellSelected() = runTest {
        // Given: 두 셀에 같은 숫자 9 입력
        val viewModel = SudokuTestHelper.createTestViewModel()
        val state = viewModel.state.first()
        val emptyCell1 = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell1 != null)
        val (row1, col1) = emptyCell1!!

        viewModel.selectCell(row1, col1)
        viewModel.setCellValue(9)

        val updatedState = viewModel.state.first()
        val emptyCell2 = SudokuTestHelper.findEmptyCell(updatedState.board, viewModel)
        assertTrue("두 번째 빈 셀을 찾을 수 있어야 함", emptyCell2 != null)
        val (row2, col2) = emptyCell2!!

        viewModel.selectCell(row2, col2)
        viewModel.setCellValue(9)

        // When: 첫 번째 셀을 다시 선택
        viewModel.selectCell(row1, col1)

        // Then: highlightedNumber는 9여야 함
        val finalState = viewModel.state.first()
        assertEquals(9, finalState.highlightedNumber)
    }

    @Test
    fun testHighlightedNumber_persistsAcrossSelections() = runTest {
        // Given: 숫자 2가 있는 셀
        val viewModel = SudokuTestHelper.createTestViewModel()
        val state = viewModel.state.first()
        val emptyCell1 = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell1 != null)
        val (row1, col1) = emptyCell1!!

        viewModel.selectCell(row1, col1)
        viewModel.setCellValue(2)

        // When: 다른 셀을 선택한 후 다시 원래 셀 선택
        val updatedState = viewModel.state.first()
        val emptyCell2 = SudokuTestHelper.findEmptyCell(updatedState.board, viewModel)
        assertTrue("두 번째 빈 셀을 찾을 수 있어야 함", emptyCell2 != null)
        val (row2, col2) = emptyCell2!!

        viewModel.selectCell(row2, col2)
        viewModel.selectCell(row1, col1)

        // Then: highlightedNumber는 여전히 2여야 함
        val finalState = viewModel.state.first()
        assertEquals(2, finalState.highlightedNumber)
    }
}
