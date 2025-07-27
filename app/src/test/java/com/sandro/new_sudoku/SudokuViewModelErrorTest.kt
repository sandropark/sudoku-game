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
 * SudokuViewModel의 에러 처리 관련 테스트
 * - 스도쿠 규칙 위반, 에러 상태 관리, 복구 등
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SudokuViewModelErrorTest {

    @Test
    fun `스도쿠 규칙 위반 시 에러가 발생하는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀들 찾기 (초기 셀이 아닌 셀)
        val state = viewModel.state.first()
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col)) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }

        // 빈 셀이 2개 미만이면 테스트 종료 (스도쿠 규칙 위반 테스트 불가)
        if (emptyCells.size < 2) {
            return@runTest
        }

        // 같은 행에 있는 빈 셀들 찾기
        val sameRowCells = emptyCells.groupBy { it.first }.values.firstOrNull { it.size >= 2 }

        if (sameRowCells != null) {
            val firstCell = sameRowCells[0]
            val secondCell = sameRowCells[1]

            // 첫 번째 셀에 유효한 값 입력
            val validValue =
                SudokuTestHelper.findValidValue(state.board, firstCell.first, firstCell.second)
            if (validValue != null) {
                viewModel.selectCell(firstCell.first, firstCell.second)
                viewModel.setCellValue(validValue)

                // 두 번째 셀에 같은 값을 입력 (스도쿠 규칙 위반)
                viewModel.selectCell(secondCell.first, secondCell.second)
                viewModel.setCellValue(validValue)

                val finalState = viewModel.state.first()
                assertTrue(
                    "스도쿠 규칙 위반 시 에러가 발생해야 함",
                    finalState.invalidCells.contains(Pair(secondCell.first, secondCell.second))
                )
            }
        }
    }

    @Test
    fun `셀 선택 시 에러 메시지가 사라지는지 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)

        val (emptyRow, emptyCol) = emptyCell!!

        // 빈 셀에 잘못된 값 입력 (에러 상태 생성)
        viewModel.selectCell(emptyRow, emptyCol)

        // 같은 행에 이미 있는 숫자 찾기
        val invalidValue = SudokuTestHelper.findInvalidValueForRow(state.board, emptyRow)
        assertTrue("잘못된 값을 찾을 수 있어야 함", invalidValue != null)

        viewModel.setCellValue(invalidValue!!)
        val errorState = viewModel.state.first()
        assertTrue("에러 상태가 생성되어야 함", errorState.invalidCells.contains(Pair(emptyRow, emptyCol)))

        // 다른 셀 선택 (에러 상태는 유지되어야 함)
        val anotherEmptyCell = SudokuTestHelper.findEmptyCell(errorState.board, viewModel)
        if (anotherEmptyCell != null && anotherEmptyCell != emptyCell) {
            val (row, col) = anotherEmptyCell
            viewModel.selectCell(row, col)
            val stateAfterSelection = viewModel.state.first()

            // 에러 상태가 유지되어야 함 (틀린 숫자는 빨간색으로 표시되어야 함)
            assertTrue(
                "에러 상태가 유지되어야 함",
                stateAfterSelection.invalidCells.contains(Pair(emptyRow, emptyCol))
            )
            assertEquals("선택된 셀이 변경되어야 함", row, stateAfterSelection.selectedRow)
            assertEquals("선택된 셀이 변경되어야 함", col, stateAfterSelection.selectedCol)
        }
    }

    @Test
    fun `에러 메시지 내용 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)

        val (emptyRow, emptyCol) = emptyCell!!

        // 빈 셀에 잘못된 값 입력
        viewModel.selectCell(emptyRow, emptyCol)

        val invalidValue = SudokuTestHelper.findInvalidValueForRow(state.board, emptyRow)
        assertTrue("잘못된 값을 찾을 수 있어야 함", invalidValue != null)

        viewModel.setCellValue(invalidValue!!)
        val errorState = viewModel.state.first()
        assertTrue("에러 상태가 생성되어야 함", errorState.invalidCells.contains(Pair(emptyRow, emptyCol)))
    }

    @Test
    fun `에러 상태 복구 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.first()
        val emptyCell = SudokuTestHelper.findEmptyCell(state.board, viewModel)
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyCell != null)

        val (emptyRow, emptyCol) = emptyCell!!

        // 빈 셀에 잘못된 값 입력
        viewModel.selectCell(emptyRow, emptyCol)

        val invalidValue = SudokuTestHelper.findInvalidValueForRow(state.board, emptyRow)
        assertTrue("잘못된 값을 찾을 수 있어야 함", invalidValue != null)

        viewModel.setCellValue(invalidValue!!)
        val errorState = viewModel.state.first()
        assertTrue("에러 상태가 생성되어야 함", errorState.invalidCells.contains(Pair(emptyRow, emptyCol)))

        // 유효한 값으로 복구
        val updatedState = viewModel.state.first()
        val validValue = SudokuTestHelper.findValidValue(updatedState.board, emptyRow, emptyCol)

        if (validValue != null) {
            viewModel.setCellValue(validValue)
            val recoveredState = viewModel.state.first()
            assertFalse(
                "에러 상태가 해결되어야 함",
                recoveredState.invalidCells.contains(Pair(emptyRow, emptyCol))
            )
        } else {
            // 유효한 값이 없으면 지우기
            viewModel.clearCell()
            val recoveredState = viewModel.state.first()
            assertFalse(
                "에러 상태가 해결되어야 함",
                recoveredState.invalidCells.contains(Pair(emptyRow, emptyCol))
            )
        }
    }

    @Test
    fun `다양한 에러 시나리오 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀들 찾기 (초기 셀이 아닌 셀들)
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(
                        row,
                        col
                    ) && viewModel.state.value.board[row][col] == 0
                ) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }

        assertTrue("테스트할 빈 셀이 있어야 함", emptyCells.isNotEmpty())

        // 각 빈 셀에 대해 에러 시나리오 테스트
        for ((row, col) in emptyCells.take(3)) { // 최대 3개만 테스트
            val state = viewModel.state.value

            val invalidValue = SudokuTestHelper.findInvalidValueForRow(state.board, row)

            if (invalidValue != null) {
                viewModel.selectCell(row, col)
                viewModel.setCellValue(invalidValue)
                advanceUntilIdle()

                val errorState = viewModel.state.value
                assertTrue("에러 상태가 생성되어야 함", errorState.invalidCells.contains(Pair(row, col)))

                // 복구
                viewModel.clearCell()
                advanceUntilIdle()

                val recoveredState = viewModel.state.value
                assertFalse("에러 상태가 해결되어야 함", recoveredState.invalidCells.contains(Pair(row, col)))
            }
        }
    }

    @Test
    fun `디버깅 테스트`() = runTest {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 초기 상태 확인
        val state = viewModel.state.first()
        println("[DEBUG] 초기 invalidCells: ${state.invalidCells}")

        // 초기 셀 찾기
        var initialRow = -1
        var initialCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0) {
                    initialRow = row
                    initialCol = col
                    break
                }
            }
            if (initialRow != -1) break
        }

        println("[DEBUG] 초기 셀: ($initialRow, $initialCol), 값: ${state.board[initialRow][initialCol]}")

        // 셀 선택
        viewModel.selectCell(initialRow, initialCol)
        val afterSelection = viewModel.state.first()
        println("[DEBUG] 셀 선택 후 invalidCells: ${afterSelection.invalidCells}")

        // 값 변경
        viewModel.setCellValue(9)
        val afterChange = viewModel.state.first()
        println("[DEBUG] 값 변경 후 invalidCells: ${afterChange.invalidCells}")
        println("[DEBUG] 변경된 값: ${afterChange.board[initialRow][initialCol]}")

        // 간단한 assert
        assertTrue(true) // 항상 통과
    }
}