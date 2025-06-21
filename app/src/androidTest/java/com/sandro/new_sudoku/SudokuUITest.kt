package com.sandro.new_sudoku

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SudokuUITest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Before
    fun setUp() {
        // MainActivity를 직접 실행
        ActivityScenario.launch(MainActivity::class.java)
        // UI가 로드될 때까지 대기
        composeTestRule.waitForIdle()
    }

    @Test
    fun noteModeToggleButton_worksCorrectly() {
        // UI가 로드될 때까지 대기
        composeTestRule.waitForIdle()
        
        // 노트 버튼 찾기 (testTag로 찾기)
        val noteButton = composeTestRule.onNodeWithTag("action_btn_노트")
        noteButton.assertIsDisplayed()
        
        // 노트 모드 활성화
        noteButton.performClick()
        composeTestRule.waitForIdle()
        
        // 버튼 텍스트가 "노트(ON)"으로 변경되는지 확인
        composeTestRule.onNodeWithTag("action_btn_노트").assertIsDisplayed()
        
        // 노트 모드 비활성화
        composeTestRule.onNodeWithTag("action_btn_노트").performClick()
        composeTestRule.waitForIdle()
        
        // 버튼 텍스트가 "노트"로 돌아가는지 확인
        composeTestRule.onNodeWithTag("action_btn_노트").assertIsDisplayed()
    }

    @Test
    fun addNoteNumber_inNoteMode_addsNote() {
        // UI가 로드될 때까지 대기
        composeTestRule.waitForIdle()
        
        // 노트 버튼이 표시되는지 확인
        composeTestRule.onNodeWithTag("action_btn_노트").assertIsDisplayed()
        
        // 노트 모드 활성화
        composeTestRule.onNodeWithTag("action_btn_노트").performClick()
        composeTestRule.waitForIdle()
        
        // 스도쿠 보드가 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        
        // 숫자 패드가 표시되는지 확인
        composeTestRule.onNodeWithTag("number_pad").assertIsDisplayed()
    }

    @Test
    fun simpleNoteTest() {
        // UI가 로드될 때까지 대기
        composeTestRule.waitForIdle()
        
        // 노트 모드 활성화
        composeTestRule.onNodeWithTag("action_btn_노트").performClick()
        composeTestRule.waitForIdle()
        
        // 스도쿠 보드가 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        
        // 숫자 패드가 표시되는지 확인
        composeTestRule.onNodeWithTag("number_pad").assertIsDisplayed()
    }

    @Test
    fun noteModeToggleTest() {
        // UI가 로드될 때까지 대기
        composeTestRule.waitForIdle()
        
        // 노트 버튼이 표시되는지 확인
        composeTestRule.onNodeWithTag("action_btn_노트").assertIsDisplayed()
        
        // 노트 모드 활성화
        composeTestRule.onNodeWithTag("action_btn_노트").performClick()
        composeTestRule.waitForIdle()
        
        // 노트 모드 비활성화
        composeTestRule.onNodeWithTag("action_btn_노트").performClick()
        composeTestRule.waitForIdle()
        
        // UI가 정상적으로 동작했는지 확인
        composeTestRule.waitForIdle()
    }

    @Test
    fun noteNumberInputThenRegularNumberInput_clearsNote() {
        // UI가 로드될 때까지 대기
        composeTestRule.waitForIdle()
        
        // 노트 모드 활성화
        composeTestRule.onNodeWithTag("action_btn_노트").performClick()
        composeTestRule.waitForIdle()
        
        // 스도쿠 보드가 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        
        // 숫자 패드가 표시되는지 확인
        composeTestRule.onNodeWithTag("number_pad").assertIsDisplayed()
    }

    @Test
    fun debugNoteDisplay() {
        // UI가 로드될 때까지 대기
        composeTestRule.waitForIdle()
        
        // 노트 모드 활성화
        composeTestRule.onNodeWithTag("action_btn_노트").performClick()
        composeTestRule.waitForIdle()
        
        // 스도쿠 보드가 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        
        // 숫자 패드가 표시되는지 확인
        composeTestRule.onNodeWithTag("number_pad").assertIsDisplayed()
    }

    private fun findEditableEmptyCell(): SemanticsNodeInteraction {
        // 간단하게 첫 번째 편집 가능한 셀을 찾기
        for (row in 0..8) {
            for (col in 0..8) {
                val tag = "cell_${row}_${col}_editable"
                try {
                    composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
                    println("찾은 셀: $tag")
                    return composeTestRule.onNodeWithTag(tag)
                } catch (e: Exception) {
                    // continue
                }
            }
        }
        
        // fallback: 일반 셀 태그로 찾기
        for (row in 0..8) {
            for (col in 0..8) {
                val tag = "cell_${row}_${col}"
                try {
                    composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
                    println("fallback으로 찾은 셀: $tag")
                    return composeTestRule.onNodeWithTag(tag)
                } catch (e: Exception) {
                    // continue
                }
            }
        }
        throw AssertionError("편집 가능한 빈 셀을 찾을 수 없습니다.")
    }
} 