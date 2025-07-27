package com.sandro.new_sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TestSolveButtonTest {

    @Test
    fun `정답 입력 버튼 클릭시 게임이 완료되어야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 초기 상태 확인
        val initialState = viewModel.state.value
        assertFalse("초기에는 게임이 완료되지 않아야 함", initialState.isGameComplete)
        assertFalse("초기에는 완료 다이얼로그가 표시되지 않아야 함", initialState.showGameCompleteDialog)

        // 타이머 시작
        viewModel.startTimer()
        assertTrue("타이머가 실행 중이어야 함", viewModel.state.value.isTimerRunning)

        // 정답 입력 실행
        viewModel.fillCorrectAnswers()

        val finalState = viewModel.state.value
        assertTrue("정답 입력 후 게임이 완료되어야 함", finalState.isGameComplete)
        assertTrue("정답 입력 후 완료 다이얼로그가 표시되어야 함", finalState.showGameCompleteDialog)
        assertFalse("정답 입력 후 타이머가 정지되어야 함", finalState.isTimerRunning)
    }

    @Test
    fun `정답 입력 시 빈 셀만 채워져야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 초기 보드 상태 저장
        val initialState = viewModel.state.value
        val initialBoard = initialState.board.map { it.clone() }.toTypedArray()

        // 정답 입력 실행
        viewModel.fillCorrectAnswers()

        val finalState = viewModel.state.value
        val finalBoard = finalState.board

        // 초기 셀들은 변경되지 않았는지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                if (viewModel.isInitialCell(row, col)) {
                    assertEquals(
                        "초기 셀은 변경되지 않아야 함",
                        initialBoard[row][col],
                        finalBoard[row][col]
                    )
                }
            }
        }

        // 모든 셀이 1-9 범위의 값으로 채워져 있는지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                assertTrue(
                    "모든 셀이 1-9 범위의 값이어야 함",
                    finalBoard[row][col] in 1..9
                )
            }
        }
    }

    @Test
    fun `정답 입력 시 스도쿠 규칙이 만족되어야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 정답 입력 실행
        viewModel.fillCorrectAnswers()

        val finalState = viewModel.state.value
        val board = finalState.board

        // 행 검증
        for (row in 0..8) {
            val rowSet = mutableSetOf<Int>()
            for (col in 0..8) {
                val value = board[row][col]
                assertFalse("행에 중복된 숫자가 없어야 함", rowSet.contains(value))
                rowSet.add(value)
            }
            assertEquals("각 행에는 1-9가 모두 있어야 함", setOf(1, 2, 3, 4, 5, 6, 7, 8, 9), rowSet)
        }

        // 열 검증
        for (col in 0..8) {
            val colSet = mutableSetOf<Int>()
            for (row in 0..8) {
                val value = board[row][col]
                assertFalse("열에 중복된 숫자가 없어야 함", colSet.contains(value))
                colSet.add(value)
            }
            assertEquals("각 열에는 1-9가 모두 있어야 함", setOf(1, 2, 3, 4, 5, 6, 7, 8, 9), colSet)
        }

        // 3x3 박스 검증
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val boxSet = mutableSetOf<Int>()
                for (row in boxRow * 3 until (boxRow + 1) * 3) {
                    for (col in boxCol * 3 until (boxCol + 1) * 3) {
                        val value = board[row][col]
                        assertFalse("3x3 박스에 중복된 숫자가 없어야 함", boxSet.contains(value))
                        boxSet.add(value)
                    }
                }
                assertEquals("각 3x3 박스에는 1-9가 모두 있어야 함", setOf(1, 2, 3, 4, 5, 6, 7, 8, 9), boxSet)
            }
        }
    }

    @Test
    fun `정답 입력 시 실수 카운트는 변경되지 않아야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

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

        val mistakeCountBeforesolve = viewModel.state.value.mistakeCount

        // 정답 입력 실행
        viewModel.fillCorrectAnswers()

        val finalState = viewModel.state.value
        assertEquals(
            "정답 입력 시 실수 카운트는 변경되지 않아야 함",
            mistakeCountBeforesolve,
            finalState.mistakeCount
        )
    }

    @Test
    fun `정답 입력 버튼은 게임 진행 중에만 동작해야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 먼저 게임을 완료상태로 만들기
        viewModel.solveGame()
        assertTrue("게임이 완료되어야 함", viewModel.state.value.isGameComplete)

        // 완료된 게임에서 정답 입력 시도 (아무 변화 없어야 함)
        val beforeState = viewModel.state.value
        viewModel.fillCorrectAnswers()
        val afterState = viewModel.state.value

        // 상태가 동일해야 함 (게임이 이미 완료되어 있으므로)
        assertEquals(
            "완료된 게임에서는 상태가 변경되지 않아야 함",
            beforeState.isGameComplete,
            afterState.isGameComplete
        )
        assertEquals(
            "완료된 게임에서는 다이얼로그 상태가 변경되지 않아야 함",
            beforeState.showGameCompleteDialog,
            afterState.showGameCompleteDialog
        )
    }
} 