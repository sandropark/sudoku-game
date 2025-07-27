package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SudokuViewModel의 Undo/Redo 기능 관련 테스트
 * - 단일/다중 되돌리기, 상태 복원 등
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SudokuViewModelUndoTest {

    @Test
    fun `단일 Undo 기능 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val initialState = viewModel.state.value
        val emptyCell = SudokuTestHelper.findEmptyCell(initialState.board, viewModel)
        assertTrue("빈 셀이 존재해야 함", emptyCell != null)

        val (row, col) = emptyCell!!
        val validValue = SudokuTestHelper.findValidValue(initialState.board, row, col)
        assertTrue("유효한 값이 존재해야 함", validValue != null)

        // 값 설정
        viewModel.selectCell(row, col)
        viewModel.setCellValue(validValue!!)
        advanceUntilIdle()

        val modifiedState = viewModel.state.value
        assertEquals("값이 설정되어야 함", validValue, modifiedState.board[row][col])

        // Undo 실행
        viewModel.onUndo()
        advanceUntilIdle()

        val undoState = viewModel.state.value
        assertEquals("Undo 후 값이 복원되어야 함", 0, undoState.board[row][col])
        assertTrue("Undo 후 에러 상태가 없어야 함", undoState.invalidCells.isEmpty())
    }

    @Test
    fun `다중 Undo 기능 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value

        // 첫 번째 빈 셀 찾기
        var emptyRow1 = -1
        var emptyCol1 = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow1 = row
                    emptyCol1 = col
                    break
                }
            }
            if (emptyRow1 != -1) break
        }
        assertTrue(emptyRow1 != -1)

        // 첫 번째 값 설정
        viewModel.selectCell(emptyRow1, emptyCol1)
        val validValue1 = SudokuTestHelper.findValidValue(state.board, emptyRow1, emptyCol1)
        assertNotNull("유효한 값이 존재해야 함", validValue1)
        viewModel.setCellValue(validValue1!!)
        advanceUntilIdle()

        val v1 = viewModel.state.value.board[emptyRow1][emptyCol1]
        assertEquals(validValue1, v1)

        // 두 번째 빈 셀 찾기
        var emptyRow2 = -1
        var emptyCol2 = -1
        for (row in 0..8) {
            for (col in 0..8) {
                val currentState = viewModel.state.value
                if ((row != emptyRow1 || col != emptyCol1) &&
                    !viewModel.isInitialCell(row, col) &&
                    currentState.board[row][col] == 0
                ) {
                    emptyRow2 = row
                    emptyCol2 = col
                    break
                }
            }
            if (emptyRow2 != -1) break
        }
        assertTrue(emptyRow2 != -1)
        assertTrue("두 셀은 다른 위치여야 함", !(emptyRow1 == emptyRow2 && emptyCol1 == emptyCol2))

        // 두 번째 값 설정
        viewModel.selectCell(emptyRow2, emptyCol2)
        advanceUntilIdle()

        val validValue2 =
            SudokuTestHelper.findValidValue(viewModel.state.value.board, emptyRow2, emptyCol2)
        assertNotNull("두 번째 유효한 값이 존재해야 함", validValue2)
        viewModel.setCellValue(validValue2!!)
        advanceUntilIdle()

        val v2_1 = viewModel.state.value.board[emptyRow1][emptyCol1]
        val v2_2 = viewModel.state.value.board[emptyRow2][emptyCol2]
        assertEquals(validValue1, v2_1)
        assertEquals(validValue2, v2_2)

        // 첫 번째 Undo (두 번째 값만 제거)
        viewModel.onUndo()
        advanceUntilIdle()

        val v3_1 = viewModel.state.value.board[emptyRow1][emptyCol1]
        val v3_2 = viewModel.state.value.board[emptyRow2][emptyCol2]
        assertEquals(validValue1, v3_1)
        assertEquals(0, v3_2)
        assertTrue(viewModel.state.value.invalidCells.isEmpty())

        // 두 번째 Undo (첫 번째 값도 제거)
        viewModel.onUndo()
        advanceUntilIdle()

        val v4_1 = viewModel.state.value.board[emptyRow1][emptyCol1]
        assertEquals(0, v4_1)
        assertTrue(viewModel.state.value.invalidCells.isEmpty())
    }

    @Test
    fun `Undo 스택 크기 제한 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        if (emptyCell == null) return@runTest

        val (row, col) = emptyCell

        // 51번 값 변경 (스택 크기는 50으로 제한)
        repeat(51) { i ->
            viewModel.selectCell(row, col)
            val value = (i % 9) + 1
            viewModel.setCellValue(value)
            advanceUntilIdle()
        }

        // 최대 50번만 되돌릴 수 있어야 함
        var undoCount = 0
        repeat(60) { // 60번 시도
            val beforeUndo = viewModel.state.value.board[row][col]
            viewModel.onUndo()
            advanceUntilIdle()
            val afterUndo = viewModel.state.value.board[row][col]

            if (beforeUndo != afterUndo) {
                undoCount++
            }
        }

        assertTrue("Undo 횟수가 50번을 초과하지 않아야 함", undoCount <= 50)
    }

    @Test
    fun `Undo 시 선택 상태 복원 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        if (emptyCell == null) return@runTest

        val (row, col) = emptyCell
        val validValue = SudokuTestHelper.findValidValue(state.board, row, col)
        if (validValue == null) return@runTest

        // 셀 선택 및 값 설정
        viewModel.selectCell(row, col)
        viewModel.setCellValue(validValue)
        advanceUntilIdle()

        // 다른 셀 선택
        viewModel.selectCell(0, 0)
        advanceUntilIdle()

        // Undo 실행
        viewModel.onUndo()
        advanceUntilIdle()

        val undoState = viewModel.state.value
        // 원래 선택했던 셀로 선택 상태가 복원되어야 함
        assertEquals("Undo 후 선택된 행이 복원되어야 함", row, undoState.selectedRow)
        assertEquals("Undo 후 선택된 열이 복원되어야 함", col, undoState.selectedCol)
        assertEquals("Undo 후 값이 복원되어야 함", 0, undoState.board[row][col])
    }

    @Test
    fun `새 게임 시 Undo 스택 초기화 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        if (emptyCell == null) return@runTest

        val (row, col) = emptyCell
        val validValue = SudokuTestHelper.findValidValue(state.board, row, col)
        if (validValue == null) return@runTest

        // 값 설정 후 새 게임 시작
        viewModel.selectCell(row, col)
        viewModel.setCellValue(validValue)
        advanceUntilIdle()

        viewModel.newGame()
        advanceUntilIdle()

        // Undo 시도 (새 게임 후이므로 아무 변화 없어야 함)
        val beforeUndo = SudokuTestHelper.deepCopyBoard(viewModel.state.value.board)
        viewModel.onUndo()
        advanceUntilIdle()
        val afterUndo = viewModel.state.value.board

        assertTrue(
            "새 게임 후 Undo 스택이 초기화되어야 함",
            SudokuTestHelper.boardsEqual(beforeUndo, afterUndo)
        )
    }

    @Test
    fun `노트 변경도 Undo로 복원되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        if (emptyCell == null) return@runTest

        val (row, col) = emptyCell

        // 노트 모드 활성화
        viewModel.toggleNoteMode()

        // 셀 선택 후 노트 추가
        viewModel.selectCell(row, col)
        viewModel.addNoteNumber(5)
        advanceUntilIdle()

        val afterNote = viewModel.state.value
        assertTrue("노트가 추가되어야 함", afterNote.notes[row][col].contains(5))

        // Undo 실행
        viewModel.onUndo()
        advanceUntilIdle()

        val afterUndo = viewModel.state.value
        assertTrue("Undo 후 노트가 제거되어야 함", afterUndo.notes[row][col].isEmpty())
    }
}