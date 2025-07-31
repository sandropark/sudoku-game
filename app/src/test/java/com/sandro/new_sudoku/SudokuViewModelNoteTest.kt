package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
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

    @Test
    fun `숫자 입력 시 같은 행의 노트에서 해당 숫자가 제거되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 2개 찾기 (같은 행)
        val state = viewModel.state.first()
        var targetRow = -1
        var inputCol = -1
        var noteCol = -1

        // 같은 행에서 빈 셀 2개 찾기
        for (row in 0..8) {
            val emptyCells = mutableListOf<Int>()
            for (col in 0..8) {
                if (state.board[row][col] == 0 && !viewModel.isInitialCell(row, col)) {
                    emptyCells.add(col)
                }
            }
            if (emptyCells.size >= 2) {
                targetRow = row
                inputCol = emptyCells[0]
                noteCol = emptyCells[1]
                break
            }
        }

        assertTrue("같은 행에 빈 셀이 2개 이상 있어야 함", targetRow != -1)

        // noteCol에 노트 추가
        viewModel.selectCell(targetRow, noteCol)
        viewModel.toggleNoteMode()
        viewModel.addNoteNumber(5)
        viewModel.addNoteNumber(7)

        // 노트가 추가되었는지 확인
        val stateWithNotes = viewModel.state.first()
        assertTrue(
            "노트에 5와 7이 추가되어야 함",
            stateWithNotes.notes[targetRow][noteCol].containsAll(setOf(5, 7))
        )

        // inputCol에 숫자 5 입력
        viewModel.toggleNoteMode() // 노트 모드 해제
        viewModel.selectCell(targetRow, inputCol)
        viewModel.setCellValue(5)

        // 같은 행의 다른 셀의 노트에서 5가 제거되었는지 확인
        val finalState = viewModel.state.first()
        assertFalse(
            "같은 행의 노트에서 5가 제거되어야 함",
            finalState.notes[targetRow][noteCol].contains(5)
        )
        assertTrue(
            "같은 행의 노트에서 7은 남아있어야 함",
            finalState.notes[targetRow][noteCol].contains(7)
        )
    }

    @Test
    fun `숫자 입력 시 같은 열의 노트에서 해당 숫자가 제거되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 2개 찾기 (같은 열)
        val state = viewModel.state.first()
        var targetCol = -1
        var inputRow = -1
        var noteRow = -1

        // 같은 열에서 빈 셀 2개 찾기
        for (col in 0..8) {
            val emptyCells = mutableListOf<Int>()
            for (row in 0..8) {
                if (state.board[row][col] == 0 && !viewModel.isInitialCell(row, col)) {
                    emptyCells.add(row)
                }
            }
            if (emptyCells.size >= 2) {
                targetCol = col
                inputRow = emptyCells[0]
                noteRow = emptyCells[1]
                break
            }
        }

        assertTrue("같은 열에 빈 셀이 2개 이상 있어야 함", targetCol != -1)

        // noteRow에 노트 추가
        viewModel.selectCell(noteRow, targetCol)
        viewModel.toggleNoteMode()
        viewModel.addNoteNumber(3)
        viewModel.addNoteNumber(8)

        // 노트가 추가되었는지 확인
        val stateWithNotes = viewModel.state.first()
        assertTrue(
            "노트에 3과 8이 추가되어야 함",
            stateWithNotes.notes[noteRow][targetCol].containsAll(setOf(3, 8))
        )

        // inputRow에 숫자 3 입력
        viewModel.toggleNoteMode() // 노트 모드 해제
        viewModel.selectCell(inputRow, targetCol)
        viewModel.setCellValue(3)

        // 같은 열의 다른 셀의 노트에서 3이 제거되었는지 확인
        val finalState = viewModel.state.first()
        assertFalse(
            "같은 열의 노트에서 3이 제거되어야 함",
            finalState.notes[noteRow][targetCol].contains(3)
        )
        assertTrue(
            "같은 열의 노트에서 8은 남아있어야 함",
            finalState.notes[noteRow][targetCol].contains(8)
        )
    }

    @Test
    fun `숫자 입력 시 같은 3x3 박스의 노트에서 해당 숫자가 제거되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 2개 찾기 (같은 3x3 박스)
        val state = viewModel.state.first()
        var inputRow = -1
        var inputCol = -1
        var noteRow = -1
        var noteCol = -1

        // 좌상단 3x3 박스에서 빈 셀 2개 찾기
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..2) {
            for (col in 0..2) {
                if (state.board[row][col] == 0 && !viewModel.isInitialCell(row, col)) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }

        assertTrue("같은 3x3 박스에 빈 셀이 2개 이상 있어야 함", emptyCells.size >= 2)

        inputRow = emptyCells[0].first
        inputCol = emptyCells[0].second
        noteRow = emptyCells[1].first
        noteCol = emptyCells[1].second

        // noteRow, noteCol에 노트 추가
        viewModel.selectCell(noteRow, noteCol)
        viewModel.toggleNoteMode()
        viewModel.addNoteNumber(6)
        viewModel.addNoteNumber(9)

        // 노트가 추가되었는지 확인
        val stateWithNotes = viewModel.state.first()
        assertTrue(
            "노트에 6과 9가 추가되어야 함",
            stateWithNotes.notes[noteRow][noteCol].containsAll(setOf(6, 9))
        )

        // inputRow, inputCol에 숫자 6 입력
        viewModel.toggleNoteMode() // 노트 모드 해제
        viewModel.selectCell(inputRow, inputCol)
        viewModel.setCellValue(6)

        // 같은 3x3 박스의 다른 셀의 노트에서 6이 제거되었는지 확인
        val finalState = viewModel.state.first()
        assertFalse(
            "같은 3x3 박스의 노트에서 6이 제거되어야 함",
            finalState.notes[noteRow][noteCol].contains(6)
        )
        assertTrue(
            "같은 3x3 박스의 노트에서 9는 남아있어야 함",
            finalState.notes[noteRow][noteCol].contains(9)
        )
    }

    @Test
    fun `숫자 입력 시 복합적인 관련 셀들의 노트에서 해당 숫자가 제거되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val state = viewModel.state.first()

        // 더 안정적인 테스트를 위해 실제로 빈 셀들을 찾아서 테스트
        var inputRow = -1
        var inputCol = -1
        var sameRowCell: Pair<Int, Int>? = null
        var sameColCell: Pair<Int, Int>? = null
        var sameBoxCell: Pair<Int, Int>? = null

        // 입력할 빈 셀 찾기
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] == 0 && !viewModel.isInitialCell(row, col)) {
                    inputRow = row
                    inputCol = col
                    break
                }
            }
            if (inputRow != -1) break
        }

        assertTrue("테스트를 위한 빈 셀이 있어야 함", inputRow != -1)

        // 같은 행의 다른 빈 셀 찾기
        for (col in 0..8) {
            if (col != inputCol && state.board[inputRow][col] == 0 && !viewModel.isInitialCell(
                    inputRow,
                    col
                )
            ) {
                sameRowCell = Pair(inputRow, col)
                break
            }
        }

        // 같은 열의 다른 빈 셀 찾기
        for (row in 0..8) {
            if (row != inputRow && state.board[row][inputCol] == 0 && !viewModel.isInitialCell(
                    row,
                    inputCol
                )
            ) {
                sameColCell = Pair(row, inputCol)
                break
            }
        }

        // 같은 3x3 박스의 다른 빈 셀 찾기
        val boxRow = (inputRow / 3) * 3
        val boxCol = (inputCol / 3) * 3
        for (row in boxRow until boxRow + 3) {
            for (col in boxCol until boxCol + 3) {
                if ((row != inputRow || col != inputCol) &&
                    state.board[row][col] == 0 &&
                    !viewModel.isInitialCell(row, col)
                ) {
                    sameBoxCell = Pair(row, col)
                    break
                }
            }
            if (sameBoxCell != null) break
        }

        val testNumber = 4

        // 같은 행의 셀에 노트 추가
        sameRowCell?.let { (row, col) ->
            viewModel.selectCell(row, col)
            viewModel.toggleNoteMode()
            viewModel.addNoteNumber(testNumber)
            viewModel.addNoteNumber(7)
            advanceUntilIdle()
        }

        // 같은 열의 셀에 노트 추가  
        sameColCell?.let { (row, col) ->
            viewModel.selectCell(row, col)
            if (!viewModel.state.first().isNoteMode) viewModel.toggleNoteMode()
            viewModel.addNoteNumber(testNumber)
            viewModel.addNoteNumber(8)
            advanceUntilIdle()
        }

        // 같은 3x3 박스의 셀에 노트 추가
        sameBoxCell?.let { (row, col) ->
            viewModel.selectCell(row, col)
            if (!viewModel.state.first().isNoteMode) viewModel.toggleNoteMode()
            viewModel.addNoteNumber(testNumber)
            viewModel.addNoteNumber(9)
            advanceUntilIdle()
        }

        // 노트 모드 해제 후 입력 셀에 숫자 입력
        if (viewModel.state.first().isNoteMode) viewModel.toggleNoteMode()
        viewModel.selectCell(inputRow, inputCol)
        viewModel.setCellValue(testNumber)
        advanceUntilIdle()

        // 관련된 모든 셀의 노트에서 해당 숫자가 제거되었는지 확인
        val finalState = viewModel.state.first()

        // 같은 행 셀 검증
        sameRowCell?.let { (row, col) ->
            assertFalse(
                "같은 행의 노트에서 숫자가 제거되어야 함",
                finalState.notes[row][col].contains(testNumber)
            )
            assertTrue(
                "다른 노트는 남아있어야 함",
                finalState.notes[row][col].contains(7)
            )
        }

        // 같은 열 셀 검증
        sameColCell?.let { (row, col) ->
            assertFalse(
                "같은 열의 노트에서 숫자가 제거되어야 함",
                finalState.notes[row][col].contains(testNumber)
            )
            assertTrue(
                "다른 노트는 남아있어야 함",
                finalState.notes[row][col].contains(8)
            )
        }

        // 같은 3x3 박스 셀 검증
        sameBoxCell?.let { (row, col) ->
            assertFalse(
                "같은 3x3 박스의 노트에서 숫자가 제거되어야 함",
                finalState.notes[row][col].contains(testNumber)
            )
            assertTrue(
                "다른 노트는 남아있어야 함",
                finalState.notes[row][col].contains(9)
            )
        }

        // 최소한 하나의 검증은 수행되어야 함
        assertTrue(
            "최소한 하나의 관련 셀이 있어야 함",
            sameRowCell != null || sameColCell != null || sameBoxCell != null
        )
    }

    @Test
    fun `숫자를 0으로 지울 때는 노트 제거가 발생하지 않는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 2개 찾기 (같은 행)
        val state = viewModel.state.first()
        var targetRow = -1
        var inputCol = -1
        var noteCol = -1

        for (row in 0..8) {
            val emptyCells = mutableListOf<Int>()
            for (col in 0..8) {
                if (state.board[row][col] == 0 && !viewModel.isInitialCell(row, col)) {
                    emptyCells.add(col)
                }
            }
            if (emptyCells.size >= 2) {
                targetRow = row
                inputCol = emptyCells[0]
                noteCol = emptyCells[1]
                break
            }
        }

        assertTrue("같은 행에 빈 셀이 2개 이상 있어야 함", targetRow != -1)

        // inputCol에 숫자 5 입력
        viewModel.selectCell(targetRow, inputCol)
        viewModel.setCellValue(5)

        // noteCol에 노트 추가 (5 포함)
        viewModel.selectCell(targetRow, noteCol)
        viewModel.toggleNoteMode()
        viewModel.addNoteNumber(5)
        viewModel.addNoteNumber(7)

        // 노트가 추가되었는지 확인
        val stateWithNotes = viewModel.state.first()
        assertTrue(
            "노트에 5와 7이 추가되어야 함",
            stateWithNotes.notes[targetRow][noteCol].containsAll(setOf(5, 7))
        )

        // inputCol의 숫자를 지움 (0으로 설정)
        viewModel.toggleNoteMode() // 노트 모드 해제
        viewModel.selectCell(targetRow, inputCol)
        viewModel.clearCell() // 또는 setCellValue(0)

        // 노트에서 5가 제거되지 않았는지 확인 (숫자를 지울 때는 노트 제거 안 함)
        val finalState = viewModel.state.first()
        assertTrue(
            "숫자를 지울 때는 노트가 제거되지 않아야 함",
            finalState.notes[targetRow][noteCol].contains(5)
        )
        assertTrue(
            "다른 노트도 남아있어야 함",
            finalState.notes[targetRow][noteCol].contains(7)
        )
    }

    @Test
    fun `숫자가 입력된 셀에 노트를 입력하면 하이라이트가 초기화되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 찾기
        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)
        val (row, col) = emptyCell!!

        // 셀 선택 후 숫자 5 입력
        viewModel.selectCell(row, col)
        viewModel.setCellValue(5)

        // 숫자 입력 후 하이라이트 상태 확인
        val stateAfterNumber = viewModel.state.first()
        assertEquals("하이라이트된 숫자가 5여야 함", 5, stateAfterNumber.highlightedNumber)
        assertTrue("5가 있는 셀들이 하이라이트되어야 함", stateAfterNumber.highlightedCells.isNotEmpty())

        // 노트 모드 활성화 후 노트 추가
        viewModel.toggleNoteMode()
        viewModel.addNoteNumber(1)

        // 노트 입력 후 하이라이트가 초기화되었는지 확인
        val stateAfterNote = viewModel.state.first()
        assertEquals("숫자가 0으로 변경되어야 함", 0, stateAfterNote.board[row][col])
        assertTrue("노트가 추가되어야 함", stateAfterNote.notes[row][col].contains(1))

        // 하이라이트가 초기화되었는지 확인 (현재는 실패할 것임)
        assertEquals("하이라이트된 숫자가 0으로 초기화되어야 함", 0, stateAfterNote.highlightedNumber)
        assertTrue("하이라이트된 셀들이 비어있어야 함", stateAfterNote.highlightedCells.isEmpty())
    }
}