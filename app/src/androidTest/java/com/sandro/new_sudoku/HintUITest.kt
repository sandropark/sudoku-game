package com.sandro.new_sudoku

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 힌트 기능 UI 테스트
 */
@RunWith(AndroidJUnit4::class)
class HintUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun 힌트_버튼이_표시되는지_테스트() {
        // Given: SudokuScreen 표시
        composeTestRule.setContent {
            val viewModel = SudokuViewModel()
            viewModel.isTestMode = true
            SudokuScreen(viewModel = viewModel)
        }

        // Then: 힌트 버튼이 표시됨
        composeTestRule
            .onNodeWithTag("action_btn_힌트")
            .assertIsDisplayed()
    }

    @Test
    fun 셀_선택_후_힌트_버튼_클릭_시_값이_입력되는지_테스트() {
        // Given: SudokuScreen 표시
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        composeTestRule.setContent {
            SudokuScreen(viewModel = viewModel)
        }

        // 빈 셀을 찾아서 클릭
        var foundEmptyCell = false
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (viewModel.state.value.board[row][col] == 0) {
                    // When: 빈 셀 선택 후 힌트 버튼 클릭
                    composeTestRule
                        .onNodeWithTag("cell_${row}_${col}_editable")
                        .performClick()

                    composeTestRule
                        .onNodeWithTag("action_btn_힌트")
                        .performClick()

                    // Then: 해당 셀에 값이 입력됨
                    val updatedValue = viewModel.state.value.board[row][col]
                    assert(updatedValue != 0) { "힌트 후 셀에 값이 입력되어야 함" }
                    assert(updatedValue in 1..9) { "입력된 값은 1-9 사이여야 함: $updatedValue" }

                    foundEmptyCell = true
                    break
                }
            }
            if (foundEmptyCell) break
        }

        assert(foundEmptyCell) { "테스트를 위한 빈 셀이 있어야 함" }
    }

    @Test
    fun 셀이_선택되지_않은_상태에서_힌트_버튼_클릭_시_아무_일이_일어나지_않는지_테스트() {
        // Given: SudokuScreen 표시 (셀 선택하지 않음)
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        composeTestRule.setContent {
            SudokuScreen(viewModel = viewModel)
        }

        // 초기 보드 상태 저장
        val initialBoard = viewModel.state.value.board.map { it.copyOf() }.toTypedArray()

        // When: 힌트 버튼 클릭 (셀 선택 없이)
        composeTestRule
            .onNodeWithTag("action_btn_힌트")
            .performClick()

        // Then: 보드 상태가 변경되지 않음
        val finalBoard = viewModel.state.value.board
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                assert(initialBoard[row][col] == finalBoard[row][col]) {
                    "셀이 선택되지 않은 상태에서는 보드가 변경되지 않아야 함 (${row}, ${col})"
                }
            }
        }
    }

    @Test
    fun 힌트_사용_후_실행취소_버튼이_작동하는지_테스트() {
        // Given: SudokuScreen 표시
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        composeTestRule.setContent {
            SudokuScreen(viewModel = viewModel)
        }

        // 빈 셀을 찾아서 클릭
        var foundEmptyCell = false
        var testRow = -1
        var testCol = -1

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (viewModel.state.value.board[row][col] == 0) {
                    testRow = row
                    testCol = col
                    foundEmptyCell = true
                    break
                }
            }
            if (foundEmptyCell) break
        }

        assert(foundEmptyCell) { "테스트를 위한 빈 셀이 있어야 함" }

        val originalValue = viewModel.state.value.board[testRow][testCol]

        // When: 셀 선택 후 힌트 사용, 그 다음 실행취소
        composeTestRule
            .onNodeWithTag("cell_${testRow}_${testCol}_editable")
            .performClick()

        composeTestRule
            .onNodeWithTag("action_btn_힌트")
            .performClick()

        // 힌트가 적용되었는지 확인
        val hintValue = viewModel.state.value.board[testRow][testCol]
        assert(hintValue != 0) { "힌트 적용 후 값이 입력되어야 함" }

        composeTestRule
            .onNodeWithTag("action_btn_실행취소")
            .performClick()

        // Then: 원래 값으로 복원됨
        val restoredValue = viewModel.state.value.board[testRow][testCol]
        assert(restoredValue == originalValue) {
            "실행취소 후 원래 값으로 복원되어야 함: original=$originalValue, restored=$restoredValue"
        }
    }

    @Test
    fun 여러_셀에_연속으로_힌트를_사용할_수_있는지_테스트() {
        // Given: SudokuScreen 표시
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        composeTestRule.setContent {
            SudokuScreen(viewModel = viewModel)
        }

        // 빈 셀 2개를 찾기
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (viewModel.state.value.board[row][col] == 0) {
                    emptyCells.add(Pair(row, col))
                    if (emptyCells.size >= 2) break
                }
            }
            if (emptyCells.size >= 2) break
        }

        assert(emptyCells.size >= 2) { "테스트를 위한 빈 셀이 2개 이상 있어야 함" }

        // When: 첫 번째 셀에 힌트 사용
        val (row1, col1) = emptyCells[0]
        composeTestRule
            .onNodeWithTag("cell_${row1}_${col1}_editable")
            .performClick()

        composeTestRule
            .onNodeWithTag("action_btn_힌트")
            .performClick()

        val firstHintValue = viewModel.state.value.board[row1][col1]

        // When: 두 번째 셀에 힌트 사용
        val (row2, col2) = emptyCells[1]
        composeTestRule
            .onNodeWithTag("cell_${row2}_${col2}_editable")
            .performClick()

        composeTestRule
            .onNodeWithTag("action_btn_힌트")
            .performClick()

        val secondHintValue = viewModel.state.value.board[row2][col2]

        // Then: 두 셀 모두에 값이 입력됨
        assert(firstHintValue != 0) { "첫 번째 힌트 값이 입력되어야 함" }
        assert(secondHintValue != 0) { "두 번째 힌트 값이 입력되어야 함" }
        assert(firstHintValue in 1..9) { "첫 번째 힌트 값은 1-9 사이여야 함" }
        assert(secondHintValue in 1..9) { "두 번째 힌트 값은 1-9 사이여야 함" }
    }
}