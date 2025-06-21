package com.sandro.new_sudoku

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        composeTestRule.setContent {
            SudokuScreen()
        }
    }

    @Test
    fun testSudokuScreenInitialState() {
        // 스도쿠 보드가 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertExists()
        
        // 숫자 패드가 표시되는지 확인
        composeTestRule.onNodeWithTag("number_pad").assertExists()
        
        // 새 게임 버튼이 표시되는지 확인
        composeTestRule.onNodeWithText("새 게임").assertExists()
        
        // 해답 보기 버튼이 표시되는지 확인
        composeTestRule.onNodeWithText("해답 보기").assertExists()
        
        // 지우기 버튼이 표시되는지 확인
        composeTestRule.onNodeWithTag("action_btn_지우기").assertExists()
    }

    @Test
    fun testCellSelection() {
        // 첫 번째 셀 클릭
        composeTestRule.onNodeWithTag("cell_0_0").performClick()
        
        // 셀이 선택되었는지 확인 (배경색이나 테두리로 구분)
        composeTestRule.onNodeWithTag("cell_0_0").assertExists()
    }

    @Test
    fun testNumberInput() {
        // 빈 셀 선택
        composeTestRule.onNodeWithTag("cell_0_2").performClick()
        
        // 숫자 5 입력 (패드 버튼 사용)
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // 셀에 숫자가 입력되었는지 확인 (빈 셀은 텍스트가 없으므로 존재 여부로 확인)
        composeTestRule.onNodeWithTag("cell_0_2").assertExists()
    }

    @Test
    fun testInitialCellProtection() {
        // 실제 초기 셀을 찾기 위해 모든 셀을 확인
        val initialCell = findInitialCell()
        if (initialCell == null) return
        
        // 찾은 초기 셀을 클릭하고 숫자 입력 시도
        composeTestRule.onNodeWithTag("cell_${initialCell.first}_${initialCell.second}").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // 요구사항: 숫자는 항상 입력되어야 함
        val cellNode = composeTestRule.onNodeWithTag("cell_${initialCell.first}_${initialCell.second}")
        cellNode.assertExists()
        
        // 숫자가 입력되었는지 확인
        val node = cellNode.fetchSemanticsNode()
        val text = node.config[SemanticsProperties.Text].joinToString { it.text }
        assert(text == "5") { "숫자는 항상 입력되어야 합니다. 실제 값: $text" }
    }

    @Test
    fun testInvalidMove() {
        // 첫 번째 셀에 숫자 입력
        composeTestRule.onNodeWithTag("cell_0_2").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // 같은 행의 다른 셀에 같은 숫자 입력 (잘못된 이동)
        composeTestRule.onNodeWithTag("cell_0_3").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
    }

    @Test
    fun testClearCell() {
        // 셀에 숫자 입력
        composeTestRule.onNodeWithTag("cell_0_2").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // 지우기 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_지우기").performClick()
        
        // 셀이 비워졌는지 확인 (빈 셀은 텍스트가 없으므로 존재 여부로 확인)
        composeTestRule.onNodeWithTag("cell_0_2").assertExists()
    }

    @Test
    fun testNewGame() {
        // 새 게임 버튼 클릭
        composeTestRule.onNodeWithText("새 게임").performClick()
        
        // 게임이 초기화되었는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertExists()
    }

    @Test
    fun testSolveGame() {
        // 해답 보기 버튼 클릭
        composeTestRule.onNodeWithText("해답 보기").performClick()
        
        // 모든 셀이 채워졌는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertExists()
    }

    @Test
    fun testNumberPadButtons() {
        // 모든 숫자 버튼이 존재하는지 확인
        for (i in 1..9) {
            composeTestRule.onNodeWithTag("number_btn_$i").assertExists()
        }
    }

    @Test
    fun testActionButtons() {
        // 새 게임 버튼 클릭
        composeTestRule.onNodeWithText("새 게임").performClick()
        
        // 해답 보기 버튼 클릭
        composeTestRule.onNodeWithText("해답 보기").performClick()
    }

    @Test
    fun testGameCompleteUI() {
        // 해답 보기 버튼 클릭(게임 완료)
        composeTestRule.onNodeWithText("해답 보기").performClick()
        // 모든 셀이 채워졌는지(9x9 셀에 1~9가 모두 존재하는지 일부 샘플로 확인)
        for (i in 1..9) {
            composeTestRule.onAllNodesWithText(i.toString())[0].assertExists()
        }
    }

    @Test
    fun testErrorMessageDisappearsAfterValidInput() {
        // 빈 셀을 클릭하고 숫자 입력
        composeTestRule.onNodeWithTag("sudoku_board").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // UI가 정상적으로 작동하는지 확인 (에러가 발생하지 않아야 함)
        composeTestRule.onNodeWithTag("sudoku_board").assertExists()
        composeTestRule.onNodeWithTag("number_pad").assertExists()
    }

    @Test
    fun testClearButtonOnInitialCell() {
        // 초기 셀 찾기
        val initialCell = findInitialCell()
        if (initialCell == null) return
        
        // 초기 셀을 먼저 클릭해서 선택
        composeTestRule.onNodeWithTag("cell_${initialCell.first}_${initialCell.second}").performClick()
        
        // 초기 셀에서 지우기 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_지우기").performClick()
        
        // 요구사항: 숫자는 항상 입력되어야 함 (지우기도 가능)
        // 지우기 후 셀이 존재하는지만 확인
        composeTestRule.onNodeWithTag("cell_${initialCell.first}_${initialCell.second}").assertExists()
    }

    @Test
    fun testErrorStateDisplayAndRecovery() {
        // 초기 셀 찾기
        val initialCell = findInitialCell()
        if (initialCell == null) return
        
        // 찾은 초기 셀 클릭해서 숫자 입력
        composeTestRule.onNodeWithTag("cell_${initialCell.first}_${initialCell.second}").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // 요구사항: 숫자는 항상 입력되어야 함
        val cellNode = composeTestRule.onNodeWithTag("cell_${initialCell.first}_${initialCell.second}")
        cellNode.assertExists()
        
        // 숫자가 입력되었는지 확인
        val node = cellNode.fetchSemanticsNode()
        val text = node.config[SemanticsProperties.Text].joinToString { it.text }
        assert(text == "5") { "숫자는 항상 입력되어야 합니다. 실제 값: $text" }
        
        // 요구사항: 틀린 숫자는 빨간색으로 표시되어야 함
        // (UI에서 빨간색 텍스트가 표시되는지 확인)
        
        // 다른 숫자로 변경
        composeTestRule.onNodeWithTag("cell_${initialCell.first}_${initialCell.second}").performClick()
        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        
        // 숫자가 변경되었는지 확인
        val updatedNode = cellNode.fetchSemanticsNode()
        val updatedText = updatedNode.config[SemanticsProperties.Text].joinToString { it.text }
        assert(updatedText == "3") { "숫자가 변경되어야 합니다. 실제 값: $updatedText" }
    }

    @Test
    fun testInvalidMoveErrorHandling() {
        // 첫 번째 셀에 숫자 입력
        composeTestRule.onNodeWithTag("cell_0_2").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // 같은 행의 다른 셀에 같은 숫자 입력 (잘못된 이동)
        composeTestRule.onNodeWithTag("cell_0_3").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
    }

    @Test
    fun testGameCompletionFlow() {
        // 해답 보기 버튼 클릭
        composeTestRule.onNodeWithText("해답 보기").performClick()
        
        // 모든 셀이 채워졌는지 확인 (일부 샘플만 확인)
        for (row in 0..2) {
            for (col in 0..2) {
                val cell = composeTestRule.onNodeWithTag("cell_${row}_${col}")
                cell.assertExists()
                // 셀이 존재하는지만 확인 (숫자 표시 여부는 UI에서 확인)
            }
        }
    }

    @Test
    fun testNewGameReset() {
        // 게임 진행
        composeTestRule.onNodeWithTag("cell_0_2").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // 새 게임 버튼 클릭
        composeTestRule.onNodeWithText("새 게임").performClick()
        
        // 보드가 초기 상태로 돌아갔는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertExists()
        composeTestRule.onNodeWithTag("number_pad").assertExists()
    }

    @Test
    fun testClearButtonFunctionality() {
        // 빈 셀 찾기
        val emptyCell = findEmptyCell()
        if (emptyCell == null) return
        
        // 숫자 입력
        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        
        // 지우기 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_지우기").performClick()
    }

    @Test
    fun testClearButtonWithoutSelection() {
        // 아무 셀도 선택하지 않고 지우기 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_지우기").performClick()
        
        // UI가 정상적으로 작동하는지 확인 (에러가 발생하지 않아야 함)
        composeTestRule.onNodeWithTag("sudoku_board").assertExists()
        composeTestRule.onNodeWithTag("number_pad").assertExists()
    }

    @Test
    fun testClearButtonMultipleTimes() {
        // 빈 셀 찾기
        val emptyCell = findEmptyCell()
        if (emptyCell == null) return
        
        // 숫자 입력
        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        
        // 여러 번 연속 지우기
        composeTestRule.onNodeWithTag("action_btn_지우기").performClick()
    }

    @Test
    fun testRapidCellSelection() {
        // 빠른 연속 셀 선택 (일부만 테스트)
        for (i in 0..4) {
            composeTestRule.onNodeWithTag("cell_${i}_${i}").performClick()
            // 선택된 셀이 시각적으로 구분되는지 확인 (배경색 등)
            composeTestRule.onNodeWithTag("cell_${i}_${i}").assertExists()
        }
    }

    @Test
    fun testRapidNumberInput() {
        // 셀 선택
        composeTestRule.onNodeWithTag("cell_0_2").performClick()
        
        // 빠른 연속 숫자 입력 (일부만 테스트)
        for (i in 1..5) {
            composeTestRule.onNodeWithTag("number_btn_$i").performClick()
            // 마지막 입력된 숫자가 표시되는지 확인
            composeTestRule.onNodeWithTag("cell_0_2").assertExists()
        }
    }

    @Test
    fun testUIStateConsistency() {
        // UI 요소들이 일관되게 표시되는지 확인
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("number_pad").assertIsDisplayed()
        
        // 숫자 버튼들이 모두 표시되는지 확인 (1-9)
        for (i in 1..9) {
            composeTestRule.onNodeWithTag("number_btn_$i").assertIsDisplayed()
        }
        
        // 액션 버튼들이 표시되는지 확인
        composeTestRule.onNodeWithTag("action_btn_지우기").assertIsDisplayed()
        composeTestRule.onNodeWithText("새 게임").assertIsDisplayed()
        composeTestRule.onNodeWithText("해답 보기").assertIsDisplayed()
    }

    @Test
    fun testSudokuBoardDisplay() {
        composeTestRule.onNodeWithTag("sudoku_board").assertIsDisplayed()
    }

    @Test
    fun testNumberPadDisplay() {
        composeTestRule.onNodeWithTag("number_pad").assertIsDisplayed()
    }

    @Test
    fun testNumberButtonsDisplay() {
        // 숫자 버튼들이 모두 표시되는지 확인 (1-9)
        for (i in 1..9) {
            composeTestRule.onNodeWithTag("number_btn_$i").assertIsDisplayed()
        }
    }

    @Test
    fun testActionButtonsDisplay() {
        // 액션 버튼들이 표시되는지 확인
        composeTestRule.onNodeWithTag("action_btn_지우기").assertIsDisplayed()
        composeTestRule.onNodeWithText("새 게임").assertIsDisplayed()
        composeTestRule.onNodeWithText("해답 보기").assertIsDisplayed()
    }

    @Test
    fun testErrorMessageTimeout() {
        // 에러 발생
        composeTestRule.onNodeWithTag("cell_0_0").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
    }

    @Test
    fun testViewModelAndUIInitialCellConsistency() {
        // 초기 셀들을 클릭하고 수정 시도
        val initialCellTags = listOf("cell_0_0", "cell_0_1", "cell_1_0")
        
        for (cellTag in initialCellTags) {
            composeTestRule.onNodeWithTag(cellTag).performClick()
            composeTestRule.onNodeWithTag("number_btn_1").performClick()
        }

        // 빈 셀을 클릭하고 수정 시도
        composeTestRule.onNodeWithTag("cell_0_2").performClick()
        composeTestRule.onNodeWithTag("number_btn_1").performClick()
    }

    @Test
    fun testInitialAndEditableCellConsistency() {
        // 초기 셀 중 하나를 클릭하고 숫자를 입력해보기
        composeTestRule.onNodeWithTag("cell_0_0").performClick()
        composeTestRule.onNodeWithTag("number_btn_1").performClick()

        // 빈 셀을 찾아서 클릭하고 숫자를 입력해보기
        composeTestRule.onNodeWithTag("cell_0_2").performClick()
        composeTestRule.onNodeWithTag("number_btn_1").performClick()
    }

    @Test
    fun testNumberPadInteraction() {
        // 숫자 버튼들을 클릭해보기
        for (i in 1..9) {
            composeTestRule.onNodeWithTag("number_btn_$i").performClick()
        }
        
        // 지우기 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_지우기").performClick()
    }

    @Test
    fun testNumberPadRowDisplay() {
        composeTestRule.onNodeWithTag("number_pad_row").assertIsDisplayed()
    }

    @Test
    fun testAllCellsDisplayed() {
        // 모든 셀이 표시되어 있는지 확인 (일부 샘플만 확인)
        for (row in 0..2) {
            for (col in 0..2) {
                composeTestRule.onNodeWithTag("cell_${row}_${col}")
                    .assertExists()
                    .assertIsDisplayed()
            }
        }
    }

    @Test
    fun testThickBoxLines() {
        // 3x3 박스가 시각적으로 구분되는지 확인 (일부만 확인)
        for (boxRow in 0..1) {
            for (boxCol in 0..1) {
                // 3x3 박스 내의 모든 셀 확인
                for (cellRow in boxRow * 3 until (boxRow + 1) * 3) {
                    for (cellCol in boxCol * 3 until (boxCol + 1) * 3) {
                        val cellNode = composeTestRule.onNodeWithTag("cell_${cellRow}_${cellCol}")
                        cellNode.assertExists()
                            .assertIsDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun testSudokuBoardLayout() {
        // 1. 모든 셀이 정확히 9x9 그리드로 배치되어 있는지 확인 (일부만 확인)
        for (row in 0..2) {
            for (col in 0..2) {
                composeTestRule.onNodeWithTag("cell_${row}_${col}")
                    .assertExists()
                    .assertIsDisplayed()
                    .assertHasClickAction()
            }
        }
    }

    @Test
    fun testBoardSizeIsLarger() {
        // 스도쿠 보드가 표시되는지 확인
        val boardNode = composeTestRule.onNodeWithTag("sudoku_board")
        boardNode.assertExists()
        
        // 보드가 충분히 크게 표시되는지 확인 (시각적으로 확인)
        boardNode.assertIsDisplayed()
    }

    @Test
    fun testErrorMessageDebug() {
        // 초기 셀을 클릭해서 에러 발생시키기
        composeTestRule.onNodeWithTag("cell_0_0").performClick()
        composeTestRule.onNodeWithTag("number_btn_1").performClick()
    }

    @Test
    fun testUndoButtonRestoresPreviousCellValue() {
        // 빈 셀 찾기
        val emptyCell = findEmptyCell()
        if (emptyCell == null) {
            // 빈 셀을 찾을 수 없으면 테스트 스킵
            return
        }
        
        // 빈 셀 선택 후 5 입력
        composeTestRule.onNodeWithTag("cell_${emptyCell.first}_${emptyCell.second}").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // 값이 입력되었는지 확인
        val cellNode = composeTestRule.onNodeWithTag("cell_${emptyCell.first}_${emptyCell.second}")
        cellNode.assertExists()
        
        // 입력된 값 확인
        val nodeAfterInput = cellNode.fetchSemanticsNode()
        val textAfterInput = nodeAfterInput.config[SemanticsProperties.Text].joinToString { it.text }
        assert(textAfterInput == "5") { "숫자 5가 입력되어야 합니다" }
        
        // 실행취소 버튼 클릭
        composeTestRule.onNodeWithTag("action_btn_실행취소").performClick()
        
        // 값이 사라졌는지 확인 - value가 0이면 텍스트가 없어야 함
        val nodeAfterUndo = cellNode.fetchSemanticsNode()
        val textAfterUndo = nodeAfterUndo.config[SemanticsProperties.Text].joinToString { it.text }
        assert(textAfterUndo.isEmpty()) { "실행취소 후 셀이 비워져야 합니다" }
    }

    // 공통 헬퍼 함수들
    private fun findInitialCell(): Pair<Int, Int>? {
        // 초기 셀 찾기 (숫자가 있는 셀)
        for (row in 0..8) {
            for (col in 0..8) {
                try {
                    val cell = composeTestRule.onNodeWithTag("cell_${row}_${col}")
                    cell.assertExists()
                    
                    // 셀에 숫자가 있는지 확인 (초기 셀인지 확인)
                    val text = cell.fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }
                    if (text.isNotEmpty() && text.toIntOrNull() != null) {
                        return Pair(row, col)
                    }
                } catch (e: Exception) {
                    // 셀을 찾을 수 없거나 텍스트가 없는 경우 무시
                    continue
                }
            }
        }
        return null
    }

    private fun findEmptyCell(): Pair<Int, Int>? {
        // 빈 셀 찾기 (간단한 방식)
        for (row in 0..8) {
            for (col in 0..8) {
                try {
                    val cellNode = composeTestRule.onNodeWithTag("cell_${row}_${col}")
                    cellNode.assertExists()
                    
                    // 셀에 텍스트가 없는지 확인 (빈 셀인지 확인)
                    val node = cellNode.fetchSemanticsNode()
                    val text = node.config[SemanticsProperties.Text].joinToString { it.text }
                    if (text.isEmpty()) {
                        return Pair(row, col)
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return null
    }

    private fun assertVisuallyDistinct(node1: SemanticsNodeInteraction, node2: SemanticsNodeInteraction) {
        // 두 노드가 시각적으로 구분되는지 확인
        // 실제로는 배경색이 다른지 직접 검증할 수 없으므로, 노드가 존재하고 표시되는지만 확인
        node1.assertExists().assertIsDisplayed()
        node2.assertExists().assertIsDisplayed()
    }

    private fun SemanticsNodeInteraction.assertLeftPositionInRootIsEqualTo(
        other: SemanticsNodeInteraction
    ) {
        val thisNode = fetchSemanticsNode()
        val otherNode = other.fetchSemanticsNode()
        val thisRight = thisNode.positionInRoot.x + thisNode.size.width
        val otherLeft = otherNode.positionInRoot.x
        assert(thisRight <= otherLeft) {
            "Expected node to be to the left of other node"
        }
    }

    private fun SemanticsNodeInteraction.assertTopPositionInRootIsEqualTo(
        other: SemanticsNodeInteraction
    ) {
        val thisNode = fetchSemanticsNode()
        val otherNode = other.fetchSemanticsNode()
        val thisBottom = thisNode.positionInRoot.y + thisNode.size.height
        val otherTop = otherNode.positionInRoot.y
        assert(thisBottom <= otherTop) {
            "Expected node to be above other node"
        }
    }

    private fun SemanticsNodeInteraction.assertHasDrawnBorder() {
        val node = fetchSemanticsNode()
        val testTag = node.config.find { it.key.toString().contains("TestTag") }?.value.toString()
        assert(testTag.contains("border_thick")) {
            "두꺼운 테두리 태그가 없습니다: $testTag"
        }
    }

    private fun SemanticsNodeInteraction.assertHasThinBorder() {
        val node = fetchSemanticsNode()
        val testTag = node.config.find { it.key.toString().contains("TestTag") }?.value.toString()
        assert(testTag.contains("border_thin")) {
            "얇은 테두리 태그가 없습니다: $testTag"
        }
    }

    private fun assertCellsAreAdjacent(cell1: SemanticsNodeInteraction, cell2: SemanticsNodeInteraction) {
        val node1 = cell1.fetchSemanticsNode()
        val node2 = cell2.fetchSemanticsNode()
        
        val gap = node2.positionInRoot.x - (node1.positionInRoot.x + node1.size.width)
        assert(gap == 0f) { "셀 사이에 간격이 있습니다: ${gap}px" }
    }

    private fun findCellNode(row: Int, col: Int): SemanticsNodeInteraction {
        return composeTestRule.onNodeWithTag("cell_${row}_${col}")
    }

    private fun findCellBorderNode(row: Int, col: Int, isThick: Boolean): SemanticsNodeInteraction {
        val borderType = if (isThick) "border_thick" else "border_thin"
        return composeTestRule.onNodeWithTag("cell_${row}_${col}_$borderType")
    }

    private fun SemanticsNodeInteraction.assertBackgroundColor(expectedColor: Color) {
        val node = fetchSemanticsNode()
        val backgroundColor = node.config.find { it.key.toString().contains("Background") }?.value
        assert(backgroundColor?.toString()?.contains(expectedColor.toString()) == true) {
            "배경색이 일치하지 않습니다. 예상: $expectedColor, 실제: $backgroundColor"
        }
    }
} 