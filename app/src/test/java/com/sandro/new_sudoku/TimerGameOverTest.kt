package com.sandro.new_sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerGameOverTest {

    @Test
    fun `실수 3번 시 타이머가 정지되어야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 타이머 상태 수동 설정
        viewModel.updateTimerForTest(30)
        viewModel.startTimer()
        assertTrue("타이머가 실행 중이어야 함", viewModel.state.value.isTimerRunning)

        // 3번의 실수를 개별적으로 만들기
        makeDistinctMistakes(viewModel, 3)

        val state = viewModel.state.value
        assertTrue("게임 오버 팝업이 표시되어야 함", state.showGameOverDialog)
        assertFalse("실수 3번 시 타이머가 정지되어야 함", state.isTimerRunning)
    }

    @Test
    fun `게임 오버 팝업에서 계속하기 선택 시 타이머가 재시작되어야 한다`() {
        val viewModel = SudokuViewModel()
        viewModel.isTestMode = true

        // 타이머 상태 확인을 위해 수동으로 설정
        viewModel.updateTimerForTest(30)
        viewModel.startTimer()

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakes(viewModel)

        assertTrue("게임 오버 팝업이 표시되어야 함", viewModel.state.value.showGameOverDialog)
        assertFalse("타이머가 정지되어야 함", viewModel.state.value.isTimerRunning)

        // 계속하기 선택
        viewModel.continueGameAfterMistakes()

        val finalState = viewModel.state.value
        assertFalse("게임 오버 팝업이 닫혀야 함", finalState.showGameOverDialog)
        assertTrue("계속하기 후 타이머가 재시작되어야 함", finalState.isTimerRunning)
    }

    @Test
    fun `게임 오버 팝업에서 새 게임 선택 시 타이머가 정지 상태를 유지해야 한다`() {
        val viewModel = SudokuViewModel()

        // 타이머 시작
        viewModel.startTimer()

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakes(viewModel)

        assertTrue("게임 오버 팝업이 표시되어야 함", viewModel.state.value.showGameOverDialog)
        assertFalse("타이머가 정지되어야 함", viewModel.state.value.isTimerRunning)

        // 새 게임 선택 (재시작 옵션 팝업으로 이동)
        viewModel.requestNewGameOptions()

        val finalState = viewModel.state.value
        assertFalse("게임 오버 팝업이 닫혀야 함", finalState.showGameOverDialog)
        assertTrue("재시작 옵션 팝업이 표시되어야 함", finalState.showRestartOptionsDialog)
        assertFalse("새 게임 선택 시 타이머는 정지 상태여야 함", finalState.isTimerRunning)
    }

    @Test
    fun `재시작 옵션에서 재시도 선택 시 타이머가 새로 시작되어야 한다`() {
        val viewModel = SudokuViewModel()

        // 타이머 시작 후 시간 설정
        viewModel.startTimer()
        viewModel.updateTimerForTest(120) // 2분

        // 3번의 실수를 만들어서 게임 오버
        createThreeMistakes(viewModel)

        // 재시작 옵션 선택
        viewModel.requestNewGameOptions()
        viewModel.retryCurrentGame()

        val finalState = viewModel.state.value
        assertFalse("재시작 옵션 팝업이 닫혀야 함", finalState.showRestartOptionsDialog)
        assertEquals("타이머가 0으로 초기화되어야 함", 0, finalState.elapsedTimeSeconds)
        assertTrue("재시도 시 타이머가 자동으로 시작되어야 함", finalState.isTimerRunning)
    }

    @Test
    fun `팝업이 표시되지 않은 상태에서는 타이머가 계속 실행되어야 한다`() {
        val viewModel = SudokuViewModel()

        // 타이머 시작
        viewModel.startTimer()
        assertTrue("타이머가 실행 중이어야 함", viewModel.state.value.isTimerRunning)

        // 실수 2번만 (팝업이 뜨지 않음)
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

        val finalState = viewModel.state.value
        assertEquals("실수 2번까지만 해야 함", 2, finalState.mistakeCount)
        assertFalse("팝업이 표시되지 않아야 함", finalState.showGameOverDialog)
        assertTrue("타이머는 계속 실행되어야 함", finalState.isTimerRunning)
    }

    // 헬퍼 메서드들
    private fun createThreeMistakes(viewModel: SudokuViewModel) {
        makeDistinctMistakes(viewModel, 3)
    }

    private fun makeDistinctMistakes(viewModel: SudokuViewModel, targetMistakes: Int) {
        val state = viewModel.state.value
        var mistakesMade = 0
        val usedCells = mutableSetOf<Pair<Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= targetMistakes) break
                if (!viewModel.isInitialCell(row, col) &&
                    state.board[row][col] == 0 &&
                    !usedCells.contains(Pair(row, col))
                ) {

                    viewModel.selectCell(row, col)
                    val wrongValue = (1..9).firstOrNull { value ->
                        state.board[row].contains(value)
                    }
                    if (wrongValue != null) {
                        viewModel.setCellValue(wrongValue)
                        usedCells.add(Pair(row, col))
                        mistakesMade++

                        // 상태 확인
                        val currentState = viewModel.state.value
                        if (mistakesMade >= 3 && currentState.showGameOverDialog) {
                            break
                        }
                    }
                }
            }
            if (mistakesMade >= targetMistakes) break
        }
    }
} 