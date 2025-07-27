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
import com.sandro.new_sudoku.BuildConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestSolveButtonUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: SudokuViewModel

    @Test
    fun testSolveButtonIsDisplayedInDebugBuild() {
        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 디버그 빌드에서만 정답 입력 버튼이 표시되는지 확인
        if (BuildConfig.DEBUG) {
            composeTestRule.onNodeWithTag("action_btn_정답입력").assertIsDisplayed()
            composeTestRule.onNodeWithText("🎯정답").assertIsDisplayed()
        } else {
            // 릴리즈 빌드에서는 버튼이 없어야 함
            composeTestRule.onNodeWithTag("action_btn_정답입력").assertDoesNotExist()
        }
    }

    @Test
    fun testSolveButtonClickCompletesGame() {
        // 디버그 빌드에서만 테스트 실행
        if (!BuildConfig.DEBUG) return

        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 초기 상태 확인
        val initialState = viewModel.state.value
        assert(!initialState.isGameComplete) { "초기에는 게임이 완료되지 않아야 함" }
        assert(!initialState.showGameCompleteDialog) { "초기에는 완료 다이얼로그가 표시되지 않아야 함" }

        // 정답 입력 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_정답입력").performClick()
        composeTestRule.waitForIdle()

        // 게임이 완료되었는지 확인
        val finalState = viewModel.state.value
        assert(finalState.isGameComplete) { "정답 입력 후 게임이 완료되어야 함" }
        assert(finalState.showGameCompleteDialog) { "정답 입력 후 완료 다이얼로그가 표시되어야 함" }

        // 완료 다이얼로그가 표시되는지 UI에서 확인
        composeTestRule.onNodeWithTag("game_complete_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("🎉 축하합니다!").assertIsDisplayed()
    }

    @Test
    fun testSolveButtonStopsTimer() {
        // 디버그 빌드에서만 테스트 실행
        if (!BuildConfig.DEBUG) return

        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 타이머가 실행 중인지 확인 (자동 시작됨)
        composeTestRule.waitForIdle()
        assert(viewModel.state.value.isTimerRunning) { "타이머가 실행 중이어야 함" }

        // 정답 입력 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_정답입력").performClick()
        composeTestRule.waitForIdle()

        // 타이머가 정지되었는지 확인
        assert(!viewModel.state.value.isTimerRunning) { "정답 입력 후 타이머가 정지되어야 함" }
    }

    @Test
    fun testSolveButtonDoesNotAffectMistakeCount() {
        // 디버그 빌드에서만 테스트 실행
        if (!BuildConfig.DEBUG) return

        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 실수 몇 번 만들기
        makeDistinctMistakes(2)
        val mistakeCountBeforeSolve = viewModel.state.value.mistakeCount

        // 정답 입력 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_정답입력").performClick()
        composeTestRule.waitForIdle()

        // 실수 카운트가 변경되지 않았는지 확인
        val finalState = viewModel.state.value
        assert(finalState.mistakeCount == mistakeCountBeforeSolve) {
            "정답 입력 시 실수 카운트는 변경되지 않아야 함: 예상=${mistakeCountBeforeSolve}, 실제=${finalState.mistakeCount}"
        }
    }

    @Test
    fun testSolveButtonWorksOnlyDuringGame() {
        // 디버그 빌드에서만 테스트 실행
        if (!BuildConfig.DEBUG) return

        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 첫 번째 정답 입력 (게임 완료)
        composeTestRule.onNodeWithTag("action_btn_정답입력").performClick()
        composeTestRule.waitForIdle()

        // 게임이 완료되었는지 확인
        assert(viewModel.state.value.isGameComplete) { "게임이 완료되어야 함" }

        // 완료 다이얼로그 닫기
        composeTestRule.onNodeWithTag("game_complete_close_btn").performClick()
        composeTestRule.waitForIdle()

        // 두 번째 정답 입력 시도 (이미 완료된 게임)
        val stateBeforeSecondClick = viewModel.state.value
        composeTestRule.onNodeWithTag("action_btn_정답입력").performClick()
        composeTestRule.waitForIdle()

        // 상태가 변경되지 않았는지 확인
        val stateAfterSecondClick = viewModel.state.value
        assert(stateBeforeSecondClick.isGameComplete == stateAfterSecondClick.isGameComplete) {
            "완료된 게임에서는 상태가 변경되지 않아야 함"
        }
    }

    @Test
    fun testSolveButtonWithStatistics() {
        // 디버그 빌드에서만 테스트 실행
        if (!BuildConfig.DEBUG) return

        composeTestRule.setContent {
            viewModel = SudokuViewModel()
            SudokuScreen(viewModel = viewModel, onBackToMain = {})
        }

        // 타이머 시간 설정
        viewModel.updateTimerForTest(90) // 1분 30초

        // 실수 1번 만들기
        makeDistinctMistakes(1)

        // 정답 입력 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_정답입력").performClick()
        composeTestRule.waitForIdle()

        // 완료 다이얼로그가 표시되는지 확인
        composeTestRule.onNodeWithTag("game_complete_dialog").assertIsDisplayed()

        // 통계 정보가 올바르게 표시되는지 확인
        composeTestRule.onNodeWithText("소요 시간:").assertIsDisplayed()
        composeTestRule.onNodeWithText("실수 횟수:").assertIsDisplayed()
        // 다이얼로그 내에서 시간과 실수 정보 확인
        composeTestRule.onNode(
            hasTestTag("game_complete_dialog").and(hasAnyDescendant(hasText("01:30")))
        ).assertExists()
        composeTestRule.onNode(
            hasTestTag("game_complete_dialog").and(hasAnyDescendant(hasText("1 회")))
        ).assertExists()
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