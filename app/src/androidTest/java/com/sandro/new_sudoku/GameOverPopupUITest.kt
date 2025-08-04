package com.sandro.new_sudoku

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameOverPopupUITest {
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
    fun testGameOverDialogDisplaysCorrectly() {
        // 여러 빈 셀에서 3번의 서로 다른 실수 만들기
        var mistakesMade = 0
        val usedCells = mutableSetOf<Pair<Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= 3) break
                if (!viewModel.isInitialCell(row, col) &&
                    viewModel.state.value.board[row][col] == 0 &&
                    !usedCells.contains(Pair(row, col))
                ) {

                    val cellTag = "cell_${row}_${col}_editable"

                    // 셀 선택
                    composeTestRule.onNodeWithTag(cellTag).performClick()
                    composeTestRule.waitForIdle()

                    // 해당 위치에 잘못된 값 찾기 (스도쿠 규칙 위반)
                    val wrongValue = findInvalidValueForCell(row, col, viewModel.state.value.board)

                    if (wrongValue != null) {
                        // 잘못된 값 입력
                        composeTestRule.onNodeWithTag("number_btn_${wrongValue}").performClick()
                        composeTestRule.waitForIdle()
                        mistakesMade++
                        usedCells.add(Pair(row, col))
                    }
                }
            }
            if (mistakesMade >= 3) break
        }

        if (mistakesMade >= 3) {
            // 팝업이 표시되는지 확인
            composeTestRule.onNodeWithTag("game_over_dialog").assertIsDisplayed()
            composeTestRule.onNodeWithText("실수 3번!").assertIsDisplayed()
            composeTestRule.onNodeWithText("계속하기").assertIsDisplayed()
            composeTestRule.onNodeWithText("새 게임").assertIsDisplayed()
        }
    }

    @Test
    fun testGameOverDialogNotDisplayedInitially() {
        // 초기 상태에서는 팝업이 표시되지 않아야 함
        composeTestRule.onNodeWithTag("game_over_dialog").assertDoesNotExist()
    }

    @Test
    fun testMistakeCountIncreases() {
        // 초기 실수 카운트 확인
        composeTestRule.onNodeWithText("실수: 0").assertIsDisplayed()

        // 실수 하나 만들기
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

            // 셀 선택
            composeTestRule.onNodeWithTag(cellTag).performClick()

            // 잘못된 값 찾기
            val wrongValue = findInvalidValueForCell(emptyRow, emptyCol, state.board)

            if (wrongValue != null) {
                // 잘못된 값 입력
                composeTestRule.onNodeWithTag("number_btn_${wrongValue}").performClick()
                composeTestRule.waitForIdle()

                // 실수 카운트가 증가했는지 확인
                composeTestRule.onNodeWithText("실수: 1").assertIsDisplayed()
            }
        }
    }

    /**
     * 특정 셀에 대해 스도쿠 규칙을 위반하는 값을 찾는다
     */
    private fun findInvalidValueForCell(row: Int, col: Int, board: Array<IntArray>): Int? {
        for (value in 1..9) {
            // 행에서 충돌하는 값 찾기
            for (c in 0..8) {
                if (c != col && board[row][c] == value) {
                    return value
                }
            }

            // 열에서 충돌하는 값 찾기
            for (r in 0..8) {
                if (r != row && board[r][col] == value) {
                    return value
                }
            }

            // 3x3 박스에서 충돌하는 값 찾기
            val boxRow = (row / 3) * 3
            val boxCol = (col / 3) * 3
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != row || c != col) && board[r][c] == value) {
                        return value
                    }
                }
            }
        }
        return null
    }
} 