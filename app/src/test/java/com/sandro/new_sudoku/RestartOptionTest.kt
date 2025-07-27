package com.sandro.new_sudoku

import com.sandro.new_sudoku.ui.DifficultyLevel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestartOptionTest {

    @Test
    fun `게임 종료 후 새 게임 버튼 클릭시 재시작 옵션 팝업이 표시되어야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 3번의 실수를 만들어서 게임 오버 팝업 표시
        createThreeMistakes(viewModel)

        // 게임 오버 팝업이 표시되었는지 확인
        assertTrue(viewModel.state.value.showGameOverDialog)

        // 새 게임 버튼 클릭
        viewModel.requestNewGameOptions()

        val updatedState = viewModel.state.value
        assertFalse("게임 오버 팝업이 닫혀야 함", updatedState.showGameOverDialog)
        assertTrue("재시작 옵션 팝업이 표시되어야 함", updatedState.showRestartOptionsDialog)
    }

    @Test
    fun `재시도 선택시 같은 보드로 게임이 재시작되어야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 초기 보드 상태 저장
        val originalBoard = viewModel.state.value.board.map { it.clone() }.toTypedArray()

        // 게임 진행 (셀에 값 입력)
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

        if (emptyRow != -1) {
            viewModel.selectCell(emptyRow, emptyCol)
            val validValue = (1..9).firstOrNull { value ->
                isValidMove(state.board, emptyRow, emptyCol, value)
            }
            if (validValue != null) {
                viewModel.setCellValue(validValue)
            }
        }

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakes(viewModel)

        // 재시작 옵션 선택
        viewModel.requestNewGameOptions()
        viewModel.retryCurrentGame()

        val finalState = viewModel.state.value
        assertFalse("재시작 옵션 팝업이 닫혀야 함", finalState.showRestartOptionsDialog)
        assertEquals("실수 카운트가 0으로 초기화되어야 함", 0, finalState.mistakeCount)
        assertFalse("게임 오버 상태가 아니어야 함", finalState.isGameOver)

        // 보드가 원래 초기 상태로 복원되었는지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                assertEquals(
                    "보드가 초기 상태로 복원되어야 함",
                    originalBoard[row][col], finalState.board[row][col]
                )
            }
        }
    }

    @Test
    fun `난이도 변경 선택시 난이도 선택 팝업이 표시되어야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakes(viewModel)

        // 재시작 옵션 선택
        viewModel.requestNewGameOptions()
        viewModel.changeDifficultyAndRestart()

        val finalState = viewModel.state.value
        assertFalse("재시작 옵션 팝업이 닫혀야 함", finalState.showRestartOptionsDialog)
        assertTrue("난이도 변경을 위한 메인화면으로 이동해야 함", finalState.shouldNavigateToMain)
    }

    @Test
    fun `재시작 옵션 팝업에서 취소시 게임 화면으로 돌아가야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakes(viewModel)

        // 재시작 옵션 선택
        viewModel.requestNewGameOptions()
        assertTrue(viewModel.state.value.showRestartOptionsDialog)

        // 취소 선택
        viewModel.cancelRestartOptions()

        val finalState = viewModel.state.value
        assertFalse("재시작 옵션 팝업이 닫혀야 함", finalState.showRestartOptionsDialog)
        assertTrue("게임 오버 팝업이 다시 표시되어야 함", finalState.showGameOverDialog)
    }

    @Test
    fun `초기 게임 상태가 올바르게 저장되어야 한다`() = runTest {
        val viewModel = SudokuViewModel()

        // 초기 상태 확인
        viewModel.state.value
        assertNotNull("초기 보드 상태가 저장되어야 함", viewModel.getInitialBoard())

        // 새 게임 시작 시에도 초기 상태가 저장되어야 함
        viewModel.newGameWithDifficulty(DifficultyLevel.MEDIUM)
        assertNotNull("새 게임 시작 시에도 초기 보드 상태가 저장되어야 함", viewModel.getInitialBoard())
    }

    // 헬퍼 메서드들
    private fun createThreeMistakes(viewModel: SudokuViewModel) {
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
    }

    private fun isValidMove(board: Array<IntArray>, row: Int, col: Int, value: Int): Boolean {
        // 행 검사
        for (c in 0..8) {
            if (c != col && board[row][c] == value) return false
        }

        // 열 검사
        for (r in 0..8) {
            if (r != row && board[r][col] == value) return false
        }

        // 3x3 박스 검사
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != row || c != col) && board[r][c] == value) return false
            }
        }

        return true
    }
} 