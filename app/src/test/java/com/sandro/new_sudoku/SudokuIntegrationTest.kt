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
        assertFalse(state.showError)
        
        // 게임이 이미 완료된 상태라면 새 게임을 생성
        if (state.isGameComplete) {
            viewModel.newGame()
            state = viewModel.state.first()
            assertFalse(state.isGameComplete)
        }
        
        // 2. 빈 셀 찾기 (초기 셀이 아닌 셀)
        var emptyRow = -1
        var emptyCol = -1
        var initialRow = -1
        var initialCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && emptyRow == -1) {
                    emptyRow = row
                    emptyCol = col
                }
                if (viewModel.isInitialCell(row, col) && initialRow == -1) {
                    initialRow = row
                    initialCol = col
                }
            }
        }
        
        // 빈 셀이 없으면 테스트를 건너뜀
        if (emptyRow == -1 || initialRow == -1) {
            return@runTest
        }
        
        // 3. 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)
        state = viewModel.state.first()
        assertEquals(emptyRow, state.selectedRow)
        assertEquals(emptyCol, state.selectedCol)
        
        // 4. 유효한 숫자 입력
        val validValue1 = (1..9).firstOrNull { value ->
            val board = viewModel.state.value.board
            if (board[emptyRow][emptyCol] != 0) return@firstOrNull false
            
            // 행 검사
            for (c in 0..8) {
                if (c != emptyCol && board[emptyRow][c] == value) return@firstOrNull false
            }
            
            // 열 검사
            for (r in 0..8) {
                if (r != emptyRow && board[r][emptyCol] == value) return@firstOrNull false
            }
            
            // 3x3 박스 검사
            val boxRow = (emptyRow / 3) * 3
            val boxCol = (emptyCol / 3) * 3
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != emptyRow || c != emptyCol) && board[r][c] == value) return@firstOrNull false
                }
            }
            
            true
        }
        assertNotNull("넣을 수 있는 값이 있어야 함", validValue1)
        viewModel.setCellValue(validValue1!!)
        state = viewModel.state.first()
        assertEquals(validValue1, state.board[emptyRow][emptyCol])
        assertFalse(state.showError)
        
        // 5. 다른 빈 셀 찾기
        var secondEmptyRow = -1
        var secondEmptyCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && (row != emptyRow || col != emptyCol)) {
                    secondEmptyRow = row
                    secondEmptyCol = col
                    break
                }
            }
            if (secondEmptyRow != -1) break
        }
        
        // 두 번째 빈 셀이 없으면 테스트를 건너뜀
        if (secondEmptyRow == -1) {
            return@runTest
        }
        
        // 6. 다른 셀 선택
        viewModel.selectCell(secondEmptyRow, secondEmptyCol)
        state = viewModel.state.first()
        assertEquals(secondEmptyRow, state.selectedRow)
        assertEquals(secondEmptyCol, state.selectedCol)
        
        // 7. 유효한 숫자 입력
        val validValue2 = (1..9).firstOrNull { value ->
            val board = viewModel.state.value.board
            if (board[secondEmptyRow][secondEmptyCol] != 0) return@firstOrNull false
            
            // 행 검사
            for (c in 0..8) {
                if (c != secondEmptyCol && board[secondEmptyRow][c] == value) return@firstOrNull false
            }
            
            // 열 검사
            for (r in 0..8) {
                if (r != secondEmptyRow && board[r][secondEmptyCol] == value) return@firstOrNull false
            }
            
            // 3x3 박스 검사
            val boxRow = (secondEmptyRow / 3) * 3
            val boxCol = (secondEmptyCol / 3) * 3
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != secondEmptyRow || c != secondEmptyCol) && board[r][c] == value) return@firstOrNull false
                }
            }
            
            true
        }
        assertNotNull("넣을 수 있는 값이 있어야 함", validValue2)
        viewModel.setCellValue(validValue2!!)
        state = viewModel.state.first()
        assertEquals(validValue2, state.board[secondEmptyRow][secondEmptyCol])
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
        
        // 빈 셀들 찾기 (초기 셀이 아닌 셀)
        var emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col)) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        
        // 게임 진행 (빈 셀들에 값 입력)
        if (emptyCells.size >= 2) {
            val validValue1 = (1..9).firstOrNull { value ->
                val board = viewModel.state.value.board
                if (board[emptyCells[0].first][emptyCells[0].second] != 0) return@firstOrNull false
                
                // 행 검사
                for (c in 0..8) {
                    if (c != emptyCells[0].second && board[emptyCells[0].first][c] == value) return@firstOrNull false
                }
                
                // 열 검사
                for (r in 0..8) {
                    if (r != emptyCells[0].first && board[r][emptyCells[0].second] == value) return@firstOrNull false
                }
                
                // 3x3 박스 검사
                val boxRow = (emptyCells[0].first / 3) * 3
                val boxCol = (emptyCells[0].second / 3) * 3
                for (r in boxRow until boxRow + 3) {
                    for (c in boxCol until boxCol + 3) {
                        if ((r != emptyCells[0].first || c != emptyCells[0].second) && board[r][c] == value) return@firstOrNull false
                    }
                }
                
                true
            }
            assertNotNull("넣을 수 있는 값이 있어야 함", validValue1)
            viewModel.selectCell(emptyCells[0].first, emptyCells[0].second)
            viewModel.setCellValue(validValue1!!)
            
            val validValue2 = (1..9).firstOrNull { value ->
                val board = viewModel.state.value.board
                if (board[emptyCells[1].first][emptyCells[1].second] != 0) return@firstOrNull false
                
                // 행 검사
                for (c in 0..8) {
                    if (c != emptyCells[1].second && board[emptyCells[1].first][c] == value) return@firstOrNull false
                }
                
                // 열 검사
                for (r in 0..8) {
                    if (r != emptyCells[1].first && board[r][emptyCells[1].second] == value) return@firstOrNull false
                }
                
                // 3x3 박스 검사
                val boxRow = (emptyCells[1].first / 3) * 3
                val boxCol = (emptyCells[1].second / 3) * 3
                for (r in boxRow until boxRow + 3) {
                    for (c in boxCol until boxCol + 3) {
                        if ((r != emptyCells[1].first || c != emptyCells[1].second) && board[r][c] == value) return@firstOrNull false
                    }
                }
                
                true
            }
            assertNotNull("넣을 수 있는 값이 있어야 함", validValue2)
            viewModel.selectCell(emptyCells[1].first, emptyCells[1].second)
            viewModel.setCellValue(validValue2!!)
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
        
        // 빈 셀들 찾기 (초기 셀이 아닌 셀)
        var emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col)) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        
        // 게임 진행 (빈 셀들에 값 입력)
        if (emptyCells.size >= 2) {
            val validValue1 = (1..9).firstOrNull { value ->
                val board = viewModel.state.value.board
                if (board[emptyCells[0].first][emptyCells[0].second] != 0) return@firstOrNull false
                
                // 행 검사
                for (c in 0..8) {
                    if (c != emptyCells[0].second && board[emptyCells[0].first][c] == value) return@firstOrNull false
                }
                
                // 열 검사
                for (r in 0..8) {
                    if (r != emptyCells[0].first && board[r][emptyCells[0].second] == value) return@firstOrNull false
                }
                
                // 3x3 박스 검사
                val boxRow = (emptyCells[0].first / 3) * 3
                val boxCol = (emptyCells[0].second / 3) * 3
                for (r in boxRow until boxRow + 3) {
                    for (c in boxCol until boxCol + 3) {
                        if ((r != emptyCells[0].first || c != emptyCells[0].second) && board[r][c] == value) return@firstOrNull false
                    }
                }
                
                true
            }
            assertNotNull("넣을 수 있는 값이 있어야 함", validValue1)
            viewModel.selectCell(emptyCells[0].first, emptyCells[0].second)
            viewModel.setCellValue(validValue1!!)
            
            val validValue2 = (1..9).firstOrNull { value ->
                val board = viewModel.state.value.board
                if (board[emptyCells[1].first][emptyCells[1].second] != 0) return@firstOrNull false
                
                // 행 검사
                for (c in 0..8) {
                    if (c != emptyCells[1].second && board[emptyCells[1].first][c] == value) return@firstOrNull false
                }
                
                // 열 검사
                for (r in 0..8) {
                    if (r != emptyCells[1].first && board[r][emptyCells[1].second] == value) return@firstOrNull false
                }
                
                // 3x3 박스 검사
                val boxRow = (emptyCells[1].first / 3) * 3
                val boxCol = (emptyCells[1].second / 3) * 3
                for (r in boxRow until boxRow + 3) {
                    for (c in boxCol until boxCol + 3) {
                        if ((r != emptyCells[1].first || c != emptyCells[1].second) && board[r][c] == value) return@firstOrNull false
                    }
                }
                
                true
            }
            assertNotNull("넣을 수 있는 값이 있어야 함", validValue2)
            viewModel.selectCell(emptyCells[1].first, emptyCells[1].second)
            viewModel.setCellValue(validValue2!!)
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
        
        // 빈 셀들 찾기 (초기 셀이 아닌 셀)
        var state = viewModel.state.first()
        var emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col)) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        
        if (emptyCells.size >= 2) {
            // 같은 행에 있는 빈 셀들 찾기
            val sameRowCells = emptyCells.groupBy { it.first }.values.firstOrNull { it.size >= 2 }
            
            if (sameRowCells != null) {
                val firstCell = sameRowCells[0]
                val secondCell = sameRowCells[1]
                
                // 첫 번째 셀에 유효한 값 입력
                val validValue = (1..9).firstOrNull { value ->
                    viewModel.state.value.board[firstCell.first][firstCell.second] == 0 && 
                    viewModel.state.value.board.all { r -> r[firstCell.second] != value } && 
                    viewModel.state.value.board[firstCell.first].all { c -> c != value }
                }
                assertNotNull("유효한 값이 있어야 함", validValue)
                
                viewModel.selectCell(firstCell.first, firstCell.second)
                viewModel.setCellValue(validValue!!)
                
                // 두 번째 셀에 같은 값을 입력 (같은 행에 같은 숫자 - 스도쿠 규칙 위반)
                viewModel.selectCell(secondCell.first, secondCell.second)
                viewModel.setCellValue(validValue) // 같은 행에 같은 숫자 입력
                
                state = viewModel.state.first()
                assertTrue(state.showError)
                assertEquals(0, state.board[secondCell.first][secondCell.second]) // 값이 설정되지 않음
            }
        }
    }

    @Test
    fun `연속적인 셀 선택과 입력 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 보드에서 빈 셀들 찾기 (초기 셀이 아닌 셀)
        var state = viewModel.state.first()
        var emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col)) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        
        // 최대 5개까지만 테스트
        val testCount = minOf(5, emptyCells.size)
        for (i in 0 until testCount) {
            val (row, col) = emptyCells[i]
            // 넣을 수 있는 값 찾기 (완전한 스도쿠 규칙 검사)
            val value = (1..9).firstOrNull { value ->
                val board = viewModel.state.value.board
                if (board[row][col] != 0) return@firstOrNull false
                // 행 검사
                for (c in 0..8) {
                    if (c != col && board[row][c] == value) return@firstOrNull false
                }
                // 열 검사
                for (r in 0..8) {
                    if (r != row && board[r][col] == value) return@firstOrNull false
                }
                // 3x3 박스 검사
                val boxRow = (row / 3) * 3
                val boxCol = (col / 3) * 3
                for (r in boxRow until boxRow + 3) {
                    for (c in boxCol until boxCol + 3) {
                        if ((r != row || c != col) && board[r][c] == value) return@firstOrNull false
                    }
                }
                true
            }
            if (value == null) break
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
                if (viewModel.isInitialCell(row, col) && initialRow == -1) {
                    initialRow = row
                    initialCol = col
                }
                if (!viewModel.isInitialCell(row, col) && emptyRow == -1) {
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
        val validValue = (1..9).firstOrNull { value ->
            val board = viewModel.state.value.board
            if (board[emptyRow][emptyCol] != 0) return@firstOrNull false
            
            // 행 검사
            for (c in 0..8) {
                if (c != emptyCol && board[emptyRow][c] == value) return@firstOrNull false
            }
            
            // 열 검사
            for (r in 0..8) {
                if (r != emptyRow && board[r][emptyCol] == value) return@firstOrNull false
            }
            
            // 3x3 박스 검사
            val boxRow = (emptyRow / 3) * 3
            val boxCol = (emptyCol / 3) * 3
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != emptyRow || c != emptyCol) && board[r][c] == value) return@firstOrNull false
                }
            }
            
            true
        }
        assertNotNull("넣을 수 있는 값이 있어야 함", validValue)
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(validValue!!)
        
        state = viewModel.state.first()
        assertFalse(state.showError)
        assertEquals(validValue, state.board[emptyRow][emptyCol])
    }

    @Test
    fun `게임 상태 일관성 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 빈 셀 찾기 (초기 셀이 아닌 셀)
        var state = viewModel.state.first()
        var emptyRow = -1
        var emptyCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col)) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }
        
        // 넣을 수 있는 유효한 값 찾기
        val validValue = (1..9).firstOrNull { value ->
            val board = viewModel.state.value.board
            if (board[emptyRow][emptyCol] != 0) return@firstOrNull false
            
            // 행 검사
            for (c in 0..8) {
                if (c != emptyCol && board[emptyRow][c] == value) return@firstOrNull false
            }
            
            // 열 검사
            for (r in 0..8) {
                if (r != emptyRow && board[r][emptyCol] == value) return@firstOrNull false
            }
            
            // 3x3 박스 검사
            val boxRow = (emptyRow / 3) * 3
            val boxCol = (emptyCol / 3) * 3
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != emptyRow || c != emptyCol) && board[r][c] == value) return@firstOrNull false
                }
            }
            
            true
        }
        assertNotNull("넣을 수 있는 값이 있어야 함", validValue)
        
        // 게임 진행
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(validValue!!)
        
        state = viewModel.state.first()
        
        // 상태 일관성 확인
        assertEquals(emptyRow, state.selectedRow)
        assertEquals(emptyCol, state.selectedCol)
        assertEquals(validValue, state.board[emptyRow][emptyCol])
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
                if (viewModel.isInitialCell(row, col)) {
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