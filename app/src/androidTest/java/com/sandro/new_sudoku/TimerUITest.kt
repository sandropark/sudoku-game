package com.sandro.new_sudoku

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sandro.new_sudoku.base.BaseUITest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerUITest : BaseUITest() {

    private lateinit var viewModel: SudokuViewModel

    @Test
    fun testTimerDisplaysCorrectly() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 초기 타이머 표시 확인 (00:00)
        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()
    }

    @Test
    fun testTimerFormatsCorrectly() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 타이머 시간을 설정하고 표시 확인
        viewModel.updateTimerForTest(65) // 1분 5초
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("01:05").assertIsDisplayed()
    }

    @Test
    fun testTimerUpdatesWhenTimeChanges() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 다양한 시간으로 업데이트하고 표시 확인
        val testTimes = listOf(
            0 to "00:00",
            30 to "00:30",
            90 to "01:30",
            3600 to "60:00"
        )

        testTimes.forEach { (seconds, expectedDisplay) ->
            viewModel.updateTimerForTest(seconds)
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(expectedDisplay).assertIsDisplayed()
        }
    }

    @Test
    fun testNewGameStartsTimer() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 새 게임 시작 후 타이머 수동 시작
        viewModel.newGame()
        viewModel.startTimer()
        composeTestRule.waitForIdle()

        // 타이머가 시작되었는지 확인 (상태를 통해)
        val isTimerRunning = viewModel.state.value.isTimerRunning
        assert(isTimerRunning) { "새 게임 시작 시 타이머가 실행되어야 함" }

        // 타이머 표시 확인
        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()
    }

    @Test
    fun testGameCompletionStopsTimer() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 게임 시작 후 타이머 수동 시작
        viewModel.newGame()
        viewModel.startTimer()
        assert(viewModel.state.value.isTimerRunning) { "게임 시작 시 타이머 실행" }

        // 게임 완료
        viewModel.solveGame()
        composeTestRule.waitForIdle()

        // 타이머가 정지되었는지 확인
        val isTimerRunning = viewModel.state.value.isTimerRunning
        assert(!isTimerRunning) { "게임 완료 시 타이머가 정지되어야 함" }
    }

    @Test
    fun testTimerWithMistakeCount() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 타이머와 실수 카운트 동시 표시 확인
        viewModel.updateTimerForTest(120) // 2분
        composeTestRule.waitForIdle()

        // StatusBar에 타이머와 실수 카운트가 모두 표시되는지 확인
        composeTestRule.onNodeWithText("02:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("실수: 0").assertIsDisplayed()
    }
} 