package com.sandro.new_sudoku

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MistakeCountUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        // SudokuScreen을 직접 테스트
        composeTestRule.setContent {
            SudokuScreen()
        }
        // UI가 로드될 때까지 대기
        composeTestRule.waitForIdle()
    }

    @Test
    fun testInitialMistakeCountIsZero() {
        // 초기 실수 카운트가 0인지 확인
        composeTestRule.onNodeWithText("실수: 0").assertIsDisplayed()
    }

    @Test
    fun testStatusBarDisplaysMistakeCount() {
        // StatusBar가 표시되는지 확인
        composeTestRule.onNodeWithText("전문가").assertIsDisplayed()
        composeTestRule.onNodeWithText("실수: 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("00:21").assertIsDisplayed()
    }

    @Test
    fun testGameScreenElementsAreVisible() {
        // 게임 화면의 주요 요소들이 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("number_pad").assertIsDisplayed()
        composeTestRule.onNodeWithTag("action_btn_실행취소").assertIsDisplayed()
        composeTestRule.onNodeWithTag("action_btn_지우기").assertIsDisplayed()
        composeTestRule.onNodeWithTag("action_btn_노트").assertIsDisplayed()
    }

    @Test
    fun testBackButtonIsDisplayed() {
        // 뒤로가기 버튼이 표시되는지 확인
        composeTestRule.onNodeWithText("←").assertIsDisplayed()
    }

    @Test
    fun testDifficultyLevelDisplayed() {
        // 난이도가 표시되는지 확인 (현재는 "전문가"로 하드코딩)
        composeTestRule.onNodeWithText("전문가").assertIsDisplayed()
    }
} 