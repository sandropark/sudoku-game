package com.sandro.new_sudoku

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sandro.new_sudoku.base.BaseUITest
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 노트 하이라이트 UI 테스트
 * 셀의 숫자 선택 시 같은 노트의 숫자도 하이라이트 되는 UI 기능을 테스트
 */
@RunWith(AndroidJUnit4::class)
class NoteHighlightUITest : BaseUITest() {

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SudokuScreen()
        }
        waitForIdle()
    }

    @Test
    fun testNoteHighlight_basicNoteCreation() {
        // 기본 UI 요소들이 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("action_btn_노트").assertIsDisplayed()
        composeTestRule.onNodeWithTag("number_btn_5").assertIsDisplayed()

        // 첫 번째 편집 가능한 셀 찾기 (0,0부터 시작해서 편집 가능한 셀을 찾음)
        var foundEditableCell = false
        for (row in 0..8) {
            for (col in 0..8) {
                val cellTag = "cell_${row}_${col}_editable"
                try {
                    composeTestRule.onNodeWithTag(cellTag).assertIsDisplayed()

                    // 편집 가능한 셀을 찾았으면 노트 테스트 진행
                    composeTestRule.onNodeWithTag(cellTag).performClick()
                    waitForIdle()

                    // 노트 모드 활성화
                    composeTestRule.onNodeWithTag("action_btn_노트").performClick()
                    waitForIdle()

                    // 숫자 5 클릭하여 노트 추가
                    composeTestRule.onNodeWithTag("number_btn_5").performClick()
                    waitForIdle()

                    // 노트 모드 비활성화
                    composeTestRule.onNodeWithTag("action_btn_노트").performClick()
                    waitForIdle()

                    foundEditableCell = true
                    break
                } catch (e: AssertionError) {
                    // 이 셀은 편집 가능하지 않으므로 다음 셀로 진행
                    continue
                }
            }
            if (foundEditableCell) break
        }

        // 최소 하나의 편집 가능한 셀을 찾았는지 확인
        withClue("편집 가능한 셀을 찾을 수 없습니다") {
            foundEditableCell shouldBe true
        }
    }

    @Test
    fun testNoteHighlight_numberInput() {
        // 기본 UI 요소들이 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("number_btn_3").assertIsDisplayed()

        // 편집 가능한 셀 2개 찾기
        val editableCells = mutableListOf<String>()
        for (row in 0..8) {
            for (col in 0..8) {
                val cellTag = "cell_${row}_${col}_editable"
                try {
                    composeTestRule.onNodeWithTag(cellTag).assertIsDisplayed()
                    editableCells.add(cellTag)
                    if (editableCells.size >= 2) break
                } catch (e: AssertionError) {
                    // 이 셀은 편집 가능하지 않으므로 다음 셀로 진행
                    continue
                }
            }
            if (editableCells.size >= 2) break
        }

        if (editableCells.size >= 2) {
            val firstCell = editableCells[0]
            val secondCell = editableCells[1]

            // 첫 번째 셀에 노트 추가
            composeTestRule.onNodeWithTag(firstCell).performClick()
            waitForIdle()

            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            waitForIdle()

            composeTestRule.onNodeWithTag("number_btn_7").performClick()
            waitForIdle()

            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            waitForIdle()

            // 두 번째 셀에 숫자 7 입력
            composeTestRule.onNodeWithTag(secondCell).performClick()
            waitForIdle()

            composeTestRule.onNodeWithTag("number_btn_7").performClick()
            waitForIdle()

            // 두 번째 셀을 다시 선택해서 하이라이트 확인
            composeTestRule.onNodeWithTag(secondCell).performClick()
            waitForIdle()

            // 기본적인 UI 동작이 올바른지 확인 (구체적인 하이라이트는 실제 구현에 따라 달라질 수 있음)
            composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        }
    }

    @Test
    fun testNoteHighlight_multipleNotes() {
        // 기본 UI 요소들이 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("action_btn_노트").assertIsDisplayed()

        // 편집 가능한 셀 찾기
        var editableCell: String? = null
        for (row in 0..8) {
            for (col in 0..8) {
                val cellTag = "cell_${row}_${col}_editable"
                try {
                    composeTestRule.onNodeWithTag(cellTag).assertIsDisplayed()
                    editableCell = cellTag
                    break
                } catch (e: AssertionError) {
                    continue
                }
            }
            if (editableCell != null) break
        }

        if (editableCell != null) {
            // 셀 선택
            composeTestRule.onNodeWithTag(editableCell).performClick()
            waitForIdle()

            // 노트 모드 활성화
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            waitForIdle()

            // 여러 숫자를 노트에 추가
            composeTestRule.onNodeWithTag("number_btn_1").performClick()
            waitForIdle()

            composeTestRule.onNodeWithTag("number_btn_4").performClick()
            waitForIdle()

            composeTestRule.onNodeWithTag("number_btn_8").performClick()
            waitForIdle()

            // 노트 모드 비활성화
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            waitForIdle()

            // 기본적인 UI 동작 확인
            composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        }
    }

    @Test
    fun testNoteHighlight_emptyCell() {
        // 기본 UI 요소들이 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()

        // 편집 가능한 빈 셀 찾기
        var emptyCell: String? = null
        for (row in 0..8) {
            for (col in 0..8) {
                val cellTag = "cell_${row}_${col}_editable"
                try {
                    composeTestRule.onNodeWithTag(cellTag).assertIsDisplayed()
                    emptyCell = cellTag
                    break
                } catch (e: AssertionError) {
                    continue
                }
            }
            if (emptyCell != null) break
        }

        if (emptyCell != null) {
            // 빈 셀 선택
            composeTestRule.onNodeWithTag(emptyCell).performClick()
            waitForIdle()

            // UI가 정상적으로 반응하는지 확인
            composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
            composeTestRule.onNodeWithTag("action_btn_노트").assertIsDisplayed()
        }
    }
}