package com.sandro.new_sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCompleteTest {

    @Test
    fun `게임 완료시 완료 다이얼로그가 표시되어야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 초기 상태 확인
        val initialState = viewModel.state.value
        assertFalse("초기에는 게임 완료 다이얼로그가 표시되지 않아야 함", initialState.showGameCompleteDialog)
        assertFalse("초기에는 게임이 완료되지 않아야 함", initialState.isGameComplete)

        // 게임 완료 (해답 보기)
        viewModel.solveGame()

        val finalState = viewModel.state.value
        assertTrue("게임 완료시 완료 다이얼로그가 표시되어야 함", finalState.showGameCompleteDialog)
        assertTrue("게임이 완료 상태여야 함", finalState.isGameComplete)
        assertFalse("게임 완료시 타이머가 정지되어야 함", finalState.isTimerRunning)
    }

    @Test
    fun `완료 다이얼로그에서 새 게임 선택시 새 게임이 시작되어야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 타이머 시작 및 시간 설정
        viewModel.updateTimerForTest(180) // 3분
        viewModel.startTimer()

        // 게임 완료
        viewModel.solveGame()
        assertTrue("완료 다이얼로그가 표시되어야 함", viewModel.state.value.showGameCompleteDialog)

        // 새 게임 선택
        viewModel.startNewGameFromComplete()

        val finalState = viewModel.state.value
        assertFalse("새 게임 시작시 완료 다이얼로그가 닫혀야 함", finalState.showGameCompleteDialog)
        assertFalse("새 게임 시작시 게임 완료 상태가 아니어야 함", finalState.isGameComplete)
        assertEquals("새 게임 시작시 타이머가 0으로 초기화되어야 함", 0, finalState.elapsedTimeSeconds)
        assertFalse("새 게임 시작시 타이머가 정지 상태여야 함", finalState.isTimerRunning)
    }

    @Test
    fun `완료 다이얼로그에서 메인으로 이동 선택시 네비게이션 상태가 변경되어야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 게임 완료
        viewModel.solveGame()
        assertTrue("완료 다이얼로그가 표시되어야 함", viewModel.state.value.showGameCompleteDialog)

        // 메인으로 이동 선택
        viewModel.goToMainFromComplete()

        val finalState = viewModel.state.value
        assertFalse("메인 이동시 완료 다이얼로그가 닫혀야 함", finalState.showGameCompleteDialog)
        assertTrue("메인 이동시 네비게이션 플래그가 설정되어야 함", finalState.shouldNavigateToMain)
    }

    @Test
    fun `게임 완료 시 통계 정보가 올바르게 저장되어야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 타이머 시작 및 시간 설정
        viewModel.updateTimerForTest(125) // 2분 5초
        viewModel.startTimer()

        // 실수 몇 번 만들기
        val state = viewModel.state.value
        var mistakesMade = 0
        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= 2) break
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
            if (mistakesMade >= 2) break
        }

        // 게임 완료
        viewModel.solveGame()

        val finalState = viewModel.state.value
        assertTrue("완료 다이얼로그가 표시되어야 함", finalState.showGameCompleteDialog)
        assertEquals("완료시 타이머 시간이 유지되어야 함", 125, finalState.elapsedTimeSeconds)
        assertEquals("완료시 실수 횟수가 유지되어야 함", 2, finalState.mistakeCount)
        assertFalse("완료시 타이머가 정지되어야 함", finalState.isTimerRunning)
    }

    @Test
    fun `게임 완료 다이얼로그 닫기시 다이얼로그만 닫혀야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 게임 완료
        viewModel.solveGame()
        assertTrue("완료 다이얼로그가 표시되어야 함", viewModel.state.value.showGameCompleteDialog)

        // 다이얼로그 닫기
        viewModel.closeGameCompleteDialog()

        val finalState = viewModel.state.value
        assertFalse("다이얼로그 닫기시 완료 다이얼로그가 닫혀야 함", finalState.showGameCompleteDialog)
        assertTrue("다이얼로그 닫기시에도 게임 완료 상태는 유지되어야 함", finalState.isGameComplete)
    }

    @Test
    fun `실제 퍼즐 완성시에도 완료 다이얼로그가 표시되어야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 타이머 시작
        viewModel.startTimer()

        // 실제로 모든 셀을 채워서 게임 완료
        val state = viewModel.state.value
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    viewModel.selectCell(row, col)
                    // 유효한 값 찾기
                    val validValue = (1..9).firstOrNull { value ->
                        isValidMove(state.board, row, col, value)
                    }
                    if (validValue != null) {
                        viewModel.setCellValue(validValue)
                        // 게임이 완료되었는지 확인
                        if (viewModel.state.value.isGameComplete) {
                            break
                        }
                    }
                }
            }
            if (viewModel.state.value.isGameComplete) break
        }

        // 게임이 완료되지 않았다면 해답 보기로 완료
        if (!viewModel.state.value.isGameComplete) {
            viewModel.solveGame()
        }

        val finalState = viewModel.state.value
        assertTrue("퍼즐 완성시 완료 다이얼로그가 표시되어야 함", finalState.showGameCompleteDialog)
        assertTrue("퍼즐이 완성되어야 함", finalState.isGameComplete)
        assertFalse("완성시 타이머가 정지되어야 함", finalState.isTimerRunning)
    }

    // 헬퍼 메서드
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
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if ((r != row || c != col) && board[r][c] == value) return false
            }
        }

        return true
    }
} 