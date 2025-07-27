package com.sandro.new_sudoku

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RowColHighlightUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: SudokuViewModel

    @Test
    fun testRowColHighlightWhenSelectingCell() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 특정 셀 선택 (3행 5열)
        val selectedRow = 3
        val selectedCol = 5
        val cellTag = if (viewModel.isInitialCell(selectedRow, selectedCol)) {
            "cell_${selectedRow}_${selectedCol}"
        } else {
            "cell_${selectedRow}_${selectedCol}_editable"
        }

        composeTestRule.onNodeWithTag(cellTag).performClick()
        composeTestRule.waitForIdle()

        // ViewModel 상태 확인
        val state = viewModel.state.value
        assert(state.selectedRow == selectedRow) { "선택된 행이 설정되어야 함" }
        assert(state.selectedCol == selectedCol) { "선택된 열이 설정되어야 함" }
        assert(state.highlightedRows.contains(selectedRow)) { "선택된 행이 하이라이트되어야 함" }
        assert(state.highlightedCols.contains(selectedCol)) { "선택된 열이 하이라이트되어야 함" }
    }

    @Test
    fun testRowColHighlightChangesWhenSelectingDifferentCell() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 첫 번째 셀 선택
        val firstRow = 1
        val firstCol = 2
        val firstCellTag = if (viewModel.isInitialCell(firstRow, firstCol)) {
            "cell_${firstRow}_${firstCol}"
        } else {
            "cell_${firstRow}_${firstCol}_editable"
        }

        composeTestRule.onNodeWithTag(firstCellTag).performClick()
        composeTestRule.waitForIdle()

        val firstState = viewModel.state.value
        assert(firstState.highlightedRows.contains(firstRow)) { "첫 번째 행이 하이라이트되어야 함" }
        assert(firstState.highlightedCols.contains(firstCol)) { "첫 번째 열이 하이라이트되어야 함" }

        // 두 번째 셀 선택
        val secondRow = 6
        val secondCol = 7
        val secondCellTag = if (viewModel.isInitialCell(secondRow, secondCol)) {
            "cell_${secondRow}_${secondCol}"
        } else {
            "cell_${secondRow}_${secondCol}_editable"
        }

        composeTestRule.onNodeWithTag(secondCellTag).performClick()
        composeTestRule.waitForIdle()

        val secondState = viewModel.state.value
        assert(secondState.highlightedRows.contains(secondRow)) { "두 번째 행이 하이라이트되어야 함" }
        assert(secondState.highlightedCols.contains(secondCol)) { "두 번째 열이 하이라이트되어야 함" }
        assert(!secondState.highlightedRows.contains(firstRow)) { "이전 행 하이라이트가 해제되어야 함" }
        assert(!secondState.highlightedCols.contains(firstCol)) { "이전 열 하이라이트가 해제되어야 함" }
    }

    @Test
    fun testRowColHighlightWithEmptyCell() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

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

        assert(emptyRow != -1) { "테스트를 위한 빈 셀이 존재해야 함" }

        // 빈 셀 선택
        val cellTag = "cell_${emptyRow}_${emptyCol}_editable"
        composeTestRule.onNodeWithTag(cellTag).performClick()
        composeTestRule.waitForIdle()

        val finalState = viewModel.state.value

        // 숫자 하이라이트는 없어야 함
        assert(finalState.highlightedNumber == 0) { "빈 셀 선택 시 하이라이트된 숫자가 0이어야 함" }
        assert(finalState.highlightedCells.isEmpty()) { "빈 셀 선택 시 하이라이트된 셀이 없어야 함" }

        // 행열 하이라이트는 있어야 함
        assert(finalState.highlightedRows.contains(emptyRow)) { "빈 셀이라도 행은 하이라이트되어야 함" }
        assert(finalState.highlightedCols.contains(emptyCol)) { "빈 셀이라도 열은 하이라이트되어야 함" }
    }

    @Test
    fun testNumberAndRowColHighlightTogether() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

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

        assert(targetRow != -1) { "테스트를 위한 숫자가 있는 셀이 존재해야 함" }

        // 해당 셀 선택
        val cellTag = "cell_${targetRow}_${targetCol}"
        composeTestRule.onNodeWithTag(cellTag).performClick()
        composeTestRule.waitForIdle()

        val finalState = viewModel.state.value

        // 숫자 하이라이트 확인
        assert(finalState.highlightedNumber == targetNumber) { "선택된 숫자가 하이라이트되어야 함" }
        assert(finalState.highlightedCells.isNotEmpty()) { "같은 숫자들이 하이라이트되어야 함" }

        // 행열 하이라이트 확인
        assert(finalState.highlightedRows.contains(targetRow)) { "선택된 행이 하이라이트되어야 함" }
        assert(finalState.highlightedCols.contains(targetCol)) { "선택된 열이 하이라이트되어야 함" }
    }
} 