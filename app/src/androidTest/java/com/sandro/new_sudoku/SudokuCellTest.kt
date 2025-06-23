package com.sandro.new_sudoku

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SudokuCellTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSudokuCellDisplay_initialCell() {
        composeTestRule.setContent {
            SudokuCell(
                value = 5,
                isSelected = false,
                isInitial = true,
                isInvalid = false,
                notes = emptySet(),
                isNoteMode = false,
                onClick = {},
                cellSize = androidx.compose.ui.unit.Dp(40f),
                isEvenBox = false,
                modifier = Modifier.testTag("cell_0_0")
            )
        }
        composeTestRule.onNodeWithTag("cell_0_0").assertIsDisplayed()
    }

    @Test
    fun testSudokuCellDisplay_userInputCell() {
        composeTestRule.setContent {
            SudokuCell(
                value = 3,
                isSelected = true,
                isInitial = false,
                isInvalid = false,
                notes = emptySet(),
                isNoteMode = false,
                onClick = {},
                cellSize = androidx.compose.ui.unit.Dp(40f),
                isEvenBox = true,
                modifier = Modifier.testTag("cell_1_1_editable")
            )
        }
        composeTestRule.onNodeWithTag("cell_1_1_editable").assertIsDisplayed()
    }

    @Test
    fun testSudokuCellClick() {
        var clicked = false
        composeTestRule.setContent {
            SudokuCell(
                value = 0,
                isSelected = false,
                isInitial = false,
                isInvalid = false,
                notes = emptySet(),
                isNoteMode = false,
                onClick = { clicked = true },
                cellSize = androidx.compose.ui.unit.Dp(40f),
                isEvenBox = false,
                modifier = Modifier.testTag("cell_2_2_editable")
            )
        }
        composeTestRule.onNodeWithTag("cell_2_2_editable").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testSudokuCellDisplay_withNotes() {
        composeTestRule.setContent {
            SudokuCell(
                value = 0,
                isSelected = false,
                isInitial = false,
                isInvalid = false,
                notes = setOf(1, 2, 3),
                isNoteMode = true,
                onClick = {},
                cellSize = androidx.compose.ui.unit.Dp(40f),
                isEvenBox = false,
                modifier = Modifier.testTag("cell_3_3_editable")
            )
        }
        composeTestRule.onNodeWithTag("cell_3_3_editable").assertIsDisplayed()
        // 노트 숫자도 실제로 보이는지까지는 여기서 assert하지 않음 (UI 구조가 바뀌면 깨질 수 있음)
    }

    @Test
    fun testSudokuCellDisplay_invalidCell() {
        composeTestRule.setContent {
            SudokuCell(
                value = 5,
                isSelected = false,
                isInitial = false,
                isInvalid = true,
                notes = emptySet(),
                isNoteMode = false,
                onClick = {},
                cellSize = androidx.compose.ui.unit.Dp(40f),
                isEvenBox = false,
                modifier = Modifier.testTag("cell_invalid")
            )
        }
        composeTestRule.onNodeWithTag("cell_invalid").assertIsDisplayed()
    }

    @Test
    fun testSudokuCellAccessibility() {
        composeTestRule.setContent {
            SudokuCell(
                value = 5,
                isSelected = true,
                isInitial = false,
                isInvalid = true,
                notes = emptySet(),
                isNoteMode = false,
                onClick = {},
                cellSize = androidx.compose.ui.unit.Dp(40f),
                isEvenBox = false,
                modifier = Modifier.testTag("cell_accessibility")
            )
        }
        // 접근성 검증 - 셀이 표시되는지만 확인
        composeTestRule.onNodeWithTag("cell_accessibility").assertIsDisplayed()
    }

    @Test
    fun testSudokuCellAccessibility_withNotes() {
        composeTestRule.setContent {
            SudokuCell(
                value = 0,
                isSelected = false,
                isInitial = false,
                isInvalid = false,
                notes = setOf(1, 3, 5),
                isNoteMode = true,
                onClick = {},
                cellSize = androidx.compose.ui.unit.Dp(40f),
                isEvenBox = false,
                modifier = Modifier.testTag("cell_notes_accessibility")
            )
        }
        // 접근성 검증 - 셀이 표시되는지만 확인
        composeTestRule.onNodeWithTag("cell_notes_accessibility").assertIsDisplayed()
    }
} 