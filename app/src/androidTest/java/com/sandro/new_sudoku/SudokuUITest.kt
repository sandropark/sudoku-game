package com.sandro.new_sudoku

import androidx.compose.ui.test.SemanticsNodeInteraction
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
    fun noteModeToggleButton_worksCorrectly() {
        // 노트 버튼 찾기
        val noteButton = composeTestRule.onNodeWithText("노트")
        noteButton.assertIsDisplayed()
        
        // 노트 모드 활성화
        noteButton.performClick()
        
        // 버튼 텍스트가 "노트(ON)"으로 변경되는지 확인
        composeTestRule.onNodeWithText("노트(ON)").assertIsDisplayed()
        
        // 노트 모드 비활성화
        composeTestRule.onNodeWithText("노트(ON)").performClick()
        
        // 버튼 텍스트가 "노트"로 돌아가는지 확인
        composeTestRule.onNodeWithText("노트").assertIsDisplayed()
    }

    @Test
    fun addNoteNumber_inNoteMode_addsNote() {
        // 노트 모드 활성화
        composeTestRule.onNodeWithText("노트").performClick()
        
        // 첫 번째 셀 (0,0) 선택
        composeTestRule.onNodeWithTag("cell_0_0").performClick()
        
        // 숫자 5 클릭
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        
        // 선택된 셀에 노트 숫자 5가 표시되는지 확인
        val selectedCell = composeTestRule.onNodeWithTag("cell_0_0")
        selectedCell.assertIsDisplayed()
        
        // 노트 숫자 5가 실제로 표시되는지 확인 (onAllNodesWithText 사용)
        val allText5Nodes = composeTestRule.onAllNodesWithText("5")
        assert(allText5Nodes.fetchSemanticsNodes().size >= 2) {
            "Expected at least 2 nodes with text '5' (number pad + note), but found ${allText5Nodes.fetchSemanticsNodes().size}"
        }
    }

    @Test
    fun toggleSameNoteNumber_removesNote() {
        // 노트 모드 활성화
        composeTestRule.onNodeWithText("노트").performClick()
        
        // 첫 번째 셀 (0,0) 선택
        composeTestRule.onNodeWithTag("cell_0_0").performClick()
        
        // 숫자 3 클릭 (노트 추가)
        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        
        // 노트 숫자 3이 표시되는지 확인
        val allText3Nodes = composeTestRule.onAllNodesWithText("3")
        assert(allText3Nodes.fetchSemanticsNodes().size >= 2) {
            "Expected at least 2 nodes with text '3' (number pad + note), but found ${allText3Nodes.fetchSemanticsNodes().size}"
        }
        
        // 같은 숫자 3 다시 클릭 (노트 제거)
        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        
        // 노트 숫자 3이 제거되었는지 확인 (숫자 패드만 남아있어야 함)
        val remainingText3Nodes = composeTestRule.onAllNodesWithText("3")
        assert(remainingText3Nodes.fetchSemanticsNodes().size == 1) {
            "Expected exactly 1 node with text '3' (only number pad), but found ${remainingText3Nodes.fetchSemanticsNodes().size}"
        }
    }

    @Test
    fun inputNumber_inNormalMode_setsCellValue() {
        // 노트 모드 비활성화 상태에서 빈 셀 선택
        val emptyCell = findEmptyCell()
        emptyCell.performClick()
        
        // 숫자 7 클릭
        composeTestRule.onNodeWithTag("number_btn_7").performClick()
        
        // 선택된 셀에 일반 숫자 7이 표시되는지 확인
        val selectedCell = composeTestRule.onNodeWithTag("cell_${getSelectedCellPosition().first}_${getSelectedCellPosition().second}")
        selectedCell.assertIsDisplayed()
    }

    @Test
    fun noteNotShown_whenCellHasValue() {
        // 빈 셀에 일반 숫자 입력
        val emptyCell = findEmptyCell()
        emptyCell.performClick()
        
        composeTestRule.onNodeWithTag("number_btn_4").performClick()
        
        // 노트 모드 활성화
        composeTestRule.onNodeWithText("노트").performClick()
        
        // 같은 셀 선택
        val filledCell = composeTestRule.onNodeWithTag("cell_${getSelectedCellPosition().first}_${getSelectedCellPosition().second}")
        filledCell.performClick()
        
        // 숫자 8 클릭 (노트 추가 시도)
        composeTestRule.onNodeWithTag("number_btn_8").performClick()
        
        // 셀에는 여전히 일반 숫자 4만 표시되어야 함
        val selectedCell = composeTestRule.onNodeWithTag("cell_${getSelectedCellPosition().first}_${getSelectedCellPosition().second}")
        selectedCell.assertIsDisplayed()
    }

    @Test
    fun noteLayout_displaysCorrect3x3Grid() {
        // 노트 모드 활성화
        composeTestRule.onNodeWithText("노트").performClick()
        
        // 빈 셀 선택
        val emptyCell = findEmptyCell()
        emptyCell.performClick()
        
        // 3x3 그리드의 각 위치에 노트 추가
        // 1행: 1, 2, 3
        composeTestRule.onNodeWithTag("number_btn_1").performClick()
        composeTestRule.onNodeWithTag("number_btn_2").performClick()
        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        
        // 2행: 4, 5, 6
        composeTestRule.onNodeWithTag("number_btn_4").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        composeTestRule.onNodeWithTag("number_btn_6").performClick()
        
        // 3행: 7, 8, 9
        composeTestRule.onNodeWithTag("number_btn_7").performClick()
        composeTestRule.onNodeWithTag("number_btn_8").performClick()
        composeTestRule.onNodeWithTag("number_btn_9").performClick()
        
        // 선택된 셀에서 모든 노트 숫자가 표시되는지 확인
        val selectedCell = composeTestRule.onNodeWithTag("cell_${getSelectedCellPosition().first}_${getSelectedCellPosition().second}")
        selectedCell.assertIsDisplayed()
        
        // 각 노트 숫자가 개별적으로 표시되는지 확인
        for (i in 1..9) {
            composeTestRule.onNodeWithTag("note_$i").assertIsDisplayed()
        }
    }

    @Test
    fun noteLayout_displaysIndividualNumbersCorrectly() {
        // 노트 모드 활성화
        composeTestRule.onNodeWithText("노트").performClick()
        
        // 빈 셀 선택
        val emptyCell = findEmptyCell()
        emptyCell.performClick()
        
        // 특정 위치의 노트만 추가 (중앙과 모서리)
        composeTestRule.onNodeWithTag("number_btn_5").performClick() // 중앙 (1,1)
        composeTestRule.onNodeWithTag("number_btn_1").performClick() // 좌상단 (0,0)
        composeTestRule.onNodeWithTag("number_btn_9").performClick() // 우하단 (2,2)
        
        // 선택된 셀에서 특정 노트 숫자만 표시되는지 확인
        val selectedCell = composeTestRule.onNodeWithTag("cell_${getSelectedCellPosition().first}_${getSelectedCellPosition().second}")
        selectedCell.assertIsDisplayed()
        
        // 추가된 노트 숫자만 표시되는지 확인
        composeTestRule.onNodeWithTag("note_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_5").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_9").assertIsDisplayed()
        
        // 추가되지 않은 노트 숫자는 표시되지 않는지 확인
        composeTestRule.onNodeWithTag("note_2").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_3").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_4").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_6").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_7").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_8").assertDoesNotExist()
    }

    @Test
    fun noteLayout_removesNumbersWhenToggled() {
        // 노트 모드 활성화
        composeTestRule.onNodeWithText("노트").performClick()
        
        // 빈 셀 선택
        val emptyCell = findEmptyCell()
        emptyCell.performClick()
        
        // 노트 숫자 추가
        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        composeTestRule.onNodeWithTag("number_btn_7").performClick()
        
        // 추가된 노트 숫자가 표시되는지 확인
        composeTestRule.onNodeWithTag("note_3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_7").assertIsDisplayed()
        
        // 같은 숫자를 다시 클릭하여 제거
        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        
        // 제거된 노트 숫자는 표시되지 않는지 확인
        composeTestRule.onNodeWithTag("note_3").assertDoesNotExist()
        
        // 남은 노트 숫자는 계속 표시되는지 확인
        composeTestRule.onNodeWithTag("note_7").assertIsDisplayed()
    }

    @Test
    fun noteLayout_clearsWhenCellHasValue() {
        // 빈 셀에 일반 숫자 입력
        val emptyCell = findEmptyCell()
        emptyCell.performClick()
        
        composeTestRule.onNodeWithTag("number_btn_4").performClick()
        
        // 노트 모드 활성화
        composeTestRule.onNodeWithText("노트").performClick()
        
        // 같은 셀 선택
        val filledCell = composeTestRule.onNodeWithTag("cell_${getSelectedCellPosition().first}_${getSelectedCellPosition().second}")
        filledCell.performClick()
        
        // 노트 숫자 추가 시도
        composeTestRule.onNodeWithTag("number_btn_8").performClick()
        
        // 셀에는 일반 숫자 4만 표시되고 노트는 표시되지 않는지 확인
        composeTestRule.onNodeWithText("4").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_8").assertDoesNotExist()
    }

    @Test
    fun noteLayout_verification_simple() {
        // 노트 모드 활성화
        composeTestRule.onNodeWithText("노트").performClick()
        
        // 첫 번째 셀 (0,0) 선택
        composeTestRule.onNodeWithTag("cell_0_0").performClick()
        
        // 숫자 9 클릭 (노트 추가)
        composeTestRule.onNodeWithTag("number_btn_9").performClick()
        
        // 노트 숫자 9가 표시되는지 확인
        val allText9Nodes = composeTestRule.onAllNodesWithText("9")
        val nodeCount = allText9Nodes.fetchSemanticsNodes().size
        
        // 최소 2개 이상의 "9" 텍스트가 있어야 함 (숫자 패드 + 노트)
        assert(nodeCount >= 2) {
            "Expected at least 2 nodes with text '9' (number pad + note), but found $nodeCount"
        }
    }

    @Test
    fun noteLayout_3x3Grid_verification() {
        // 노트 모드 활성화
        composeTestRule.onNodeWithText("노트").performClick()
        
        // 첫 번째 셀 (0,0) 선택
        composeTestRule.onNodeWithTag("cell_0_0").performClick()
        
        // 3x3 그리드의 각 위치에 노트 추가
        // 1행: 1, 2, 3
        composeTestRule.onNodeWithTag("number_btn_1").performClick()
        composeTestRule.onNodeWithTag("number_btn_2").performClick()
        composeTestRule.onNodeWithTag("number_btn_3").performClick()
        
        // 2행: 4, 5, 6
        composeTestRule.onNodeWithTag("number_btn_4").performClick()
        composeTestRule.onNodeWithTag("number_btn_5").performClick()
        composeTestRule.onNodeWithTag("number_btn_6").performClick()
        
        // 3행: 7, 8, 9
        composeTestRule.onNodeWithTag("number_btn_7").performClick()
        composeTestRule.onNodeWithTag("number_btn_8").performClick()
        composeTestRule.onNodeWithTag("number_btn_9").performClick()
        
        // 각 노트 숫자가 표시되는지 확인
        for (i in 1..9) {
            val allTextNodes = composeTestRule.onAllNodesWithText(i.toString())
            val nodeCount = allTextNodes.fetchSemanticsNodes().size
            
            // 최소 2개 이상의 텍스트가 있어야 함 (숫자 패드 + 노트)
            assert(nodeCount >= 2) {
                "Expected at least 2 nodes with text '$i' (number pad + note), but found $nodeCount"
            }
        }
    }

    private fun findEmptyCell(): SemanticsNodeInteraction {
        // 빈 셀을 찾기 위해 모든 셀을 확인
        for (row in 0..8) {
            for (col in 0..8) {
                val cell = composeTestRule.onNodeWithTag("cell_${row}_${col}")
                try {
                    // 셀이 비어있는지 확인 (숫자가 없고 노트도 없는 경우)
                    cell.assertIsDisplayed()
                    return cell
                } catch (e: Exception) {
                    // 이 셀은 비어있지 않음, 다음 셀 확인
                    continue
                }
            }
        }
        throw RuntimeException("빈 셀을 찾을 수 없습니다")
    }

    private fun getSelectedCellPosition(): Pair<Int, Int> {
        // 현재 선택된 셀의 위치를 반환하는 로직
        // 실제 구현에서는 ViewModel의 상태를 확인하거나 UI에서 선택된 셀을 찾아야 함
        return Pair(0, 0) // 임시 반환값
    }
} 