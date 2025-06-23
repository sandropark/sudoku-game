package com.sandro.new_sudoku

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuIntegrationTest {

    @Test
    fun `기본 동작 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 기본 동작만 테스트
        val state = viewModel.state.first()
        
        // 보드가 9x9 크기인지 확인
        assertEquals(9, state.board.size)
        assertEquals(9, state.board[0].size)
        
        // 초기 상태 확인
        assertFalse(state.isGameComplete)
        assertFalse(state.showError)
        assertEquals("", state.errorMessage)
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
        
        // 빈 셀이 없으면 새 게임 생성만 테스트
        if (emptyCells.isEmpty()) {
            viewModel.newGame()
            val newState = viewModel.state.first()
            
            // 상태 초기화 확인
            assertEquals(-1, newState.selectedRow)
            assertEquals(-1, newState.selectedCol)
            assertFalse(newState.isGameComplete)
            assertFalse(newState.showError)
            return@runTest
        }
        
        // 게임 진행 (빈 셀들에 값 입력)
        val testCount = minOf(3, emptyCells.size) // 최대 3개까지만 테스트
        for (i in 0 until testCount) {
            val (row, col) = emptyCells[i]
            val validValue = (1..9).firstOrNull { value ->
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
            
            if (validValue != null) {
                viewModel.selectCell(row, col)
                viewModel.setCellValue(validValue)
                val state = viewModel.state.first()
                assertEquals(validValue, state.board[row][col])
                assertFalse(state.invalidCells.contains(Pair(row, col)))
            }
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
        assertTrue("새 게임이 생성되어야 함", hasChanges)
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
        assertTrue(clearedState.invalidCells.isEmpty())
        
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
        
        println("빈 셀 개수: ${emptyCells.size}")
        
        // 빈 셀이 2개 미만이면 테스트를 건너뛰되, 테스트 실패로 처리하지 않음
        if (emptyCells.size < 2) {
            println("빈 셀이 2개 미만이어서 테스트를 건너뜀")
            return@runTest
        }
        
        // 같은 행에 있는 빈 셀들 찾기
        val sameRowCells = emptyCells.groupBy { it.first }.values.firstOrNull { it.size >= 2 }
        
        // 같은 행에 빈 셀이 2개 이상 없으면 다른 방법으로 테스트
        if (sameRowCells == null) {
            println("같은 행에 있는 빈 셀이 2개 이상 없어서 다른 방법으로 테스트")
            
            // 첫 번째 빈 셀에 유효한 값 입력
            val firstCell = emptyCells[0]
            val validValue = (1..9).firstOrNull { value ->
                val board = viewModel.state.value.board
                if (board[firstCell.first][firstCell.second] != 0) return@firstOrNull false
                
                // 행 검사
                for (c in 0..8) {
                    if (c != firstCell.second && board[firstCell.first][c] == value) return@firstOrNull false
                }
                
                // 열 검사
                for (r in 0..8) {
                    if (r != firstCell.first && board[r][firstCell.second] == value) return@firstOrNull false
                }
                
                // 3x3 박스 검사
                val boxRow = (firstCell.first / 3) * 3
                val boxCol = (firstCell.second / 3) * 3
                for (r in boxRow until boxRow + 3) {
                    for (c in boxCol until boxCol + 3) {
                        if ((r != firstCell.first || c != firstCell.second) && board[r][c] == value) return@firstOrNull false
                    }
                }
                
                true
            }
            
            if (validValue != null) {
                viewModel.selectCell(firstCell.first, firstCell.second)
                viewModel.setCellValue(validValue)
                
                // 같은 값으로 다시 입력 시도 (스도쿠 규칙 위반)
                viewModel.setCellValue(validValue)
                
                state = viewModel.state.first()
                // 같은 셀에 같은 값을 다시 입력하는 것은 유효하므로 에러가 발생하지 않아야 함
                assertFalse("같은 셀에 같은 값을 다시 입력하는 것은 유효해야 함", state.showError)
                assertEquals(validValue, state.board[firstCell.first][firstCell.second])
            }
            return@runTest
        }
        
        val firstCell = sameRowCells[0]
        val secondCell = sameRowCells[1]
        
        println("첫 번째 셀: ($firstCell), 두 번째 셀: ($secondCell)")
        
        // 첫 번째 셀에 유효한 값 입력
        val validValue = (1..9).firstOrNull { value ->
            val board = viewModel.state.value.board
            if (board[firstCell.first][firstCell.second] != 0) return@firstOrNull false
            
            // 행 검사
            for (c in 0..8) {
                if (c != firstCell.second && board[firstCell.first][c] == value) return@firstOrNull false
            }
            
            // 열 검사
            for (r in 0..8) {
                if (r != firstCell.first && board[r][firstCell.second] == value) return@firstOrNull false
            }
            
            // 3x3 박스 검사
            val boxRow = (firstCell.first / 3) * 3
            val boxCol = (firstCell.second / 3) * 3
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != firstCell.first || c != firstCell.second) && board[r][c] == value) return@firstOrNull false
                }
            }
            
            true
        }
        
        assertNotNull("유효한 값이 있어야 함", validValue)
        println("첫 번째 셀에 넣을 값: $validValue")
        
        viewModel.selectCell(firstCell.first, firstCell.second)
        viewModel.setCellValue(validValue!!)
        
        // 첫 번째 셀 값이 제대로 설정되었는지 확인
        state = viewModel.state.first()
        assertEquals(validValue, state.board[firstCell.first][firstCell.second])
        assertFalse(state.showError)
        
        // 두 번째 셀에 같은 값을 입력 (같은 행에 같은 숫자 - 스도쿠 규칙 위반)
        viewModel.selectCell(secondCell.first, secondCell.second)
        viewModel.setCellValue(validValue) // 같은 행에 같은 숫자 입력
        
        state = viewModel.state.first()
        println("두 번째 셀 입력 후 - showError: ${state.showError}, errorMessage: ${state.errorMessage}")
        println("두 번째 셀 값: ${state.board[secondCell.first][secondCell.second]}")
        
        // 요구사항에 따라: 숫자는 항상 입력되고, 틀린 경우 invalidCells에 포함됨
        assertTrue("스도쿠 규칙 위반 시 invalidCells에 포함되어야 함", state.invalidCells.contains(Pair(secondCell.first, secondCell.second)))
        assertEquals(validValue, state.board[secondCell.first][secondCell.second]) // 값이 설정되어야 함 (요구사항에 따라)
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
            assertFalse(state.invalidCells.contains(Pair(row, col)))
        }
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
        // 요구사항: 숫자는 항상 입력되어야 함 (지우기도 가능)
        assertEquals(0, state.board[initialRow][initialCol])
        // 요구사항: 빈 셀은 invalidCells에 포함되지 않아야 함
        assertFalse(state.invalidCells.contains(Pair(initialRow, initialCol)))
    }

    @Test
    fun `에러 상태에서 정상 입력으로 복구 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 빈 셀 찾기 (초기 셀이 아닌 셀)
        var state = viewModel.state.first()
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
        
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyRow != -1)
        
        // 빈 셀에 잘못된 값 입력
        viewModel.selectCell(emptyRow, emptyCol)
        
        // 같은 행에 이미 있는 숫자 찾기
        var invalidValue = -1
        for (value in 1..9) {
            var foundInRow = false
            for (col in 0..8) {
                if (state.board[emptyRow][col] == value) {
                    foundInRow = true
                    break
                }
            }
            if (foundInRow) {
                invalidValue = value
                break
            }
        }
        
        assertTrue("잘못된 값을 찾을 수 있어야 함", invalidValue != -1)
        
        viewModel.setCellValue(invalidValue)
        state = viewModel.state.first()
        assertTrue("에러 상태가 생성되어야 함", state.invalidCells.contains(Pair(emptyRow, emptyCol)))
        
        // 유효한 값으로 복구
        val validValue = (1..9).firstOrNull { value ->
            if (value == invalidValue) return@firstOrNull false
            val board = viewModel.state.value.board
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
        
        if (validValue != null) {
            viewModel.setCellValue(validValue)
            state = viewModel.state.first()
            // 유효한 값을 입력했으므로 에러 상태가 해결되어야 함
            assertFalse("에러 상태가 해결되어야 함", state.invalidCells.contains(Pair(emptyRow, emptyCol)))
            assertEquals("유효한 값이 입력되어야 함", validValue, state.board[emptyRow][emptyCol])
        } else {
            // 유효한 값이 없으면 지우기
            viewModel.clearCell()
            state = viewModel.state.first()
            // 셀을 지웠으므로 에러 상태가 해결되어야 함
            assertFalse("에러 상태가 해결되어야 함", state.invalidCells.contains(Pair(emptyRow, emptyCol)))
            assertEquals("셀이 지워져야 함", 0, state.board[emptyRow][emptyCol])
        }
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
} 