package com.sandro.new_sudoku

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NumberHighlightUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: SudokuViewModel

    @Test
    fun testNumberHighlightWhenSelectingCell() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        val state = viewModel.state.value

        // 숫자가 있는 초기 셀 찾기
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

        // 해당 셀 클릭
        val cellTag = "cell_${targetRow}_${targetCol}"
        composeTestRule.onNodeWithTag(cellTag).performClick()
        composeTestRule.waitForIdle()

        // 하이라이트가 적용되었는지 확인 (같은 숫자를 가진 다른 셀들)
        val finalState = viewModel.state.value
        assert(finalState.highlightedNumber == targetNumber) {
            "선택된 숫자가 하이라이트되어야 함: 예상=${targetNumber}, 실제=${finalState.highlightedNumber}"
        }
        assert(finalState.highlightedCells.isNotEmpty()) { "하이라이트된 셀이 있어야 함" }
        assert(finalState.highlightedCells.contains(Pair(targetRow, targetCol))) {
            "선택된 셀 자체도 하이라이트되어야 함"
        }
    }

    @Test
    fun testNoHighlightWhenSelectingEmptyCell() {
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

        // 빈 셀 클릭
        val cellTag = "cell_${emptyRow}_${emptyCol}_editable"
        composeTestRule.onNodeWithTag(cellTag).performClick()
        composeTestRule.waitForIdle()

        // 하이라이트가 없어야 함
        val finalState = viewModel.state.value
        assert(finalState.highlightedNumber == 0) { "빈 셀 선택 시 하이라이트된 숫자가 0이어야 함" }
        assert(finalState.highlightedCells.isEmpty()) { "빈 셀 선택 시 하이라이트된 셀이 없어야 함" }
    }

    @Test
    fun testHighlightChangesWhenSelectingDifferentNumbers() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        val state = viewModel.state.value

        // 첫 번째와 두 번째 서로 다른 숫자가 있는 셀 찾기
        var firstRow = -1
        var firstCol = -1
        var firstNumber = 0
        var secondRow = -1
        var secondCol = -1
        var secondNumber = 0

        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0) {
                    if (firstRow == -1) {
                        firstRow = row
                        firstCol = col
                        firstNumber = state.board[row][col]
                    } else if (state.board[row][col] != firstNumber) {
                        secondRow = row
                        secondCol = col
                        secondNumber = state.board[row][col]
                        break
                    }
                }
            }
            if (secondRow != -1) break
        }

        assert(firstRow != -1 && secondRow != -1) { "서로 다른 숫자를 가진 두 셀이 존재해야 함" }

        // 첫 번째 셀 클릭
        val firstCellTag = "cell_${firstRow}_${firstCol}"
        composeTestRule.onNodeWithTag(firstCellTag).performClick()
        composeTestRule.waitForIdle()

        val firstState = viewModel.state.value
        assert(firstState.highlightedNumber == firstNumber) { "첫 번째 숫자가 하이라이트되어야 함" }

        // 두 번째 셀 클릭
        val secondCellTag = "cell_${secondRow}_${secondCol}"
        composeTestRule.onNodeWithTag(secondCellTag).performClick()
        composeTestRule.waitForIdle()

        val secondState = viewModel.state.value
        assert(secondState.highlightedNumber == secondNumber) { "두 번째 숫자가 하이라이트되어야 함" }
        assert(secondState.highlightedNumber != firstState.highlightedNumber) { "하이라이트된 숫자가 변경되어야 함" }
    }

    @Test
    fun testHighlightIncludesUserInputNumbers() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        val state = viewModel.state.value

        // 빈 셀에 숫자 입력
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

        // 빈 셀 선택 후 숫자 입력
        val cellTag = "cell_${emptyRow}_${emptyCol}_editable"
        composeTestRule.onNodeWithTag(cellTag).performClick()
        composeTestRule.waitForIdle()

        // 숫자 5 입력
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        composeTestRule.waitForIdle()

        // 입력한 셀 다시 클릭
        composeTestRule.onNodeWithTag(cellTag).performClick()
        composeTestRule.waitForIdle()

        // 입력한 숫자가 하이라이트되었는지 확인
        val finalState = viewModel.state.value
        assert(finalState.highlightedNumber == 5) { "입력한 숫자가 하이라이트되어야 함" }
        assert(finalState.highlightedCells.contains(Pair(emptyRow, emptyCol))) {
            "입력한 셀이 하이라이트되어야 함"
        }
    }

    @Test
    fun testHighlightImmediatelyOnNumberInput() {
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

        // 숫자 7 입력 후 즉시 하이라이트 확인
        composeTestRule.onNodeWithTag("number_btn_7").performClick()
        composeTestRule.waitForIdle()

        // 숫자 입력 후 즉시 하이라이트가 적용되었는지 확인
        val afterInputState = viewModel.state.value
        assert(afterInputState.board[emptyRow][emptyCol] == 7) { "숫자가 입력되어야 함" }
        assert(afterInputState.highlightedNumber == 7) { "입력한 숫자가 즉시 하이라이트되어야 함" }
        assert(afterInputState.highlightedCells.contains(Pair(emptyRow, emptyCol))) {
            "입력한 셀이 즉시 하이라이트되어야 함"
        }

        // 보드에서 숫자 7을 가진 모든 셀이 하이라이트되었는지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                if (afterInputState.board[row][col] == 7) {
                    assert(afterInputState.highlightedCells.contains(Pair(row, col))) {
                        "숫자 7을 가진 모든 셀이 하이라이트되어야 함: ($row, $col)"
                    }
                }
            }
        }
    }

    @Test
    fun testHighlightClearsOnNumberRemoval() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        val state = viewModel.state.value

        // 빈 셀에 숫자 입력
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

        // 빈 셀 선택 후 숫자 입력
        val cellTag = "cell_${emptyRow}_${emptyCol}_editable"
        composeTestRule.onNodeWithTag(cellTag).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        composeTestRule.waitForIdle()

        val afterInputState = viewModel.state.value
        assert(afterInputState.highlightedNumber == 3) { "입력 후 하이라이트되어야 함" }

        // 숫자 제거 (지우기 버튼 클릭)
        composeTestRule.onNodeWithTag("action_btn_지우기").performClick()
        composeTestRule.waitForIdle()

        // 제거 후 하이라이트가 꺼졌는지 확인
        val afterRemovalState = viewModel.state.value
        assert(afterRemovalState.board[emptyRow][emptyCol] == 0) { "숫자가 제거되어야 함" }
        assert(afterRemovalState.highlightedNumber == 0) { "숫자 제거 후 하이라이트가 꺼져야 함" }
        assert(afterRemovalState.highlightedCells.isEmpty()) { "숫자 제거 후 하이라이트된 셀이 없어야 함" }
    }
} 