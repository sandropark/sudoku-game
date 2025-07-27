package com.sandro.new_sudoku

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NumberToggleUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: SudokuViewModel

    @Before
    fun setUp() {
        viewModel = SudokuViewModel()
        composeTestRule.setContent {
            SudokuScreen(viewModel = viewModel)
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testNumberToggleFunctionality() {
        // 편집 가능한 빈 셀 찾기
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

        if (emptyRow != -1) {
            val cellTag = "cell_${emptyRow}_${emptyCol}_editable"

            // 1. 셀 선택
            composeTestRule.onNodeWithTag(cellTag).performClick()
            composeTestRule.waitForIdle()

            // 2. 숫자 5 입력
            composeTestRule.onNodeWithTag("number_btn_5").performClick()
            composeTestRule.waitForIdle()

            // 3. 같은 숫자 5 다시 클릭 (토글 - 지워짐)
            composeTestRule.onNodeWithTag("number_btn_5").performClick()
            composeTestRule.waitForIdle()

            // 4. 결과 확인: 셀이 비어있어야 함
            // UI에서 빈 셀은 텍스트가 표시되지 않으므로 직접 상태 확인
            val finalState = viewModel.state.value
            assert(finalState.board[emptyRow][emptyCol] == 0) {
                "셀이 비어있어야 함 (토글 기능으로 지워졌어야 함)"
            }
        }
    }

    @Test
    fun testNumberReplacementFunctionality() {
        // 편집 가능한 빈 셀 찾기
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

        if (emptyRow != -1) {
            val cellTag = "cell_${emptyRow}_${emptyCol}_editable"

            // 1. 셀 선택
            composeTestRule.onNodeWithTag(cellTag).performClick()
            composeTestRule.waitForIdle()

            // 2. 숫자 3 입력
            composeTestRule.onNodeWithTag("number_btn_3").performClick()
            composeTestRule.waitForIdle()

            // 3. 다른 숫자 7 입력 (교체)
            composeTestRule.onNodeWithTag("number_btn_7").performClick()
            composeTestRule.waitForIdle()

            // 4. 결과 확인: 셀에 7이 있어야 함
            val finalState = viewModel.state.value
            assert(finalState.board[emptyRow][emptyCol] == 7) {
                "셀에 7이 있어야 함 (3에서 7로 교체되었어야 함)"
            }
        }
    }

    @Test
    fun testInitialCellsNotToggleable() {
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

        if (initialRow != -1) {
            val cellTag = "cell_${initialRow}_${initialCol}"

            // 1. 초기 셀 선택 시도
            composeTestRule.onNodeWithTag(cellTag).performClick()
            composeTestRule.waitForIdle()

            // 2. 같은 숫자 클릭 시도
            composeTestRule.onNodeWithTag("number_btn_${initialValue}").performClick()
            composeTestRule.waitForIdle()

            // 3. 결과 확인: 초기 셀 값이 변경되지 않아야 함
            val finalState = viewModel.state.value
            assert(finalState.board[initialRow][initialCol] == initialValue) {
                "초기 셀의 값은 변경되지 않아야 함"
            }
        }
    }

    @Test
    fun testZeroInputClearsCell() {
        // 편집 가능한 빈 셀 찾기
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

        if (emptyRow != -1) {
            val cellTag = "cell_${emptyRow}_${emptyCol}_editable"

            // 1. 셀 선택
            composeTestRule.onNodeWithTag(cellTag).performClick()
            composeTestRule.waitForIdle()

            // 2. 숫자 8 입력
            composeTestRule.onNodeWithTag("number_btn_8").performClick()
            composeTestRule.waitForIdle()

            // 3. 지우기 버튼 클릭 (0 입력과 동일한 효과)
            composeTestRule.onNodeWithTag("action_btn_지우기").performClick()
            composeTestRule.waitForIdle()

            // 4. 결과 확인: 셀이 비어있어야 함
            val finalState = viewModel.state.value
            assert(finalState.board[emptyRow][emptyCol] == 0) {
                "지우기 버튼으로 셀이 비워져야 함"
            }
        }
    }
} 