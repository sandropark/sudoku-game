package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SudokuViewModel의 게임 플로우 관련 테스트
 * - 새 게임, 보드 초기화, 게임 완료, 해답 보기 등
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SudokuViewModelGameTest {

    @Test
    fun `새 게임 생성이 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 초기 상태 저장
        val initialState = viewModel.state.first()

        // 빈 셀에 값 입력하여 상태 변경
        val emptyCell = SudokuTestHelper.findEmptyCell(initialState.board, viewModel)
        if (emptyCell != null) {
            val (row, col) = emptyCell
            viewModel.selectCell(row, col)
            viewModel.setCellValue(4)
        }

        // 새 게임 생성
        viewModel.newGame()
        val newState = viewModel.state.first()

        // 상태가 초기화되었는지 확인
        assertEquals(-1, newState.selectedRow)
        assertEquals(-1, newState.selectedCol)
        assertFalse(newState.isGameComplete)
        assertFalse(newState.showError)

        // 보드가 새로운 게임으로 변경되었는지 확인
        val hasChanges = SudokuTestHelper.hasChanges(initialState.board, newState.board)
        assertTrue(hasChanges)
    }

    @Test
    fun `게임 해답 보기가 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        viewModel.solveGame()
        val state = viewModel.state.first()

        // 모든 셀이 채워졌는지 확인
        assertTrue(state.isGameComplete)
        for (row in state.board) {
            for (cell in row) {
                assertNotEquals(0, cell)
            }
        }

        // solution과 일치하는지 확인
        val gameField = viewModel.javaClass.getDeclaredField("game")
        gameField.isAccessible = true
        val game = gameField.get(viewModel) as SudokuGame
        val solutionField = game.javaClass.getDeclaredField("solution")
        solutionField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val solution = solutionField.get(game) as Array<IntArray>

        for (i in 0..8) {
            for (j in 0..8) {
                assertEquals("해답 보기 후 보드가 solution과 일치해야 함", solution[i][j], state.board[i][j])
            }
        }
    }

    @Test
    fun `보드 초기화가 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 초기 상태 저장
        val initialState = viewModel.state.first()
        val initialBoard = SudokuTestHelper.deepCopyBoard(initialState.board)

        // 빈 셀들에 값 입력
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col)) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }

        // 일부 셀 수정
        if (emptyCells.size >= 2) {
            viewModel.selectCell(emptyCells[0].first, emptyCells[0].second)
            viewModel.setCellValue(4)
            viewModel.selectCell(emptyCells[1].first, emptyCells[1].second)
            viewModel.setCellValue(7)
        }

        // 보드 초기화
        viewModel.clearBoard()
        val clearedState = viewModel.state.first()

        // 상태가 초기화되었는지 확인
        assertEquals(-1, clearedState.selectedRow)
        assertEquals(-1, clearedState.selectedCol)
        assertFalse(clearedState.isGameComplete)
        assertFalse(clearedState.showError)

        // 보드가 초기 상태로 돌아갔는지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                assertEquals(initialBoard[row][col], clearedState.board[row][col])
            }
        }
    }

    @Test
    fun `게임 완료 플로우 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val initialState = viewModel.state.first()
        assertFalse("초기 상태는 완료되지 않아야 함", initialState.isGameComplete)

        // 게임 해결
        viewModel.solveGame()
        val solvedState = viewModel.state.first()
        assertTrue("해결된 게임은 완료되어야 함", solvedState.isGameComplete)

        // 새 게임 시작
        viewModel.newGame()
        val newGameState = viewModel.state.first()
        assertFalse("새 게임은 완료되지 않아야 함", newGameState.isGameComplete)
        assertTrue("새 게임에서는 에러가 없어야 함", newGameState.invalidCells.isEmpty())
    }

    @Test
    fun `보드 클리어 및 리셋 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val initialBoard = SudokuTestHelper.deepCopyBoard(viewModel.state.first().board)
        val emptyCell = SudokuTestHelper.findEmptyCell(initialBoard, viewModel)
        if (emptyCell == null) return@runTest

        val (row, col) = emptyCell

        // 유효한 값 찾기
        val validValue = SudokuTestHelper.findValidValue(initialBoard, row, col)
        if (validValue == null) return@runTest

        viewModel.selectCell(row, col)
        viewModel.setCellValue(validValue)
        val modifiedBoard = viewModel.state.first().board

        assertNotEquals("보드가 수정되어야 함", initialBoard[row][col], modifiedBoard[row][col])
        assertEquals("입력한 값이 저장되어야 함", validValue, modifiedBoard[row][col])

        // 보드 클리어
        viewModel.clearBoard()
        val clearedBoard = viewModel.state.first().board

        for (i in 0..8) {
            assertArrayEquals("클리어된 보드는 초기 보드와 같아야 함", initialBoard[i], clearedBoard[i])
        }

        val clearedState = viewModel.state.first()
        assertEquals("선택된 행이 초기화되어야 함", -1, clearedState.selectedRow)
        assertEquals("선택된 열이 초기화되어야 함", -1, clearedState.selectedCol)
        assertTrue("에러 상태가 초기화되어야 함", clearedState.invalidCells.isEmpty())
    }

    @Test
    fun `StateFlow 일관성 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()
        val initialBoard = viewModel.state.first().board
        val emptyCell = SudokuTestHelper.findEmptyCell(initialBoard, viewModel)
        if (emptyCell == null) return@runTest

        val (row, col) = emptyCell
        val validValue = SudokuTestHelper.findValidValue(initialBoard, row, col)
        if (validValue == null) return@runTest

        viewModel.selectCell(row, col)
        val initialState = viewModel.state.first()
        viewModel.setCellValue(validValue)
        advanceUntilIdle()

        val updatedState = viewModel.state.first()
        assertNotEquals("상태 변화가 있어야 함", initialState, updatedState)
    }

    @Test
    fun `테스트용 정답 입력 기능 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val initialState = viewModel.state.first()

        // 이미 게임이 완료된 경우가 아님을 확인
        assertFalse("초기에는 게임이 완료되지 않아야 함", initialState.isGameComplete)

        // 정답을 모두 입력
        viewModel.fillCorrectAnswers()
        val finalState = viewModel.state.first()

        // 게임이 완료되었는지 확인
        assertTrue("정답 입력 후 게임이 완료되어야 함", finalState.isGameComplete)
        assertTrue("게임 완료 다이얼로그가 표시되어야 함", finalState.showGameCompleteDialog)

        // 모든 셀이 채워졌는지 확인
        for (row in finalState.board) {
            for (cell in row) {
                assertNotEquals("모든 셀이 채워져야 함", 0, cell)
            }
        }

        // 에러 상태가 없어야 함
        assertTrue("에러 상태가 없어야 함", finalState.invalidCells.isEmpty())
        assertFalse("에러 메시지가 없어야 함", finalState.showError)

        // 노트가 초기화되어야 함
        for (row in 0..8) {
            for (col in 0..8) {
                assertTrue("노트가 초기화되어야 함", finalState.notes[row][col].isEmpty())
            }
        }
    }
}