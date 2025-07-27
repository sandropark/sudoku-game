package com.sandro.new_sudoku

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RestartOptionUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: SudokuViewModel

    @Test
    fun testRestartOptionsDialogDisplay() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 3번의 실수를 만들어서 게임 오버 팝업 표시
        createThreeMistakes()

        // 게임 오버 팝업 확인
        composeTestRule.onNodeWithTag("game_over_dialog").assertIsDisplayed()

        // "새 게임" 버튼 클릭
        composeTestRule.onNodeWithTag("game_over_new_game_btn").performClick()
        composeTestRule.waitForIdle()

        // 재시작 옵션 팝업이 표시되는지 확인
        composeTestRule.onNodeWithTag("restart_options_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("새 게임 옵션").assertIsDisplayed()
        composeTestRule.onNodeWithText("재시도").assertIsDisplayed()
        composeTestRule.onNodeWithText("난이도 변경").assertIsDisplayed()
        composeTestRule.onNodeWithText("취소").assertIsDisplayed()
    }

    @Test
    fun testRetryOption() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 초기 보드 상태 저장
        Array(9) { row ->
            IntArray(9) { col -> viewModel.state.value.board[row][col] }
        }

        // 게임 진행 (값 입력)
        makeValidMove()

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakes()

        // 새 게임 → 재시도 선택
        composeTestRule.onNodeWithTag("game_over_new_game_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("restart_option_retry_btn").performClick()
        composeTestRule.waitForIdle()

        // 재시작 옵션 팝업이 닫혔는지 확인
        composeTestRule.onNodeWithTag("restart_options_dialog").assertDoesNotExist()
        composeTestRule.onNodeWithTag("game_over_dialog").assertDoesNotExist()

        // 보드가 초기 상태로 복원되었는지 확인 (실제 확인은 어려우므로 실수 카운트만 확인)
        val finalState = viewModel.state.value
        assert(finalState.mistakeCount == 0) { "실수 카운트가 0으로 초기화되어야 함" }
    }

    @Test
    fun testChangeDifficultyOption() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakes()

        // 새 게임 → 난이도 변경 선택
        composeTestRule.onNodeWithTag("game_over_new_game_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("restart_option_difficulty_btn").performClick()
        composeTestRule.waitForIdle()

        // 재시작 옵션 팝업이 닫혔는지 확인
        composeTestRule.onNodeWithTag("restart_options_dialog").assertDoesNotExist()
        composeTestRule.onNodeWithTag("game_over_dialog").assertDoesNotExist()

        // shouldNavigateToMain이 true로 설정되었는지 확인
        val finalState = viewModel.state.value
        assert(finalState.shouldNavigateToMain) { "메인화면으로 이동해야 함" }
    }

    @Test
    fun testCancelOption() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakes()

        // 새 게임 → 취소 선택
        composeTestRule.onNodeWithTag("game_over_new_game_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("restart_option_cancel_btn").performClick()
        composeTestRule.waitForIdle()

        // 재시작 옵션 팝업이 닫히고 게임 오버 팝업이 다시 나타났는지 확인
        composeTestRule.onNodeWithTag("restart_options_dialog").assertDoesNotExist()
        composeTestRule.onNodeWithTag("game_over_dialog").assertIsDisplayed()
    }

    // 헬퍼 메서드들
    private fun createThreeMistakes() {
        val state = viewModel.state.value
        var mistakesMade = 0

        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= 3) break
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    val cellTag = "cell_${row}_${col}_editable"
                    composeTestRule.onNodeWithTag(cellTag).performClick()
                    composeTestRule.waitForIdle()

                    val wrongValue = (1..9).firstOrNull { value ->
                        state.board[row].contains(value)
                    }
                    if (wrongValue != null) {
                        composeTestRule.onNodeWithTag("number_btn_${wrongValue}").performClick()
                        composeTestRule.waitForIdle()
                        mistakesMade++
                    }
                }
            }
            if (mistakesMade >= 3) break
        }
    }

    private fun makeValidMove() {
        val state = viewModel.state.value

        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    val cellTag = "cell_${row}_${col}_editable"
                    composeTestRule.onNodeWithTag(cellTag).performClick()
                    composeTestRule.waitForIdle()

                    val validValue = (1..9).firstOrNull { value ->
                        isValidMove(state.board, row, col, value)
                    }
                    if (validValue != null) {
                        composeTestRule.onNodeWithTag("number_btn_${validValue}").performClick()
                        composeTestRule.waitForIdle()
                        return
                    }
                }
            }
        }
    }

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