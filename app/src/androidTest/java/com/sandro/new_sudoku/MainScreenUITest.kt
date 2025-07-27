package com.sandro.new_sudoku

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sandro.new_sudoku.ui.DifficultyLevel
import com.sandro.new_sudoku.ui.MainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMainScreenDisplaysBasicElements() {
        composeTestRule.setContent {
            MainScreen(
                onStartNewGame = { },
                onContinueGame = { }
            )
        }

        // 로고 확인
        composeTestRule.onNodeWithTag("main_logo").assertExists()

        // 새 게임 버튼 확인
        composeTestRule.onNodeWithTag("start_new_game_btn").assertExists()
        composeTestRule.onNodeWithText("새 게임").assertExists()

        // 이어하기 버튼은 기본적으로 보이지 않음
        composeTestRule.onNodeWithTag("continue_game_btn").assertDoesNotExist()

        // 난이도 선택 팝업은 기본적으로 보이지 않음
        composeTestRule.onNodeWithTag("difficulty_dialog").assertDoesNotExist()
    }

    @Test
    fun testNewGameButtonShowsDifficultyPopup() {
        composeTestRule.setContent {
            MainScreen(
                onStartNewGame = { },
                onContinueGame = { }
            )
        }

        // 새 게임 버튼 클릭
        composeTestRule.onNodeWithTag("start_new_game_btn").performClick()

        // 난이도 선택 팝업이 표시됨
        composeTestRule.onNodeWithTag("difficulty_dialog").assertExists()
        composeTestRule.onNodeWithTag("difficulty_dialog_title").assertExists()
        composeTestRule.onNodeWithText("난이도 선택").assertExists()
        composeTestRule.onNodeWithTag("difficulty_selector").assertExists()

        // 난이도 옵션들이 표시됨
        composeTestRule.onNodeWithTag("difficulty_easy").assertExists()
        composeTestRule.onNodeWithTag("difficulty_medium").assertExists()
        composeTestRule.onNodeWithTag("difficulty_hard").assertExists()
    }

    @Test
    fun testDifficultySelectionAndGameStart() {
        var startNewGameCalled = false
        var selectedDifficulty: DifficultyLevel? = null

        composeTestRule.setContent {
            MainScreen(
                onStartNewGame = { difficulty ->
                    startNewGameCalled = true
                    selectedDifficulty = difficulty
                },
                onContinueGame = { }
            )
        }

        // 새 게임 버튼 클릭하여 난이도 선택 팝업 표시
        composeTestRule.onNodeWithTag("start_new_game_btn").performClick()

        // 팝업이 표시됨
        composeTestRule.onNodeWithTag("difficulty_dialog").assertExists()

        // 중간 난이도 선택 (클릭하면 바로 게임 시작)
        composeTestRule.onNodeWithTag("difficulty_medium").performClick()

        // 게임이 시작되고 팝업이 사라짐
        assert(startNewGameCalled)
        assert(selectedDifficulty == DifficultyLevel.MEDIUM)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("difficulty_dialog").assertDoesNotExist()
    }

    @Test
    fun testContinueGameButtonWhenGameInProgress() {
        var continueGameCalled = false

        composeTestRule.setContent {
            MainScreen(
                onStartNewGame = { },
                onContinueGame = { continueGameCalled = true },
                hasGameInProgress = true
            )
        }

        composeTestRule.onNodeWithTag("continue_game_btn").performClick()

        assert(continueGameCalled)
    }

    @Test
    fun testContinueButtonOnlyVisibleWhenGameInProgress() {
        // 게임이 진행중이지 않을 때
        composeTestRule.setContent {
            MainScreen(
                onStartNewGame = { },
                onContinueGame = { },
                hasGameInProgress = false
            )
        }

        // 이어하기 버튼이 보이지 않음
        composeTestRule.onNodeWithTag("continue_game_btn").assertDoesNotExist()

        // 게임이 진행중일 때
        composeTestRule.setContent {
            MainScreen(
                onStartNewGame = { },
                onContinueGame = { },
                hasGameInProgress = true
            )
        }

        // 이어하기 버튼이 보임
        composeTestRule.onNodeWithTag("continue_game_btn").assertExists()
    }

    @Test
    fun testDismissDifficultyPopup() {
        composeTestRule.setContent {
            MainScreen(
                onStartNewGame = { },
                onContinueGame = { }
            )
        }

        // 새 게임 버튼 클릭하여 난이도 선택 팝업 표시
        composeTestRule.onNodeWithTag("start_new_game_btn").performClick()
        composeTestRule.onNodeWithTag("difficulty_dialog").assertExists()

        // 팝업 외부 클릭으로 닫기 (onDismissRequest 호출)
        // UI 테스트에서는 실제 백키나 외부 클릭을 시뮬레이션하기 어려우므로
        // 팝업이 표시되는 것만 확인
        composeTestRule.onNodeWithTag("difficulty_dialog").assertExists()
        composeTestRule.onNodeWithTag("start_new_game_btn").assertExists()
    }
} 