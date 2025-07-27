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
class TimerGameOverUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: SudokuViewModel

    @Test
    fun testTimerStopsOnGameOver() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 게임 화면 로드 후 타이머가 시작되는지 확인
        composeTestRule.waitForIdle()
        assert(viewModel.state.value.isTimerRunning) { "타이머가 시작되어야 함" }

        // 3번의 실수를 UI를 통해 만들기
        createThreeMistakesViaUI()

        // 게임 오버 팝업이 표시되는지 확인
        composeTestRule.onNodeWithTag("game_over_dialog").assertIsDisplayed()

        // 타이머가 정지되었는지 확인
        assert(!viewModel.state.value.isTimerRunning) { "게임 오버 시 타이머가 정지되어야 함" }
    }

    @Test
    fun testTimerRestartsOnContinue() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 게임 화면 로드 후 타이머 시작 확인
        composeTestRule.waitForIdle()
        assert(viewModel.state.value.isTimerRunning) { "타이머가 시작되어야 함" }

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakesViaUI()

        // 게임 오버 팝업 확인
        composeTestRule.onNodeWithTag("game_over_dialog").assertIsDisplayed()
        assert(!viewModel.state.value.isTimerRunning) { "게임 오버 시 타이머 정지" }

        // 계속하기 버튼 클릭
        composeTestRule.onNodeWithTag("game_over_continue_btn").performClick()
        composeTestRule.waitForIdle()

        // 팝업이 닫히고 타이머가 재시작되었는지 확인
        composeTestRule.onNodeWithTag("game_over_dialog").assertDoesNotExist()
        assert(viewModel.state.value.isTimerRunning) { "계속하기 후 타이머가 재시작되어야 함" }
    }

    @Test
    fun testTimerDisplayUpdatesDuringGame() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 초기 타이머 표시 확인
        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()

        // 타이머 시간을 수동으로 설정
        viewModel.updateTimerForTest(125) // 2분 5초
        composeTestRule.waitForIdle()

        // 업데이트된 시간이 표시되는지 확인
        composeTestRule.onNodeWithText("02:05").assertIsDisplayed()
    }

    @Test
    fun testTimerStaysStoppedOnNewGameOption() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakesViaUI()

        // 게임 오버 팝업 확인
        composeTestRule.onNodeWithTag("game_over_dialog").assertIsDisplayed()
        assert(!viewModel.state.value.isTimerRunning) { "게임 오버 시 타이머 정지" }

        // 새 게임 버튼 클릭
        composeTestRule.onNodeWithTag("game_over_new_game_btn").performClick()
        composeTestRule.waitForIdle()

        // 재시작 옵션 팝업이 표시되고 타이머는 여전히 정지 상태인지 확인
        composeTestRule.onNodeWithTag("restart_options_dialog").assertIsDisplayed()
        assert(!viewModel.state.value.isTimerRunning) { "새 게임 옵션에서도 타이머는 정지 상태여야 함" }
    }

    // 헬퍼 메서드들
    private fun createThreeMistakesViaUI() {
        val state = viewModel.state.value
        var mistakesMade = 0
        val usedCells = mutableSetOf<Pair<Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= 3) break
                if (!viewModel.isInitialCell(row, col) &&
                    state.board[row][col] == 0 &&
                    !usedCells.contains(Pair(row, col))
                ) {

                    val cellTag = "cell_${row}_${col}_editable"
                    composeTestRule.onNodeWithTag(cellTag).performClick()
                    composeTestRule.waitForIdle()

                    val wrongValue = (1..9).firstOrNull { value ->
                        state.board[row].contains(value)
                    }
                    if (wrongValue != null) {
                        composeTestRule.onNodeWithTag("number_btn_${wrongValue}").performClick()
                        composeTestRule.waitForIdle()
                        usedCells.add(Pair(row, col))
                        mistakesMade++

                        // 3번째 실수 후 팝업이 나타나는지 확인
                        if (mistakesMade >= 3) {
                            if (viewModel.state.value.showGameOverDialog) {
                                break
                            }
                        }
                    }
                }
            }
            if (mistakesMade >= 3) break
        }
    }
} 