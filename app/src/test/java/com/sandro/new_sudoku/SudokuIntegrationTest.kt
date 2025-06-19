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
        
        // 2. 빈 셀 찾기
        var emptyRow = -1
        var emptyCol = -1
        var initialRow = -1
        var initialCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] == 0 && emptyRow == -1) {
                    emptyRow = row
                    emptyCol = col
                }
                if (state.board[row][col] != 0 && initialRow == -1) {
                    initialRow = row
                    initialCol = col
                }
            }
        }
        
        // 3. 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)
        state = viewModel.state.first()
        assertEquals(emptyRow, state.selectedRow)
        assertEquals(emptyCol, state.selectedCol)
        
        // 4. 유효한 숫자 입력
        viewModel.setCellValue(4)
        state = viewModel.state.first()
        assertEquals(4, state.board[emptyRow][emptyCol])
        assertFalse(state.showError)
        
        // 5. 다른 빈 셀 찾기
        var secondEmptyRow = -1
        var secondEmptyCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] == 0 && (row != emptyRow || col != emptyCol)) {
                    secondEmptyRow = row
                    secondEmptyCol = col
                    break
                }
            }
            if (secondEmptyRow != -1) break
        }
        
        // 6. 다른 셀 선택
        viewModel.selectCell(secondEmptyRow, secondEmptyCol)
        state = viewModel.state.first()
        assertEquals(secondEmptyRow, state.selectedRow)
        assertEquals(secondEmptyCol, state.selectedCol)
        
        // 7. 유효한 숫자 입력
        viewModel.setCellValue(6)
        state = viewModel.state.first()
        assertEquals(6, state.board[secondEmptyRow][secondEmptyCol])
        assertFalse(state.showError)
        
        // 8. 초기 셀 수정 시도 (에러 발생)
        viewModel.selectCell(initialRow, initialCol)
        viewModel.setCellValue(9)
        state = viewModel.state.first()
        assertTrue(state.showError)
        assertNotEquals(9, state.board[initialRow][initialCol]) // 변경되지 않음
        
        // 9. 다른 셀 선택 (에러 메시지 사라짐)
        viewModel.selectCell(emptyRow, emptyCol) // 빈 셀 선택
        state = viewModel.state.first()
        assertFalse(state.showError)
        
        // 10. 셀 지우기 (선택된 셀에서만 작동)
        viewModel.clearCell()
        state = viewModel.state.first()
        assertEquals(0, state.board[emptyRow][emptyCol]) // 선택된 셀이 지워짐
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
        
        // 빈 셀들 찾기
        var emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (initialState.board[row][col] == 0) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        
        // 게임 진행 (빈 셀들에 값 입력)
        if (emptyCells.size >= 2) {
            viewModel.selectCell(emptyCells[0].first, emptyCells[0].second)
            viewModel.setCellValue(4)
            viewModel.selectCell(emptyCells[1].first, emptyCells[1].second)
            viewModel.setCellValue(6)
        }
        
        // 새 게임 생성
        viewModel.newGame()
        val newState = viewModel.state.first()
        
        // 상태 초기화 확인
        assertEquals(-1, newState.selectedRow)
        assertEquals(-1, newState.selectedCol)
        assertFalse(newState.isGameComplete)
        assertFalse(newState.showError)
        
        // 보드가 새로운 게임으로 변경되었는지 확인 (같지 않아야 함)
        var hasChanges = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (initialState.board[row][col] != newState.board[row][col]) {
                    hasChanges = true
                    break
                }
            }
            if (hasChanges) break
        }
        assertTrue(hasChanges)
    }

    @Test
    fun `보드 초기화 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 상태 저장
        val initialState = viewModel.state.first()
        
        // 빈 셀들 찾기
        var emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (initialState.board[row][col] == 0) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        
        // 게임 진행 (빈 셀들에 값 입력)
        if (emptyCells.size >= 2) {
            viewModel.selectCell(emptyCells[0].first, emptyCells[0].second)
            viewModel.setCellValue(4)
            viewModel.selectCell(emptyCells[1].first, emptyCells[1].second)
            viewModel.setCellValue(6)
        }
        
        // 보드 초기화
        viewModel.clearBoard()
        val clearedState = viewModel.state.first()
        
        // 상태 초기화 확인
        assertEquals(-1, clearedState.selectedRow)
        assertEquals(-1, clearedState.selectedCol)
        assertFalse(clearedState.isGameComplete)
        assertFalse(clearedState.showError)
        
        // 보드가 초기 상태로 돌아갔는지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                assertEquals(initialState.board[row][col], clearedState.board[row][col])
            }
        }
    }

    @Test
    fun `스도쿠 규칙 위반 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 빈 셀들 찾기
        var state = viewModel.state.first()
        var emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] == 0) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        
        if (emptyCells.size >= 2) {
            // 같은 행에 같은 숫자 입력 시도
            val firstCell = emptyCells[0]
            val secondCell = emptyCells[1]
            
            // 같은 행에 있는지 확인
            if (firstCell.first == secondCell.first) {
                viewModel.selectCell(firstCell.first, firstCell.second)
                viewModel.setCellValue(1)
                
                viewModel.selectCell(secondCell.first, secondCell.second)
                viewModel.setCellValue(1) // 같은 행에 1을 또 입력
                
                state = viewModel.state.first()
                assertTrue(state.showError)
                assertEquals(0, state.board[secondCell.first][secondCell.second]) // 값이 설정되지 않음
            }
        }
    }

    @Test
    fun `연속적인 셀 선택과 입력 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 보드에서 빈 셀들 찾기
        var state = viewModel.state.first()
        var emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] == 0) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        
        // 최대 5개까지만 테스트
        val testCount = minOf(5, emptyCells.size)
        for (i in 0 until testCount) {
            val (row, col) = emptyCells[i]
            val value = i + 1
            viewModel.selectCell(row, col)
            viewModel.setCellValue(value)
            
            state = viewModel.state.first()
            assertEquals(value, state.board[row][col])
            assertFalse(state.showError)
        }
    }

    @Test
    fun `에러 상태에서 정상 입력으로 복구 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 셀과 빈 셀 찾기
        var state = viewModel.state.first()
        var initialRow = -1
        var initialCol = -1
        var emptyRow = -1
        var emptyCol = -1
        
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0 && initialRow == -1) {
                    initialRow = row
                    initialCol = col
                }
                if (state.board[row][col] == 0 && emptyRow == -1) {
                    emptyRow = row
                    emptyCol = col
                }
            }
        }
        
        // 에러 발생
        viewModel.selectCell(initialRow, initialCol)
        viewModel.setCellValue(9)
        
        state = viewModel.state.first()
        assertTrue(state.showError)
        
        // 정상 입력으로 복구
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(4)
        
        state = viewModel.state.first()
        assertFalse(state.showError)
        assertEquals(4, state.board[emptyRow][emptyCol])
    }

    @Test
    fun `게임 상태 일관성 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 빈 셀 찾기
        var state = viewModel.state.first()
        var emptyRow = -1
        var emptyCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }
        
        // 게임 진행
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(4)
        
        state = viewModel.state.first()
        
        // 상태 일관성 확인
        assertEquals(emptyRow, state.selectedRow)
        assertEquals(emptyCol, state.selectedCol)
        assertEquals(4, state.board[emptyRow][emptyCol])
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
        
        // 초기 셀 찾기
        var state = viewModel.state.first()
        var initialRow = -1
        var initialCol = -1
        var initialValue = 0
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0) {
                    initialRow = row
                    initialCol = col
                    initialValue = state.board[row][col]
                    break
                }
            }
            if (initialRow != -1) break
        }
        
        // 초기 셀 선택 후 지우기 시도
        viewModel.selectCell(initialRow, initialCol)
        viewModel.clearCell()
        
        state = viewModel.state.first()
        // 초기 셀은 지워지지 않아야 함
        assertEquals(initialValue, state.board[initialRow][initialCol])
        assertTrue(state.showError)
    }
} 