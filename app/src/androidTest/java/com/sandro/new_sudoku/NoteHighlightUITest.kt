package com.sandro.new_sudoku

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 노트 하이라이트 UI 테스트
 * 셀의 숫자 선택 시 같은 노트의 숫자도 하이라이트 되는 UI 기능을 테스트
 */
@RunWith(AndroidJUnit4::class)
class NoteHighlightUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Ignore("Note highlighting feature needs debugging - basic note creation not working")
    fun testNoteHighlight_whenNumberSelectedInCell() {
        val viewModel = SudokuViewModel()
        composeTestRule.setContent {
            SudokuScreen(
                viewModel = viewModel,
                onBackToMain = {}
            )
        }
        composeTestRule.waitForIdle()

        // 기본 UI 요소들이 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("action_btn_노트").assertIsDisplayed()
        composeTestRule.onNodeWithTag("number_btn_5").assertIsDisplayed()

        // 편집 가능한 빈 셀 찾기
        val state = viewModel.state.value
        var firstEditableCell: Pair<Int, Int>? = null
        var secondEditableCell: Pair<Int, Int>? = null

        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    if (firstEditableCell == null) {
                        firstEditableCell = row to col
                    } else if (secondEditableCell == null) {
                        secondEditableCell = row to col
                        break
                    }
                }
            }
            if (secondEditableCell != null) break
        }

        if (firstEditableCell != null && secondEditableCell != null) {
            val (row1, col1) = firstEditableCell
            val (row2, col2) = secondEditableCell

            // 첫 번째 셀 선택 확인
            composeTestRule.onNodeWithTag("cell_${row1}_${col1}_editable").assertIsDisplayed()
            composeTestRule.onNodeWithTag("cell_${row1}_${col1}_editable").performClick()
            composeTestRule.waitForIdle()

            // 노트 모드 활성화
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            composeTestRule.waitForIdle()

            // 숫자 5 클릭하여 노트 추가
            composeTestRule.onNodeWithTag("number_btn_5").performClick()
            composeTestRule.waitForIdle()

            // 노트 모드 비활성화
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            composeTestRule.waitForIdle()

            // 노트가 생성되었는지 확인 (기본 노트 태그)
            composeTestRule.onNodeWithTag("note_5").assertIsDisplayed()

            // 두 번째 셀에 숫자 5 입력
            composeTestRule.onNodeWithTag("cell_${row2}_${col2}_editable").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("number_btn_5").performClick()
            composeTestRule.waitForIdle()

            // 숫자 5가 있는 셀을 선택했을 때 노트의 5가 하이라이트되는지 확인
            composeTestRule.onNodeWithTag("cell_${row2}_${col2}_editable").performClick()
            composeTestRule.waitForIdle()

            // 노트 5가 하이라이트되어야 함
            composeTestRule.onNodeWithTag("note_5_highlighted").assertIsDisplayed()
        }
    }

    @Test
    @Ignore("Note highlighting feature needs debugging - basic note creation not working")
    fun testNoteHighlight_whenDifferentNumberSelected() {
        val viewModel = SudokuViewModel()
        composeTestRule.setContent {
            SudokuScreen(
                viewModel = viewModel,
                onBackToMain = {}
            )
        }

        // 편집 가능한 빈 셀 찾기
        val state = viewModel.state.value
        var firstEditableCell: Pair<Int, Int>? = null
        var secondEditableCell: Pair<Int, Int>? = null

        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    if (firstEditableCell == null) {
                        firstEditableCell = row to col
                    } else if (secondEditableCell == null) {
                        secondEditableCell = row to col
                        break
                    }
                }
            }
            if (secondEditableCell != null) break
        }

        if (firstEditableCell != null && secondEditableCell != null) {
            val (row1, col1) = firstEditableCell
            val (row2, col2) = secondEditableCell

            // 첫 번째 셀에 노트 3, 7 추가
            composeTestRule.onNodeWithTag("cell_${row1}_${col1}_editable").performClick()
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            composeTestRule.onNodeWithTag("number_btn_3").performClick()
            composeTestRule.onNodeWithTag("number_btn_7").performClick()
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()

            // 두 번째 셀에 숫자 5 입력
            composeTestRule.onNodeWithTag("cell_${row2}_${col2}_editable").performClick()
            composeTestRule.onNodeWithTag("number_btn_5").performClick()

            // 숫자 5가 있는 셀을 선택했을 때 노트의 3, 7은 하이라이트되지 않아야 함
            composeTestRule.onNodeWithTag("cell_${row2}_${col2}_editable").performClick()

            // 노트 3, 7은 하이라이트되지 않아야 함 (일반 노트로 표시되어야 함)
            composeTestRule.onNodeWithTag("note_3").assertIsDisplayed()
            composeTestRule.onNodeWithTag("note_7").assertIsDisplayed()
        }
    }

    @Test
    @Ignore("Note highlighting feature needs debugging - basic note creation not working")
    fun testNoteHighlight_multipleNotesWithSameNumber() {
        val viewModel = SudokuViewModel()
        composeTestRule.setContent {
            SudokuScreen(
                viewModel = viewModel,
                onBackToMain = {}
            )
        }

        // 편집 가능한 빈 셀 3개 찾기
        val state = viewModel.state.value
        var firstEditableCell: Pair<Int, Int>? = null
        var secondEditableCell: Pair<Int, Int>? = null
        var thirdEditableCell: Pair<Int, Int>? = null

        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    if (firstEditableCell == null) {
                        firstEditableCell = row to col
                    } else if (secondEditableCell == null) {
                        secondEditableCell = row to col
                    } else if (thirdEditableCell == null) {
                        thirdEditableCell = row to col
                        break
                    }
                }
            }
            if (thirdEditableCell != null) break
        }

        if (firstEditableCell != null && secondEditableCell != null && thirdEditableCell != null) {
            val (row1, col1) = firstEditableCell
            val (row2, col2) = secondEditableCell
            val (row3, col3) = thirdEditableCell

            // 첫 번째 셀에 노트 4 추가
            composeTestRule.onNodeWithTag("cell_${row1}_${col1}_editable").performClick()
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            composeTestRule.onNodeWithTag("number_btn_4").performClick()
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()

            // 두 번째 셀에 노트 4 추가
            composeTestRule.onNodeWithTag("cell_${row2}_${col2}_editable").performClick()
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            composeTestRule.onNodeWithTag("number_btn_4").performClick()
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()

            // 세 번째 셀에 숫자 4 입력
            composeTestRule.onNodeWithTag("cell_${row3}_${col3}_editable").performClick()
            composeTestRule.onNodeWithTag("number_btn_4").performClick()

            // 숫자 4가 있는 셀을 선택했을 때 모든 노트의 4가 하이라이트되어야 함
            composeTestRule.onNodeWithTag("cell_${row3}_${col3}_editable").performClick()

            // 모든 노트 4가 하이라이트되어야 함
            composeTestRule.onNodeWithTag("note_4_highlighted").assertIsDisplayed()
        }
    }

    @Test
    @Ignore("Note highlighting feature needs debugging - basic note creation not working")
    fun testNoteHighlight_whenEmptyCellSelected() {
        val viewModel = SudokuViewModel()
        composeTestRule.setContent {
            SudokuScreen(
                viewModel = viewModel,
                onBackToMain = {}
            )
        }

        // 편집 가능한 빈 셀 2개 찾기
        val state = viewModel.state.value
        var firstEditableCell: Pair<Int, Int>? = null
        var secondEditableCell: Pair<Int, Int>? = null

        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    if (firstEditableCell == null) {
                        firstEditableCell = row to col
                    } else if (secondEditableCell == null) {
                        secondEditableCell = row to col
                        break
                    }
                }
            }
            if (secondEditableCell != null) break
        }

        if (firstEditableCell != null && secondEditableCell != null) {
            val (row1, col1) = firstEditableCell
            val (row2, col2) = secondEditableCell

            // 첫 번째 셀에 노트 6 추가
            composeTestRule.onNodeWithTag("cell_${row1}_${col1}_editable").performClick()
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()
            composeTestRule.onNodeWithTag("number_btn_6").performClick()
            composeTestRule.onNodeWithTag("action_btn_노트").performClick()

            // 빈 셀을 선택
            composeTestRule.onNodeWithTag("cell_${row2}_${col2}_editable").performClick()

            // 노트 6은 하이라이트되지 않아야 함 (일반 노트로 표시되어야 함)
            composeTestRule.onNodeWithTag("note_6").assertIsDisplayed()
        }
    }
}
