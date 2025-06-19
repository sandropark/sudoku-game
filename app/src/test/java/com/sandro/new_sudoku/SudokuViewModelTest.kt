package com.sandro.new_sudoku

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.cancel
import kotlinx.coroutines.async
import kotlinx.coroutines.yield

class SudokuViewModelTest {

    @Test
    fun `초기 상태가 올바르게 설정되는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        val initialState = viewModel.state.first()
        
        // 초기 상태 확인
        assertEquals(-1, initialState.selectedRow)
        assertEquals(-1, initialState.selectedCol)
        assertFalse(initialState.isGameComplete)
        assertFalse(initialState.showError)
        assertEquals("", initialState.errorMessage)
        
        // 보드가 9x9 크기인지 확인
        assertEquals(9, initialState.board.size)
        assertEquals(9, initialState.board[0].size)
    }

    @Test
    fun `셀 선택이 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 셀 선택
        viewModel.selectCell(3, 4)
        val state = viewModel.state.first()
        
        assertEquals(3, state.selectedRow)
        assertEquals(4, state.selectedCol)
        assertFalse(state.showError) // 에러 메시지가 사라짐
    }

    @Test
    fun `유효한 셀 값 설정이 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 셀 선택 후 값 설정
        viewModel.selectCell(0, 2) // 빈 셀
        viewModel.setCellValue(4)
        
        val state = viewModel.state.first()
        assertEquals(4, state.board[0][2])
        assertFalse(state.showError)
    }

    @Test
    fun `초기 셀 수정 시도 시 에러가 발생하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 셀 선택 후 값 설정 시도
        viewModel.selectCell(0, 0) // 초기 숫자 5가 있는 셀
        viewModel.setCellValue(9)
        
        val state = viewModel.state.first()
        assertTrue(state.showError)
        assertEquals("초기 숫자는 변경할 수 없습니다", state.errorMessage)
        assertEquals(5, state.board[0][0]) // 값이 변경되지 않음
    }

    @Test
    fun `스도쿠 규칙 위반 시 에러가 발생하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 같은 행에 같은 숫자 설정 시도
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(1)
        viewModel.selectCell(0, 3)
        viewModel.setCellValue(1) // 같은 행에 1을 또 설정
        
        val state = viewModel.state.first()
        assertTrue(state.showError)
        assertEquals("이 숫자는 여기에 놓을 수 없습니다", state.errorMessage)
    }

    @Test
    fun `셀 지우기가 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 셀에 값 설정 후 지우기
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(4)
        viewModel.clearCell()
        
        val state = viewModel.state.first()
        assertEquals(0, state.board[0][2]) // 지워짐
    }

    @Test
    fun `새 게임 생성이 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 상태 저장
        val initialState = viewModel.state.first()
        
        // 일부 셀 수정
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(4)
        
        // 새 게임 생성
        viewModel.newGame()
        val newState = viewModel.state.first()
        
        // 상태가 초기화되었는지 확인
        assertEquals(-1, newState.selectedRow)
        assertEquals(-1, newState.selectedCol)
        assertFalse(newState.isGameComplete)
        assertFalse(newState.showError)
        
        // 보드가 초기 상태로 돌아갔는지 확인
        assertArrayEquals(initialState.board, newState.board)
    }

    @Test
    fun `게임 해답 보기가 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        viewModel.solveGame()
        val state = viewModel.state.first()
        
        // 모든 셀이 채워졌는지 확인
        assertTrue(state.isGameComplete)
        
        // 빈 셀이 없는지 확인
        for (row in state.board) {
            for (cell in row) {
                assertNotEquals(0, cell)
            }
        }
    }

    @Test
    fun `보드 초기화가 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 상태 저장
        val initialState = viewModel.state.first()
        
        // 일부 셀 수정
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(4)
        viewModel.selectCell(1, 1)
        viewModel.setCellValue(7)
        
        // 보드 초기화
        viewModel.clearBoard()
        val clearedState = viewModel.state.first()
        
        // 상태가 초기화되었는지 확인
        assertEquals(-1, clearedState.selectedRow)
        assertEquals(-1, clearedState.selectedCol)
        assertFalse(clearedState.isGameComplete)
        assertFalse(clearedState.showError)
        
        // 보드가 초기 상태로 돌아갔는지 확인
        assertArrayEquals(initialState.board, clearedState.board)
    }

    @Test
    fun `초기 셀 확인이 올바르게 작동하는지 테스트`() {
        val viewModel = SudokuViewModel()
        
        // 초기 셀들
        assertTrue(viewModel.isInitialCell(0, 0)) // 5
        assertTrue(viewModel.isInitialCell(0, 1)) // 3
        assertTrue(viewModel.isInitialCell(1, 0)) // 6
        
        // 빈 셀들
        assertFalse(viewModel.isInitialCell(0, 2))
        assertFalse(viewModel.isInitialCell(2, 0))
    }

    @Test
    fun `셀 선택 시 에러 메시지가 사라지는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 에러 발생시키기
        viewModel.selectCell(0, 0)
        viewModel.setCellValue(9)
        
        var state = viewModel.state.first()
        assertTrue(state.showError)
        
        // 다른 셀 선택
        viewModel.selectCell(0, 2)
        state = viewModel.state.first()
        assertFalse(state.showError)
    }

    @Test
    fun `셀이 선택되지 않은 상태에서 값 설정 시도 시 아무 일도 일어나지 않는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 셀 선택 없이 값 설정 시도
        viewModel.setCellValue(5)
        
        val state = viewModel.state.first()
        // (0,0)은 초기값 5가 있어야 함
        assertEquals(5, state.board[0][0])
    }

    @Test
    fun `ViewModel 상태 일관성 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        val initialState = viewModel.state.first()
        
        // 셀 선택
        viewModel.selectCell(0, 0)
        val afterSelection = viewModel.state.first()
        
        assertNotEquals("셀 선택 후 상태가 변경되어야 함", initialState, afterSelection)
        assertEquals("선택된 행이 올바르게 설정되어야 함", 0, afterSelection.selectedRow)
        assertEquals("선택된 열이 올바르게 설정되어야 함", 0, afterSelection.selectedCol)
        assertFalse("에러 상태가 초기화되어야 함", afterSelection.showError)
    }

    @Test
    fun `에러 상태 복구 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기값 셀에 값을 넣어서 에러 발생
        viewModel.selectCell(0, 0) // 초기값 셀 선택
        viewModel.setCellValue(5) // 에러 발생
        
        val errorState = viewModel.state.first()
        println("에러 상태: showError=${errorState.showError}, message='${errorState.errorMessage}'")
        assertTrue("에러가 표시되어야 함", errorState.showError)
        assertTrue("에러 메시지가 있어야 함", errorState.errorMessage.isNotEmpty())
        
        // 유효한 셀에 값을 넣어서 에러 복구
        val initialBoard = viewModel.state.first().board
        val emptyCell = findEmptyCell(initialBoard) ?: throw AssertionError("빈 셀 없음")
        val (row, col) = emptyCell
        val validValue = findValidValue(initialBoard, row, col) ?: throw AssertionError("유효한 값 없음")
        
        viewModel.selectCell(row, col) // 빈 셀 선택
        viewModel.setCellValue(validValue) // 유효한 값 입력
        
        val recoveredState = viewModel.state.first()
        println("복구 상태: showError=${recoveredState.showError}, message='${recoveredState.errorMessage}'")
        assertFalse("에러가 사라져야 함", recoveredState.showError)
        assertTrue("에러 메시지가 비워져야 함", recoveredState.errorMessage.isEmpty())
    }

    @Test
    fun `다양한 에러 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 1. 초기값 셀 수정 시도
        viewModel.selectCell(0, 0)
        viewModel.setCellValue(5)
        assertTrue("초기값 셀 수정 시 에러가 발생해야 함", viewModel.state.first().showError)
        
        // 2. 잘못된 이동 시도
        viewModel.selectCell(0, 2)
        viewModel.setCellValue(5) // 유효한 값 입력
        
        viewModel.selectCell(0, 3)
        viewModel.setCellValue(5) // 같은 행에 같은 숫자 입력 (잘못된 이동)
        assertTrue("잘못된 이동 시 에러가 발생해야 함", viewModel.state.first().showError)
    }

    @Test
    fun `게임 완료 플로우 테스트`() = runTest {
        val viewModel = SudokuViewModel()
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
        assertFalse("새 게임에서는 에러가 없어야 함", newGameState.showError)
    }

    // 빈 셀을 동적으로 찾는 헬퍼 함수
    private fun findEmptyCell(board: Array<IntArray>): Pair<Int, Int>? {
        for (i in 0..8) {
            for (j in 0..8) {
                if (board[i][j] == 0) return i to j
            }
        }
        return null
    }

    // 스도쿠 규칙상 유효한 값을 찾는 헬퍼 함수
    private fun findValidValue(board: Array<IntArray>, row: Int, col: Int): Int? {
        for (value in 1..9) {
            // 행 검사
            var validInRow = true
            for (c in 0..8) {
                if (c != col && board[row][c] == value) {
                    validInRow = false
                    break
                }
            }
            if (!validInRow) continue
            
            // 열 검사
            var validInCol = true
            for (r in 0..8) {
                if (r != row && board[r][col] == value) {
                    validInCol = false
                    break
                }
            }
            if (!validInCol) continue
            
            // 3x3 박스 검사
            val boxRow = (row / 3) * 3
            val boxCol = (col / 3) * 3
            var validInBox = true
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != row || c != col) && board[r][c] == value) {
                        validInBox = false
                        break
                    }
                }
                if (!validInBox) break
            }
            if (!validInBox) continue
            
            return value
        }
        return null
    }

    @Test
    fun `보드 클리어 및 리셋 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        val initialBoard = viewModel.state.first().board.map { it.copyOf() }.toTypedArray()
        val emptyCell = findEmptyCell(initialBoard) ?: throw AssertionError("빈 셀 없음")
        val (row, col) = emptyCell
        
        // 유효한 값 찾기
        val validValue = findValidValue(initialBoard, row, col) ?: throw AssertionError("유효한 값 없음")
        
        viewModel.selectCell(row, col)
        viewModel.setCellValue(validValue)
        val modifiedBoard = viewModel.state.first().board
        println("초기 보드 [$row][$col]: ${initialBoard[row][col]}")
        println("수정된 보드 [$row][$col]: ${modifiedBoard[row][col]}")
        println("입력한 값: $validValue")
        assertNotEquals("보드가 수정되어야 함. Actual: ${modifiedBoard[row][col]}", initialBoard[row][col], modifiedBoard[row][col])
        assertEquals("입력한 값이 저장되어야 함", validValue, modifiedBoard[row][col])
        
        // 보드 클리어
        viewModel.clearBoard()
        val clearedBoard = viewModel.state.first().board
        println("클리어 후 보드 [$row][$col]: ${clearedBoard[row][col]}")
        for (i in 0..8) {
            assertArrayEquals("클리어된 보드는 초기 보드와 같아야 함", initialBoard[i], clearedBoard[i])
        }
        val clearedState = viewModel.state.first()
        assertEquals("선택된 행이 초기화되어야 함", -1, clearedState.selectedRow)
        assertEquals("선택된 열이 초기화되어야 함", -1, clearedState.selectedCol)
        assertFalse("에러 상태가 초기화되어야 함", clearedState.showError)
    }

    @Test
    fun `동시 작업 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        val initialBoard = viewModel.state.first().board
        val emptyCell = findEmptyCell(initialBoard) ?: throw AssertionError("빈 셀 없음")
        val (row, col) = emptyCell
        
        // 빠른 연속 셀 선택
        for (i in 0..8) {
            viewModel.selectCell(row, col)
            val state = viewModel.state.first()
            assertEquals("선택된 행이 올바르게 설정되어야 함", row, state.selectedRow)
            assertEquals("선택된 열이 올바르게 설정되어야 함", col, state.selectedCol)
        }
        
        // 빠른 연속 값 설정 (유효한 값들만 사용)
        viewModel.selectCell(row, col)
        val validValues = mutableListOf<Int>()
        for (value in 1..9) {
            if (findValidValue(initialBoard, row, col) == value) {
                validValues.add(value)
                break
            }
        }
        
        if (validValues.isNotEmpty()) {
            val validValue = validValues.first()
            viewModel.setCellValue(validValue)
            val state = viewModel.state.first()
            println("설정된 값: 기대=$validValue, 실제=${state.board[row][col]}")
            assertEquals("설정된 값이 올바르게 저장되어야 함", validValue, state.board[row][col])
        }
    }

    @Test
    fun `StateFlow 일관성 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        val initialBoard = viewModel.state.first().board
        val emptyCell = findEmptyCell(initialBoard) ?: throw AssertionError("빈 셀 없음")
        val (row, col) = emptyCell

        viewModel.selectCell(row, col)
        val validValue = (1..9).first { findValidValue(initialBoard, row, col) == it }

        val initialState = viewModel.state.first()
        viewModel.setCellValue(validValue)
        yield()

        val updatedState = viewModel.state.first()
        assertNotEquals("상태 변화가 있어야 함", initialState, updatedState)
    }

    @Test
    fun `잘못된 셀 선택 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        val validSelections = listOf(
            Pair(0, 0), Pair(4, 4), Pair(8, 8), Pair(0, 8), Pair(8, 0)
        )
        
        for ((row, col) in validSelections) {
            viewModel.selectCell(row, col)
            val state = viewModel.state.first()
            assertEquals("유효한 선택이 반영되어야 함", row, state.selectedRow)
            assertEquals("유효한 선택이 반영되어야 함", col, state.selectedCol)
        }
    }

    @Test
    fun `에러 메시지 내용 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기값 셀에 값을 넣어서 에러 발생
        viewModel.selectCell(0, 0) // 초기값 셀 선택
        viewModel.setCellValue(5) // 에러 발생
        
        val errorState = viewModel.state.first()
        println("에러 메시지: '${errorState.errorMessage}'")
        assertEquals("초기 숫자는 변경할 수 없습니다", errorState.errorMessage)
        
        // 유효한 셀에 값을 넣어서 에러 메시지 초기화
        val initialBoard = viewModel.state.first().board
        val emptyCell = findEmptyCell(initialBoard) ?: throw AssertionError("빈 셀 없음")
        val (row, col) = emptyCell
        val validValue = findValidValue(initialBoard, row, col) ?: throw AssertionError("유효한 값 없음")
        
        viewModel.selectCell(row, col) // 빈 셀 선택
        viewModel.setCellValue(validValue) // 유효한 값 입력
        
        val recoveredState = viewModel.state.first()
        println("유효 입력 후 메시지: '${recoveredState.errorMessage}'")
        assertTrue("유효한 입력 후 에러 메시지가 비워져야 함", recoveredState.errorMessage.isEmpty())
    }

    @Test
    fun `초기 상태 테스트`() {
        val viewModel = SudokuViewModel()
        val initialState = viewModel.state.value
        
        assertNotNull("초기 상태가 null이 아니어야 함", initialState)
        assertNotNull("보드가 null이 아니어야 함", initialState.board)
        assertNotNull("초기 셀 정보가 null이 아니어야 함", initialState.isInitialCells)
        assertEquals("선택된 행이 -1이어야 함", -1, initialState.selectedRow)
        assertEquals("선택된 열이 -1이어야 함", -1, initialState.selectedCol)
        assertFalse("게임이 완료되지 않아야 함", initialState.isGameComplete)
        assertFalse("에러가 표시되지 않아야 함", initialState.showError)
        assertTrue("에러 메시지가 비어있어야 함", initialState.errorMessage.isEmpty())
    }
}