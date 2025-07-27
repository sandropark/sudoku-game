package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SudokuViewModel의 노트 기능 관련 테스트
 * - 노트 모드 토글, 노트 추가/제거, 숫자와 노트 간 전환 등
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SudokuViewModelNoteTest {

    @Test
    fun `노트 모드 토글이 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 초기 상태 확인
        val initialState = viewModel.state.first()
        assertFalse("초기에는 노트 모드가 비활성화되어야 함", initialState.isNoteMode)

        // 노트 모드 활성화
        viewModel.toggleNoteMode()
        val afterToggle = viewModel.state.first()
        assertTrue("노트 모드가 활성화되어야 함", afterToggle.isNoteMode)

        // 노트 모드 비활성화
        viewModel.toggleNoteMode()
        val afterSecondToggle = viewModel.state.first()
        assertFalse("노트 모드가 비활성화되어야 함", afterSecondToggle.isNoteMode)
    }

    @Test
    fun `노트 숫자 추가가 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 찾기 (초기 셀이 아닌 셀)
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)

        val (row, col) = emptyCell!!

        // 노트 모드 활성화
        viewModel.toggleNoteMode()

        // 빈 셀 선택
        viewModel.selectCell(row, col)

        // 노트 숫자 추가
        viewModel.addNoteNumber(5)
        val finalState = viewModel.state.first()

        assertTrue("노트에 숫자 5가 추가되어야 함", finalState.notes[row][col].contains(5))
        assertEquals("노트에 숫자가 1개만 있어야 함", 1, finalState.notes[row][col].size)
    }

    @Test
    fun `노트 숫자 제거가 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 찾기 (초기 셀이 아닌 셀)
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)

        val (row, col) = emptyCell!!

        // 노트 모드 활성화
        viewModel.toggleNoteMode()

        // 빈 셀 선택
        viewModel.selectCell(row, col)

        // 노트 숫자 추가
        viewModel.addNoteNumber(5)
        viewModel.addNoteNumber(7)

        // 노트 숫자 제거
        viewModel.removeNoteNumber(5)
        val finalState = viewModel.state.first()

        assertFalse("노트에서 숫자 5가 제거되어야 함", finalState.notes[row][col].contains(5))
        assertTrue("노트에 숫자 7은 남아있어야 함", finalState.notes[row][col].contains(7))
        assertEquals("노트에 숫자가 1개만 있어야 함", 1, finalState.notes[row][col].size)
    }

    @Test
    fun `초기 셀에 숫자 입력 시도 시 변경되지 않는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 초기 셀 찾기
        val state = viewModel.state.first()
        var initialRow = -1
        var initialCol = -1
        var initialValue = 0
        for (row in 0..8) {
            for (col in 0..8) {
                if (viewModel.isInitialCell(row, col)) {
                    initialRow = row
                    initialCol = col
                    initialValue = state.board[row][col]
                    break
                }
            }
            if (initialRow != -1) break
        }

        assertTrue("초기 셀이 존재해야 함", initialRow != -1)

        // 초기 셀 선택 후 다른 숫자 입력 시도
        viewModel.selectCell(initialRow, initialCol)
        val newValue = if (initialValue == 1) 2 else 1
        viewModel.setCellValue(newValue)

        val finalState = viewModel.state.first()
        // 요구사항: 초기 셀은 변경할 수 없음
        assertEquals("초기 셀 값이 변경되지 않아야 함", initialValue, finalState.board[initialRow][initialCol])
        // 에러도 발생하지 않아야 함 (단순히 무시됨)
        assertFalse("에러가 발생하지 않아야 함", finalState.showError)
        // 초기 셀 상태도 유지되어야 함
        assertTrue("초기 셀 상태가 유지되어야 함", viewModel.isInitialCell(initialRow, initialCol))
    }

    @Test
    fun `노트 모드에서 같은 숫자를 다시 클릭하면 제거되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 찾기 (초기 셀이 아닌 셀)
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)

        val (row, col) = emptyCell!!

        // 노트 모드 활성화
        viewModel.toggleNoteMode()

        // 빈 셀 선택
        viewModel.selectCell(row, col)

        // 노트 숫자 추가
        viewModel.addNoteNumber(5)
        val afterAdd = viewModel.state.first()
        assertTrue("노트에 숫자 5가 추가되어야 함", afterAdd.notes[row][col].contains(5))

        // 같은 숫자 다시 클릭 (제거)
        viewModel.addNoteNumber(5)
        val afterRemove = viewModel.state.first()
        assertFalse("노트에서 숫자 5가 제거되어야 함", afterRemove.notes[row][col].contains(5))
        assertEquals("노트가 비어있어야 함", 0, afterRemove.notes[row][col].size)
    }

    @Test
    fun `노트 모드가 비활성화된 상태에서 노트 숫자 추가가 무시되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 찾기 (초기 셀이 아닌 셀)
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)

        val (row, col) = emptyCell!!

        // 노트 모드 비활성화 상태에서 셀 선택
        viewModel.selectCell(row, col)

        // 노트 숫자 추가 시도
        viewModel.addNoteNumber(5)
        val finalState = viewModel.state.first()

        assertFalse("노트 모드가 비활성화된 상태에서는 노트가 추가되지 않아야 함", finalState.notes[row][col].contains(5))
        assertEquals("노트가 비어있어야 함", 0, finalState.notes[row][col].size)
    }

    @Test
    fun `초기 셀은 숫자와 노트 모두 변경 불가`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        viewModel.newGame()
        val state = viewModel.state.first()

        // 초기 셀 찾기
        var initialRow = -1
        var initialCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (viewModel.isInitialCell(row, col)) {
                    initialRow = row
                    initialCol = col
                    break
                }
            }
            if (initialRow != -1) break
        }
        assertTrue("초기 셀을 찾을 수 있어야 함", initialRow != -1)

        // 초기 셀 값 저장
        val originalValue = state.board[initialRow][initialCol]

        // 숫자 입력 시도
        viewModel.selectCell(initialRow, initialCol)
        viewModel.setCellValue(5)
        val afterValue = viewModel.state.first().board[initialRow][initialCol]
        assertEquals("초기 셀 값은 변경되면 안 됨", originalValue, afterValue)

        // 노트 입력 시도
        viewModel.toggleNoteMode()
        viewModel.addNoteNumber(7)
        val notes = viewModel.state.first().notes[initialRow][initialCol]
        assertTrue("초기 셀에는 노트가 추가되면 안 됨", notes.isEmpty())
    }

    @Test
    fun `노트가 있는 상태에서 숫자 입력 시 노트가 사라지고 숫자가 입력되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 찾기
        val emptyCell = SudokuTestHelper.findEmptyCell(viewModel.state.first().board, viewModel)
        assertTrue("빈 셀이 존재해야 함", emptyCell != null)
        val (row, col) = emptyCell!!

        // 셀 선택
        viewModel.selectCell(row, col)

        // 노트 모드 활성화
        viewModel.toggleNoteMode()

        // 노트 추가 (1, 2, 3)
        viewModel.addNoteNumber(1)
        viewModel.addNoteNumber(2)
        viewModel.addNoteNumber(3)

        // 노트가 추가되었는지 확인
        val stateWithNotes = viewModel.state.first()
        assertTrue("노트가 추가되어야 함", stateWithNotes.notes[row][col].containsAll(setOf(1, 2, 3)))
        assertEquals("일반 숫자는 0이어야 함", 0, stateWithNotes.board[row][col])

        // 노트 모드 비활성화
        viewModel.toggleNoteMode()

        // 숫자 5 입력
        viewModel.setCellValue(5)

        // 노트가 사라지고 숫자가 입력되었는지 확인
        val stateAfterInput = viewModel.state.first()
        assertTrue("노트가 사라져야 함", stateAfterInput.notes[row][col].isEmpty())
        assertEquals("숫자가 입력되어야 함", 5, stateAfterInput.board[row][col])
    }

    @Test
    fun `숫자가 있는 상태에서 노트 입력 시 숫자가 사라지고 노트가 입력되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 찾기
        val emptyCell = SudokuTestHelper.findEmptyCell(viewModel.state.first().board, viewModel)
        assertTrue("빈 셀이 존재해야 함", emptyCell != null)
        val (row, col) = emptyCell!!

        // 셀 선택
        viewModel.selectCell(row, col)

        // 숫자 5 입력
        viewModel.setCellValue(5)

        // 숫자가 입력되었는지 확인
        val stateWithNumber = viewModel.state.first()
        assertEquals("숫자가 입력되어야 함", 5, stateWithNumber.board[row][col])

        // 노트 모드 활성화
        viewModel.toggleNoteMode()

        // 노트 추가 (1, 2, 3)
        viewModel.addNoteNumber(1)
        viewModel.addNoteNumber(2)
        viewModel.addNoteNumber(3)

        // 숫자가 사라지고 노트가 입력되었는지 확인
        val stateWithNotes = viewModel.state.first()
        assertTrue("노트가 추가되어야 함", stateWithNotes.notes[row][col].containsAll(setOf(1, 2, 3)))
        assertEquals("일반 숫자는 0이어야 함", 0, stateWithNotes.board[row][col])
    }
}