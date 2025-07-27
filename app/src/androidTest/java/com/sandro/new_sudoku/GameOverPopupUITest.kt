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
        // 실제 실수를 3번 만들어서 팝업 표시
        // 빈 셀 찾아서 같은 값 3번 입력
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
            // 빈 셀은 초기 셀이 아니므로 _editable 접미사가 붙음
            val cellTag = "cell_${emptyRow}_${emptyCol}_editable"

            // 셀 선택
            composeTestRule.onNodeWithTag(cellTag).performClick()

            // 같은 행에 있는 숫자 찾기
            val existingValue = (1..9).firstOrNull { value ->
                state.board[emptyRow].contains(value)
            }

            if (existingValue != null) {
                // 3번 잘못된 값 입력
                repeat(3) {
                    composeTestRule.onNodeWithTag("number_btn_${existingValue}").performClick()
                    composeTestRule.waitForIdle()
                }

                // 팝업이 표시되는지 확인
                composeTestRule.onNodeWithTag("game_over_dialog").assertIsDisplayed()
                composeTestRule.onNodeWithText("실수 3번!").assertIsDisplayed()
                composeTestRule.onNodeWithText("계속하기").assertIsDisplayed()
                composeTestRule.onNodeWithText("새 게임").assertIsDisplayed()
            }
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
            // 빈 셀은 초기 셀이 아니므로 _editable 접미사가 붙음
            val cellTag = "cell_${emptyRow}_${emptyCol}_editable"

            // 셀 선택
            composeTestRule.onNodeWithTag(cellTag).performClick()

            // 같은 행에 있는 숫자 찾기
            val existingValue = (1..9).firstOrNull { value ->
                state.board[emptyRow].contains(value)
            }

            if (existingValue != null) {
                // 잘못된 값 입력
                composeTestRule.onNodeWithTag("number_btn_${existingValue}").performClick()
                composeTestRule.waitForIdle()

                // 실수 카운트가 증가했는지 확인
                composeTestRule.onNodeWithText("실수: 1").assertIsDisplayed()
            }
        }
    }
} 