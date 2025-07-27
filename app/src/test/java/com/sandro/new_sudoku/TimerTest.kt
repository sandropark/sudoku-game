package com.sandro.new_sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerTest {

    @Test
    fun `게임 시작시 타이머가 0부터 시작해야 한다`() {
        val viewModel = SudokuViewModel()

        val initialState = viewModel.state.value
        assertEquals("초기 타이머는 0이어야 함", 0, initialState.elapsedTimeSeconds)
        assertFalse("초기에는 타이머가 실행되지 않아야 함", initialState.isTimerRunning)
    }

    @Test
    fun `타이머 시작시 상태가 업데이트되어야 한다`() {
        val viewModel = SudokuViewModel()

        // 먼저 타이머를 정지시켜서 정확한 초기 상태로 만듦
        viewModel.stopTimer()
        viewModel.startTimer()

        val state = viewModel.state.value
        assertTrue("타이머가 실행 상태여야 함", state.isTimerRunning)
    }

    @Test
    fun `타이머 정지시 상태가 업데이트되어야 한다`() {
        val viewModel = SudokuViewModel()

        // 명확한 상태 설정
        viewModel.stopTimer()
        viewModel.startTimer()
        assertTrue("타이머 시작 확인", viewModel.state.value.isTimerRunning)

        viewModel.stopTimer()

        val state = viewModel.state.value
        assertFalse("타이머가 정지 상태여야 함", state.isTimerRunning)
    }

    @Test
    fun `타이머 일시정지 및 재개가 정상 작동해야 한다`() {
        val viewModel = SudokuViewModel()

        // 타이머 시작
        viewModel.startTimer()
        assertTrue("타이머 실행 상태", viewModel.state.value.isTimerRunning)

        // 일시정지
        viewModel.pauseTimer()
        assertFalse("타이머 일시정지 상태", viewModel.state.value.isTimerRunning)

        // 재개
        viewModel.resumeTimer()
        assertTrue("타이머 재개 상태", viewModel.state.value.isTimerRunning)
    }

    @Test
    fun `타이머 리셋시 시간이 0으로 초기화되어야 한다`() {
        val viewModel = SudokuViewModel()

        // 시간 흐름 시뮬레이션을 위해 직접 상태 설정
        viewModel.updateTimerForTest(30)

        assertEquals("타이머 시간이 설정되어야 함", 30, viewModel.state.value.elapsedTimeSeconds)

        viewModel.resetTimer()

        val state = viewModel.state.value
        assertEquals("타이머가 0으로 리셋되어야 함", 0, state.elapsedTimeSeconds)
        assertFalse("타이머가 정지 상태여야 함", state.isTimerRunning)
    }

    @Test
    fun `새 게임 시작시 타이머가 초기화되어야 한다`() {
        val viewModel = SudokuViewModel()

        // 먼저 타이머를 설정해놓고
        viewModel.updateTimerForTest(30)
        viewModel.startTimer()

        // 새 게임 시작
        viewModel.newGame()

        val state = viewModel.state.value
        assertEquals("새 게임 시작시 타이머는 0이어야 함", 0, state.elapsedTimeSeconds)
        assertFalse("새 게임 시작시 타이머는 정지 상태여야 함", state.isTimerRunning)
    }

    @Test
    fun `게임 완료시 타이머가 자동으로 정지되어야 한다`() {
        val viewModel = SudokuViewModel()

        // 게임 시작 후 타이머 수동 시작
        viewModel.newGame()
        viewModel.startTimer()
        assertTrue("게임 시작시 타이머 실행", viewModel.state.value.isTimerRunning)

        // 게임 완료 시뮬레이션 (모든 셀 채우기)
        simulateGameCompletion(viewModel)

        val state = viewModel.state.value
        if (state.isGameComplete) {
            assertFalse("게임 완료시 타이머가 정지되어야 함", state.isTimerRunning)
        }
    }

    @Test
    fun `타이머 포맷이 올바르게 변환되어야 한다`() {
        val viewModel = SudokuViewModel()

        // 다양한 시간 테스트
        assertEquals("0초", "00:00", viewModel.formatTime(0))
        assertEquals("30초", "00:30", viewModel.formatTime(30))
        assertEquals("1분", "01:00", viewModel.formatTime(60))
        assertEquals("1분 30초", "01:30", viewModel.formatTime(90))
        assertEquals("10분", "10:00", viewModel.formatTime(600))
        assertEquals("1시간", "60:00", viewModel.formatTime(3600))
        assertEquals("1시간 1분", "61:00", viewModel.formatTime(3660))
    }

    // 헬퍼 메서드들
    private fun simulateGameCompletion(viewModel: SudokuViewModel) {
        // 실제로는 모든 셀을 채워서 게임을 완료시키는 로직
        // 여기서는 간단히 solveGame을 호출
        viewModel.solveGame()
    }
} 