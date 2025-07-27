package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RowColHighlightTest {

    @Test
    fun `셀 선택 시 해당 행과 열이 하이라이트되어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 특정 셀 선택 (3, 5)
        val selectedRow = 3
        val selectedCol = 5
        viewModel.selectCell(selectedRow, selectedCol)

        val state = viewModel.state.value
        assertEquals("선택된 행이 설정되어야 함", selectedRow, state.selectedRow)
        assertEquals("선택된 열이 설정되어야 함", selectedCol, state.selectedCol)
        assertTrue("선택된 행이 하이라이트되어야 함", state.highlightedRows.contains(selectedRow))
        assertTrue("선택된 열이 하이라이트되어야 함", state.highlightedCols.contains(selectedCol))
    }

    @Test
    fun `다른 셀 선택 시 행과 열 하이라이트가 변경되어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 첫 번째 셀 선택 (2, 4)
        val firstRow = 2
        val firstCol = 4
        viewModel.selectCell(firstRow, firstCol)

        val firstState = viewModel.state.value
        assertTrue("첫 번째 행이 하이라이트되어야 함", firstState.highlightedRows.contains(firstRow))
        assertTrue("첫 번째 열이 하이라이트되어야 함", firstState.highlightedCols.contains(firstCol))

        // 두 번째 셀 선택 (6, 1)
        val secondRow = 6
        val secondCol = 1
        viewModel.selectCell(secondRow, secondCol)

        val secondState = viewModel.state.value
        assertTrue("두 번째 행이 하이라이트되어야 함", secondState.highlightedRows.contains(secondRow))
        assertTrue("두 번째 열이 하이라이트되어야 함", secondState.highlightedCols.contains(secondCol))
        assertFalse("이전 행 하이라이트가 해제되어야 함", secondState.highlightedRows.contains(firstRow))
        assertFalse("이전 열 하이라이트가 해제되어야 함", secondState.highlightedCols.contains(firstCol))
    }

    @Test
    fun `셀 선택 해제 시 행과 열 하이라이트가 사라져야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 셀 선택
        val selectedRow = 4
        val selectedCol = 7
        viewModel.selectCell(selectedRow, selectedCol)

        val selectedState = viewModel.state.value
        assertTrue("행이 하이라이트되어야 함", selectedState.highlightedRows.contains(selectedRow))
        assertTrue("열이 하이라이트되어야 함", selectedState.highlightedCols.contains(selectedCol))

        // 선택 해제
        viewModel.clearSelection()

        val clearedState = viewModel.state.value
        assertTrue("선택 해제 시 하이라이트된 행이 없어야 함", clearedState.highlightedRows.isEmpty())
        assertTrue("선택 해제 시 하이라이트된 열이 없어야 함", clearedState.highlightedCols.isEmpty())
    }

    @Test
    fun `행과 열 하이라이트는 하나씩만 활성화되어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 셀 선택
        val selectedRow = 1
        val selectedCol = 8
        viewModel.selectCell(selectedRow, selectedCol)

        val state = viewModel.state.value
        assertEquals("하이라이트된 행이 하나여야 함", 1, state.highlightedRows.size)
        assertEquals("하이라이트된 열이 하나여야 함", 1, state.highlightedCols.size)
        assertTrue("선택된 행만 하이라이트되어야 함", state.highlightedRows.contains(selectedRow))
        assertTrue("선택된 열만 하이라이트되어야 함", state.highlightedCols.contains(selectedCol))
    }

    @Test
    fun `숫자 하이라이트와 행열 하이라이트가 동시에 작동해야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value

        // 숫자가 있는 셀 찾기
        var targetRow = -1
        var targetCol = -1
        var targetNumber = 0

        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0) {
                    targetRow = row
                    targetCol = col
                    targetNumber = state.board[row][col]
                    break
                }
            }
            if (targetRow != -1) break
        }

        assertTrue("테스트를 위한 숫자가 있는 셀이 존재해야 함", targetRow != -1)

        // 해당 셀 선택
        viewModel.selectCell(targetRow, targetCol)

        val updatedState = viewModel.state.value

        // 숫자 하이라이트 확인
        assertEquals("선택된 숫자가 하이라이트되어야 함", targetNumber, updatedState.highlightedNumber)
        assertFalse("같은 숫자들이 하이라이트되어야 함", updatedState.highlightedCells.isEmpty())

        // 행열 하이라이트 확인
        assertTrue("선택된 행이 하이라이트되어야 함", updatedState.highlightedRows.contains(targetRow))
        assertTrue("선택된 열이 하이라이트되어야 함", updatedState.highlightedCols.contains(targetCol))
    }

    @Test
    fun `빈 셀 선택 시에도 행과 열은 하이라이트되어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value

        // 빈 셀 찾기
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

        assertTrue("테스트를 위한 빈 셀이 존재해야 함", emptyRow != -1)

        // 빈 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)

        val updatedState = viewModel.state.value

        // 숫자 하이라이트는 없어야 함
        assertEquals("빈 셀 선택 시 하이라이트된 숫자가 0이어야 함", 0, updatedState.highlightedNumber)
        assertTrue("빈 셀 선택 시 하이라이트된 셀이 없어야 함", updatedState.highlightedCells.isEmpty())

        // 행열 하이라이트는 있어야 함
        assertTrue("빈 셀이라도 행은 하이라이트되어야 함", updatedState.highlightedRows.contains(emptyRow))
        assertTrue("빈 셀이라도 열은 하이라이트되어야 함", updatedState.highlightedCols.contains(emptyCol))
    }
} 