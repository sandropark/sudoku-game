package com.sandro.new_sudoku

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameOverPopupTest {

    @Test
    fun `실수 3번시 게임 종료 팝업이 표시되어야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 빈 셀 찾기
        val state = viewModel.state.value
        var emptyRow = -1
        var emptyCol = -1

        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        // 더 간단한 방법: 3번의 실수를 여러 셀에서 만들기
        var mistakesMade = 0

        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= 3) break
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    viewModel.selectCell(row, col)
                    val wrongValue = (1..9).firstOrNull { value ->
                        state.board[row].contains(value)
                    }
                    if (wrongValue != null) {
                        viewModel.setCellValue(wrongValue)
                        mistakesMade++

                        val updatedState = viewModel.state.value
                        assertEquals(mistakesMade, updatedState.mistakeCount)

                        if (mistakesMade == 3) { // 3번째 실수
                            assertTrue(
                                "3번째 실수 후 게임 종료 팝업이 표시되어야 함",
                                updatedState.showGameOverDialog
                            )
                            assertFalse("팝업 표시 시 게임은 종료 상태가 아니어야 함", updatedState.isGameOver)
                        }
                    }
                }
            }
            if (mistakesMade >= 3) break
        }
    }

    @Test
    fun `게임 종료 팝업에서 계속하기 선택시 팝업이 닫히고 게임이 계속되어야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 더 간단한 방법: 3번의 실수를 여러 셀에서 만들기
        val state = viewModel.state.value
        var mistakesMade = 0

        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= 3) break
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    viewModel.selectCell(row, col)
                    val wrongValue = (1..9).firstOrNull { value ->
                        state.board[row].contains(value)
                    }
                    if (wrongValue != null) {
                        viewModel.setCellValue(wrongValue)
                        mistakesMade++
                    }
                }
            }
            if (mistakesMade >= 3) break
        }

        // 팝업이 표시되었는지 확인
        assertTrue(viewModel.state.value.showGameOverDialog)

        // 계속하기 선택
        viewModel.continueGameAfterMistakes()

        val updatedState = viewModel.state.value
        assertFalse("계속하기 후 팝업이 닫혀야 함", updatedState.showGameOverDialog)
        assertFalse("계속하기 후 게임이 종료 상태가 아니어야 함", updatedState.isGameOver)
        assertEquals("계속하기 후 실수 카운트가 2가 되어야 함", 2, updatedState.mistakeCount)
    }

    @Test
    fun `게임 종료 팝업에서 새 게임 선택시 새 게임이 시작되어야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 더 간단한 방법: 3번의 실수를 여러 셀에서 만들기
        val state = viewModel.state.value
        var mistakesMade = 0

        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= 3) break
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    viewModel.selectCell(row, col)
                    val wrongValue = (1..9).firstOrNull { value ->
                        state.board[row].contains(value)
                    }
                    if (wrongValue != null) {
                        viewModel.setCellValue(wrongValue)
                        mistakesMade++
                    }
                }
            }
            if (mistakesMade >= 3) break
        }

        // 팝업이 표시되었는지 확인
        assertTrue(viewModel.state.value.showGameOverDialog)

        // 새 게임 선택
        viewModel.startNewGameAfterMistakes()

        val updatedState = viewModel.state.value
        assertFalse("새 게임 후 팝업이 닫혀야 함", updatedState.showGameOverDialog)
        assertFalse("새 게임 후 게임이 종료 상태가 아니어야 함", updatedState.isGameOver)
        assertEquals("새 게임 후 실수 카운트가 0이어야 함", 0, updatedState.mistakeCount)
        assertEquals("새 게임 후 선택된 셀이 없어야 함", -1, updatedState.selectedRow)
        assertEquals("새 게임 후 선택된 셀이 없어야 함", -1, updatedState.selectedCol)
    }

    @Test
    fun `초기 상태에서는 게임 종료 팝업이 표시되지 않아야 한다`() = runTest {
        val viewModel = SudokuViewModel()
        val state = viewModel.state.value

        assertFalse("초기 상태에서 게임 종료 팝업이 표시되지 않아야 함", state.showGameOverDialog)
        assertFalse("초기 상태에서 게임이 종료 상태가 아니어야 함", state.isGameOver)
        assertEquals("초기 상태에서 실수 카운트가 0이어야 함", 0, state.mistakeCount)
    }
} 