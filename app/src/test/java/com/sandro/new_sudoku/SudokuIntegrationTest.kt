package com.sandro.new_sudoku

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import org.junit.Test
import org.junit.Assert.*

class SudokuIntegrationTest {

    @Test
    fun `전체 게임 플레이 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 1. 초기 상태 확인
        var state = viewModel.state.first()
        assertEquals(-1, state.selectedRow)
        assertEquals(-1, state.selectedCol)
        assertFalse(state.isGameComplete)
        assertFalse(state.showError)
        
        // 2. 셀 선택
        viewModel.selectCell(0, 2)
        state = viewModel.state.first()
        assertEquals(0, state.selectedRow)
        assertEquals(2, state.selectedCol)
        
        // 3. 유효한 숫자 입력
        viewModel.setCellValue(4)
        state = viewModel.state.first()
        assertEquals(4, state.board[0][2])
        assertFalse(state.showError)
        
        // 4. 다른 셀 선택
        viewModel.selectCell(0, 3)
        state = viewModel.state.first()
        assertEquals(0, state.selectedRow)
        assertEquals(3, state.selectedCol)
        
        // 5. 유효한 숫자 입력
        viewModel.setCellValue(6)
        state = viewModel.state.first()
        assertEquals(6, state.board[0][3])
        assertFalse(state.showError)
        
        // 6. 초기 셀 수정 시도 (에러 발생)
        viewModel.selectCell(0, 0)
        viewModel.setCellValue(9)
        state = viewModel.state.first()
        assertTrue(state.showError)
        assertEquals(5, state.board[0][0]) // 변경되지 않음
        
        // 7. 다른 셀 선택 (에러 메시지 사라짐)
        viewModel.selectCell(0, 5) // 빈 셀 선택
        state = viewModel.state.first()
        assertFalse(state.showError)
        
        // 8. 셀 지우기 (선택된 셀에서만 작동)
        viewModel.clearCell()
        state = viewModel.state.first()
        assertEquals(0, state.board[0][5]) // 선택된 셀이 지워짐
    }

    @Test
    fun `게임 완료 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 해답 보기로 게임 완료
        viewModel.solveGame()
        val state = viewModel.state.first()
        
        // 게임 완료 상태 확인
        assertTrue(state.isGameComplete)
        
        // 모든 셀이 채워졌는지 확인
        for (row in state.board) {
            for (cell in row) {
                assertNotEquals(0, cell)
            }
        }
    }

    @Test
    fun `새 게임 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 상태 저장
        val initialState = viewModel.state.first()
        
        // 게임 진행
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(4)
        viewModel.selectCell(0, 3)
        viewModel.setCellValue(6)
        
        // 새 게임 생성
        viewModel.newGame()
        val newState = viewModel.state.first()
        
        // 상태 초기화 확인
        assertEquals(-1, newState.selectedRow)
        assertEquals(-1, newState.selectedCol)
        assertFalse(newState.isGameComplete)
        assertFalse(newState.showError)
        
        // 보드가 초기 상태로 돌아갔는지 확인
        assertArrayEquals(initialState.board, newState.board)
    }

    @Test
    fun `보드 초기화 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 상태 저장
        val initialState = viewModel.state.first()
        
        // 게임 진행
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(4)
        viewModel.selectCell(0, 3)
        viewModel.setCellValue(6)
        
        // 보드 초기화
        viewModel.clearBoard()
        val clearedState = viewModel.state.first()
        
        // 상태 초기화 확인
        assertEquals(-1, clearedState.selectedRow)
        assertEquals(-1, clearedState.selectedCol)
        assertFalse(clearedState.isGameComplete)
        assertFalse(clearedState.showError)
        
        // 보드가 초기 상태로 돌아갔는지 확인
        assertArrayEquals(initialState.board, clearedState.board)
    }

    @Test
    fun `스도쿠 규칙 위반 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 첫 번째 행에 같은 숫자 입력 시도
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(1)
        
        viewModel.selectCell(0, 3)
        viewModel.setCellValue(1) // 같은 행에 1을 또 입력
        
        val state = viewModel.state.first()
        assertTrue(state.showError)
        assertEquals(0, state.board[0][3]) // 값이 설정되지 않음
    }

    @Test
    fun `연속적인 셀 선택과 입력 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 보드에서 빈 셀들만 선택하여 연속 입력 테스트
        val testCases = listOf(
            Triple(0, 2, 4), // 빈 셀
            Triple(0, 3, 6), // 빈 셀
            Triple(0, 5, 8), // 빈 셀
            Triple(0, 6, 9), // 빈 셀
            Triple(0, 7, 1)  // 빈 셀
        )
        
        for ((row, col, value) in testCases) {
            viewModel.selectCell(row, col)
            viewModel.setCellValue(value)
            
            val state = viewModel.state.first()
            assertEquals(value, state.board[row][col])
            assertFalse(state.showError)
        }
    }

    @Test
    fun `에러 상태에서 정상 입력으로 복구 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 에러 발생
        viewModel.selectCell(0, 0)
        viewModel.setCellValue(9)
        
        var state = viewModel.state.first()
        assertTrue(state.showError)
        
        // 정상 입력으로 복구
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(4)
        
        state = viewModel.state.first()
        assertFalse(state.showError)
        assertEquals(4, state.board[0][2])
    }

    @Test
    fun `게임 상태 일관성 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 게임 진행
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(4)
        
        var state = viewModel.state.first()
        
        // 상태 일관성 확인
        assertEquals(0, state.selectedRow)
        assertEquals(2, state.selectedCol)
        assertEquals(4, state.board[0][2])
        assertFalse(state.isGameComplete)
        assertFalse(state.showError)
        
        // 해답 보기
        viewModel.solveGame()
        state = viewModel.state.first()
        
        // 게임 완료 상태 확인
        assertTrue(state.isGameComplete)
        
        // 모든 셀이 채워졌는지 확인
        for (row in state.board) {
            for (cell in row) {
                assertNotEquals(0, cell)
            }
        }
    }

    @Test
    fun `셀 선택 없이 지우기 시도 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 셀 선택 없이 지우기 시도
        viewModel.clearCell()
        val state = viewModel.state.first()
        
        // 아무 일도 일어나지 않아야 함
        assertEquals(-1, state.selectedRow)
        assertEquals(-1, state.selectedCol)
        assertFalse(state.showError)
    }

    @Test
    fun `초기 셀에서 지우기 시도 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 셀 선택 후 지우기 시도
        viewModel.selectCell(0, 0) // 초기값 5가 있는 셀
        viewModel.clearCell()
        
        val state = viewModel.state.first()
        // 초기 셀은 지워지지 않아야 함
        assertEquals(5, state.board[0][0])
        assertTrue(state.showError)
    }
} 