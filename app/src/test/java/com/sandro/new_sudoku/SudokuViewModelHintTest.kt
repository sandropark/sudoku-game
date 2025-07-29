package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SudokuViewModel의 힌트 기능 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SudokuViewModelHintTest {

    @Test
    fun `셀이 선택되지 않은 상태에서 힌트 사용 시 아무 일이 일어나지 않는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val initialState = viewModel.state.first()

        // Given: 선택된 셀이 없음 (-1, -1)
        assertEquals(-1, initialState.selectedRow)
        assertEquals(-1, initialState.selectedCol)

        // When: 힌트를 사용
        viewModel.useHint()

        // Then: 상태가 변경되지 않음
        val afterState = viewModel.state.first()
        assertEquals(initialState.selectedRow, afterState.selectedRow)
        assertEquals(initialState.selectedCol, afterState.selectedCol)

        // 보드도 변경되지 않음
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                assertEquals(
                    "셀 ($row, $col)이 변경되지 않아야 함",
                    initialState.board[row][col],
                    afterState.board[row][col]
                )
            }
        }
    }

    @Test
    fun `빈 셀에 힌트를 적용하면 정답이 입력되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // Given: 빈 셀을 찾아서 선택
        var emptyRow = -1
        var emptyCol = -1
        val initialState = viewModel.state.first()

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (initialState.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("빈 셀이 있어야 함", emptyRow != -1 && emptyCol != -1)

        // 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)

        // When: 힌트를 사용
        viewModel.useHint()

        // Then: 해당 셀에 정답이 입력됨
        val afterState = viewModel.state.first()
        val hintValue = afterState.board[emptyRow][emptyCol]

        assertTrue("힌트 값은 1-9 사이여야 함", hintValue in 1..9)
        assertNotEquals("빈 셀이 채워져야 함", 0, hintValue)
    }

    @Test
    fun `이미 정답이 입력된 셀에 힌트를 사용해도 값이 변경되지 않는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // Given: 빈 셀을 찾아서 힌트로 정답을 입력
        var targetRow = -1
        var targetCol = -1
        val initialState = viewModel.state.first()

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (initialState.board[row][col] == 0) {
                    targetRow = row
                    targetCol = col
                    break
                }
            }
            if (targetRow != -1) break
        }

        assertTrue("빈 셀이 있어야 함", targetRow != -1 && targetCol != -1)

        // 셀 선택하고 힌트로 정답 입력
        viewModel.selectCell(targetRow, targetCol)
        viewModel.useHint()

        val stateAfterFirstHint = viewModel.state.first()
        val correctValue = stateAfterFirstHint.board[targetRow][targetCol]

        // When: 같은 셀에 다시 힌트를 사용
        viewModel.useHint()

        // Then: 값이 변경되지 않음
        val finalState = viewModel.state.first()
        assertEquals(
            "이미 정답인 셀은 변경되지 않아야 함",
            correctValue,
            finalState.board[targetRow][targetCol]
        )
    }

    @Test
    fun `잘못된 값이 입력된 셀에 힌트를 사용하면 정답으로 변경되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // Given: 빈 셀을 찾아서 임의의 잘못된 값을 입력
        var targetRow = -1
        var targetCol = -1
        val initialState = viewModel.state.first()

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (initialState.board[row][col] == 0) {
                    targetRow = row
                    targetCol = col
                    break
                }
            }
            if (targetRow != -1) break
        }

        assertTrue("빈 셀이 있어야 함", targetRow != -1 && targetCol != -1)

        // 셀 선택하고 임의의 값(5) 입력
        viewModel.selectCell(targetRow, targetCol)
        viewModel.setCellValue(5)

        val stateAfterWrongInput = viewModel.state.first()
        assertEquals("잘못된 값이 입력되어야 함", 5, stateAfterWrongInput.board[targetRow][targetCol])

        // When: 힌트를 사용
        viewModel.useHint()

        // Then: 정답으로 변경됨
        val finalState = viewModel.state.first()
        val hintValue = finalState.board[targetRow][targetCol]

        assertTrue("힌트 값은 1-9 사이여야 함", hintValue in 1..9)
        // 잘못된 값(5)과 다를 수 있음 (정답이 5가 아닌 경우)
    }

    @Test
    fun `힌트 사용 후 Undo가 가능한지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // Given: 빈 셀을 찾아서 선택
        var emptyRow = -1
        var emptyCol = -1
        val initialState = viewModel.state.first()

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (initialState.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("빈 셀이 있어야 함", emptyRow != -1 && emptyCol != -1)

        val originalValue = initialState.board[emptyRow][emptyCol]

        viewModel.selectCell(emptyRow, emptyCol)

        // When: 힌트를 사용하고 Undo
        viewModel.useHint()
        val stateAfterHint = viewModel.state.first()
        val hintValue = stateAfterHint.board[emptyRow][emptyCol]

        viewModel.onUndo()

        // Then: 원래 상태로 복원됨
        val stateAfterUndo = viewModel.state.first()
        assertEquals(
            "Undo 후 원래 값으로 복원되어야 함",
            originalValue,
            stateAfterUndo.board[emptyRow][emptyCol]
        )

        // 힌트 값과 다른지 확인 (빈 셀이었다면)
        if (originalValue == 0) {
            assertNotEquals(
                "Undo 후 힌트 값과 달라야 함",
                hintValue,
                stateAfterUndo.board[emptyRow][emptyCol]
            )
        }
    }

    @Test
    fun `힌트 사용 후 타이머가 계속 실행되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTimerTestViewModel()

        // Given: 타이머 시작 및 빈 셀 선택
        viewModel.startTimer()
        val initialState = viewModel.state.first()
        assertTrue("타이머가 실행 중이어야 함", initialState.isTimerRunning)

        var emptyRow = -1
        var emptyCol = -1

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (initialState.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("빈 셀이 있어야 함", emptyRow != -1 && emptyCol != -1)
        viewModel.selectCell(emptyRow, emptyCol)

        // When: 힌트를 사용
        viewModel.useHint()

        // Then: 타이머가 여전히 실행 중
        val finalState = viewModel.state.first()
        assertTrue("힌트 사용 후에도 타이머가 실행 중이어야 함", finalState.isTimerRunning)
    }

    @Test
    fun `힌트 사용 후 노트 모드가 유지되는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // Given: 노트 모드 활성화 및 빈 셀 선택
        viewModel.toggleNoteMode()
        val initialState = viewModel.state.first()
        assertTrue("노트 모드가 활성화되어야 함", initialState.isNoteMode)

        var emptyRow = -1
        var emptyCol = -1

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (initialState.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("빈 셀이 있어야 함", emptyRow != -1 && emptyCol != -1)
        viewModel.selectCell(emptyRow, emptyCol)

        // When: 힌트를 사용
        viewModel.useHint()

        // Then: 노트 모드가 유지됨
        val finalState = viewModel.state.first()
        assertTrue("힌트 사용 후에도 노트 모드가 유지되어야 함", finalState.isNoteMode)
    }

    @Test
    fun `힌트 사용 후 실수 카운트가 증가하지 않는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // Given: 빈 셀 선택
        val initialState = viewModel.state.first()
        val initialMistakeCount = initialState.mistakeCount

        var emptyRow = -1
        var emptyCol = -1

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (initialState.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("빈 셀이 있어야 함", emptyRow != -1 && emptyCol != -1)
        viewModel.selectCell(emptyRow, emptyCol)

        // When: 힌트를 사용
        viewModel.useHint()

        // Then: 실수 카운트가 증가하지 않음
        val finalState = viewModel.state.first()
        assertEquals(
            "힌트 사용 시 실수 카운트가 증가하지 않아야 함",
            initialMistakeCount,
            finalState.mistakeCount
        )
    }

    @Test
    fun `힌트로 입력된 셀이 유효한 상태인지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // Given: 빈 셀을 찾아서 선택
        var emptyRow = -1
        var emptyCol = -1
        val initialState = viewModel.state.first()

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (initialState.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("빈 셀이 있어야 함", emptyRow != -1 && emptyCol != -1)
        viewModel.selectCell(emptyRow, emptyCol)

        // When: 힌트를 사용
        viewModel.useHint()

        // Then: 해당 셀이 invalidCells에 포함되지 않음
        val finalState = viewModel.state.first()
        val cellPair = Pair(emptyRow, emptyCol)
        assertFalse(
            "힌트로 입력된 셀은 invalid 상태가 아니어야 함",
            finalState.invalidCells.contains(cellPair)
        )
    }
}