package com.sandro.new_sudoku

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameCompleteUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: SudokuViewModel

    @Test
    fun testGameCompleteDialogDisplay() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 게임 완료 (해답 보기)
        viewModel.solveGame()
        composeTestRule.waitForIdle()

        // 게임 완료 다이얼로그가 표시되는지 확인
        composeTestRule.onNodeWithTag("game_complete_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("🎉 축하합니다!").assertIsDisplayed()
        composeTestRule.onNodeWithText("퍼즐을 완료했습니다!").assertIsDisplayed()
    }

    // @Test
    fun testGameCompleteDialogStatistics() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 타이머 시간 설정
        viewModel.updateTimerForTest(125) // 2분 5초

        // 게임 완료
        viewModel.solveGame()
        composeTestRule.waitForIdle()

        // 다이얼로그가 표시되는지 확인
        composeTestRule.onNodeWithTag("game_complete_dialog").assertIsDisplayed()

        // 통계 정보가 올바르게 표시되는지 확인
        composeTestRule.onNodeWithText("소요 시간:").assertIsDisplayed()
        // 다이얼로그 내에서만 02:05 텍스트를 찾기 위해 더 구체적으로 검색
        composeTestRule.onNode(
            hasTestTag("game_complete_dialog").and(hasAnyDescendant(hasText("02:05")))
        ).assertExists()
        composeTestRule.onNodeWithText("실수 횟수:").assertIsDisplayed()
        composeTestRule.onNodeWithText("힌트 사용:").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 회").assertIsDisplayed()
    }

    @Test
    fun testGameCompleteDialogRetryButton() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 게임 완료
        viewModel.solveGame()
        composeTestRule.waitForIdle()

        // 게임 완료 다이얼로그 확인
        composeTestRule.onNodeWithTag("game_complete_dialog").assertIsDisplayed()

        // 다시하기 버튼 클릭
        composeTestRule.onNodeWithTag("game_complete_retry_btn").performClick()
        composeTestRule.waitForIdle()

        // 다이얼로그가 닫히고 게임이 재시작되었는지 확인
        composeTestRule.onNodeWithTag("game_complete_dialog").assertDoesNotExist()

        val newState = viewModel.state.value
        assert(!newState.isGameComplete) { "게임이 재시작되어야 함" }
        assert(!newState.showGameCompleteDialog) { "완료 다이얼로그가 닫혀야 함" }
        assert(newState.elapsedTimeSeconds == 0) { "타이머가 초기화되어야 함" }
        assert(newState.isTimerRunning) { "다시하기 시 타이머가 자동으로 시작되어야 함" }
    }

    @Test
    fun testGameCompleteDialogMainMenuButton() {
        var navigationTriggered = false

        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(
                viewModel = viewModel,
                onBackToMain = { navigationTriggered = true }
            )
        }

        // 게임 완료
        viewModel.solveGame()
        composeTestRule.waitForIdle()

        // 게임 완료 다이얼로그 확인
        composeTestRule.onNodeWithTag("game_complete_dialog").assertIsDisplayed()

        // 메인 메뉴 버튼 클릭
        composeTestRule.onNodeWithTag("game_complete_main_menu_btn").performClick()
        composeTestRule.waitForIdle()

        // 다이얼로그가 닫히고 네비게이션 상태가 변경되었는지 확인
        composeTestRule.onNodeWithTag("game_complete_dialog").assertDoesNotExist()

        val finalState = viewModel.state.value
        assert(!finalState.showGameCompleteDialog) { "완료 다이얼로그가 닫혀야 함" }
        assert(finalState.shouldNavigateToMain) { "메인 네비게이션 플래그가 설정되어야 함" }
    }


    @Test
    fun testRealPuzzleCompletionShowsDialog() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 실제 퍼즐 완성 시뮬레이션 (해답 보기로 대체)
        viewModel.solveGame()
        composeTestRule.waitForIdle()

        // 완료 다이얼로그가 자동으로 표시되는지 확인
        composeTestRule.onNodeWithTag("game_complete_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("🎉 축하합니다!").assertIsDisplayed()
    }

    // 헬퍼 메서드들
    private fun makeDistinctMistakes(count: Int) {
        val state = viewModel.state.value
        var mistakesMade = 0
        val usedCells = mutableSetOf<Pair<Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= count) break
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
                    }
                }
            }
            if (mistakesMade >= count) break
        }
    }
} 