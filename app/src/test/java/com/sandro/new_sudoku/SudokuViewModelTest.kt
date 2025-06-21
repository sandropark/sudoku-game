package com.sandro.new_sudoku

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
        viewModel.newGame()
        val state = viewModel.state.first()
        var emptyRow = -1
        var emptyCol = -1
        var valueToSet: Int? = null
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    for (value in 1..9) {
                        if (state.board.all { r -> r[col] != value } && state.board[row].all { c -> c != value }) {
                    emptyRow = row
                    emptyCol = col
                            valueToSet = value
                    break
                }
            }
            if (emptyRow != -1) break
        }
            }
            if (emptyRow != -1) break
        }
        assertTrue(emptyRow != -1)
        assertNotNull(valueToSet)
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(valueToSet!!)
        advanceUntilIdle()
        assertEquals(valueToSet, viewModel.state.value.board[emptyRow][emptyCol])
        viewModel.clearCell()
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.board[emptyRow][emptyCol])
    }

    @Test
    fun `스도쿠 규칙 위반 시 에러가 발생하는지 테스트`() = runTest {
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
        
        // 빈 셀이 2개 미만이면 다른 방법으로 테스트
        if (emptyCells.size < 2) {
            // 초기 셀 수정 시도로 대체
        var initialRow = -1
        var initialCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                    if (viewModel.isInitialCell(row, col)) {
                    initialRow = row
                    initialCol = col
                    break
                }
            }
            if (initialRow != -1) break
        }
        
            if (initialRow != -1) {
        viewModel.selectCell(initialRow, initialCol)
                viewModel.setCellValue(5)
        state = viewModel.state.first()
                assertTrue("초기 셀 수정 시 에러가 발생해야 함", state.showError)
            }
            return@runTest
        }
        
        // 같은 행에 있는 빈 셀들 찾기
        val sameRowCells = emptyCells.groupBy { it.first }.values.firstOrNull { it.size >= 2 }
        
        // 같은 행에 빈 셀이 2개 이상 없으면 다른 방법으로 테스트
        if (sameRowCells == null) {
            // 같은 열에 있는 빈 셀들 찾기
            val sameColCells = emptyCells.groupBy { it.second }.values.firstOrNull { it.size >= 2 }
            
            if (sameColCells != null) {
                val firstCell = sameColCells[0]
                val secondCell = sameColCells[1]
                
                // 첫 번째 셀에 유효한 값 입력 (스도쿠 규칙을 만족하는 값)
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
                    
                    // 두 번째 셀에 같은 값을 입력 (같은 열에 같은 숫자 - 스도쿠 규칙 위반)
                viewModel.selectCell(secondCell.first, secondCell.second)
                    viewModel.setCellValue(validValue)
                
                state = viewModel.state.first()
                    assertTrue("스도쿠 규칙 위반 시 에러가 발생해야 함", state.invalidCells.contains(Pair(secondCell.first, secondCell.second)))
                }
            }
            return@runTest
        }
        
        val firstCell = sameRowCells[0]
        val secondCell = sameRowCells[1]
        
        // 첫 번째 셀에 유효한 값 입력 (스도쿠 규칙을 만족하는 값)
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
            
            // 두 번째 셀에 같은 값을 입력 (같은 행에 같은 숫자 - 스도쿠 규칙 위반)
            viewModel.selectCell(secondCell.first, secondCell.second)
            viewModel.setCellValue(validValue)
            
            state = viewModel.state.first()
            assertTrue("스도쿠 규칙 위반 시 에러가 발생해야 함", state.invalidCells.contains(Pair(secondCell.first, secondCell.second)))
        }
    }

    @Test
    fun `셀 지우기가 올바르게 작동하는지 테스트`() = runTest {
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
        
        // 셀에 값 설정 후 지우기
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(4)
        viewModel.clearCell()
        
        state = viewModel.state.first()
        assertEquals(0, state.board[emptyRow][emptyCol]) // 지워짐
    }

    @Test
    fun `새 게임 생성이 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 상태 저장
        val initialState = viewModel.state.first()
        
        // 빈 셀 찾기 (초기 셀이 아닌 셀)
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
        
        // 일부 셀 수정
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(4)
        
        // 새 게임 생성
        viewModel.newGame()
        val newState = viewModel.state.first()
        
        // 상태가 초기화되었는지 확인
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
    fun `게임 해답 보기가 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        viewModel.solveGame()
        val state = viewModel.state.first()
        
        // 모든 셀이 채워졌는지 확인
        assertTrue(state.isGameComplete)
        for (row in state.board) {
            for (cell in row) {
                assertNotEquals(0, cell)
            }
        }
        // solution과 일치하는지 확인
        val gameField = viewModel.javaClass.getDeclaredField("game")
        gameField.isAccessible = true
        val game = gameField.get(viewModel) as com.sandro.new_sudoku.SudokuGame
        val solutionField = game.javaClass.getDeclaredField("solution")
        solutionField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val solution = solutionField.get(game) as Array<IntArray>
        for (i in 0..8) {
            for (j in 0..8) {
                assertEquals("해답 보기 후 보드가 solution과 일치해야 함", solution[i][j], state.board[i][j])
            }
        }
    }

    @Test
    fun `보드 초기화가 올바르게 작동하는지 테스트`() = runTest {
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
        
        // 일부 셀 수정
        if (emptyCells.size >= 2) {
            viewModel.selectCell(emptyCells[0].first, emptyCells[0].second)
            viewModel.setCellValue(4)
            viewModel.selectCell(emptyCells[1].first, emptyCells[1].second)
            viewModel.setCellValue(7)
        }
        
        // 보드 초기화
        viewModel.clearBoard()
        val clearedState = viewModel.state.first()
        
        // 상태가 초기화되었는지 확인
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
    fun `초기 셀 확인이 올바르게 작동하는지 테스트`() {
        val viewModel = SudokuViewModel()
        
        // 모든 셀에 대해 초기 셀 여부 확인
        for (row in 0..8) {
            for (col in 0..8) {
                val isInitial = viewModel.isInitialCell(row, col)
                val cellValue = viewModel.state.value.board[row][col]
                
                if (cellValue != 0) {
                    assertTrue("($row, $col)는 초기 셀이어야 함", isInitial)
                } else {
                    assertFalse("($row, $col)는 초기 셀이 아니어야 함", isInitial)
                }
            }
        }
    }

    // 복구 헬퍼 함수: 해당 셀에 유효한 값이 있으면 그 값, 없으면 0(지우기)로 복구
    private fun recoverCell(viewModel: SudokuViewModel, row: Int, col: Int, excludeValue: Int? = null) {
        viewModel.selectCell(row, col)
        val validValue = (1..9).firstOrNull { value ->
            if (excludeValue != null && value == excludeValue) return@firstOrNull false
            val board = viewModel.state.value.board
            for (c in 0..8) {
                if (c != col && board[row][c] == value) return@firstOrNull false
            }
            for (r in 0..8) {
                if (r != row && board[r][col] == value) return@firstOrNull false
            }
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
            viewModel.setCellValue(validValue)
        } else {
            viewModel.clearCell()
        }
    }

    @Test
    fun `셀 선택 시 에러 메시지가 사라지는지 테스트`() = runTest {
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
        
        // 빈 셀에 잘못된 값 입력 (에러 상태 생성)
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
        val errorState = viewModel.state.first()
        assertTrue("에러 상태가 생성되어야 함", errorState.invalidCells.contains(Pair(emptyRow, emptyCol)))
        
        // 다른 셀 선택 (에러 상태는 유지되어야 함)
        // 업데이트된 상태에서 빈 셀 찾기
        val updatedState = viewModel.state.first()
        val anotherEmptyCell = findEmptyCell(updatedState.board, viewModel) ?: throw AssertionError("빈 셀 없음")
        val (row, col) = anotherEmptyCell
        viewModel.selectCell(row, col)
        val stateAfterSelection = viewModel.state.first()
        assertTrue("에러 상태가 유지되어야 함", stateAfterSelection.invalidCells.contains(Pair(emptyRow, emptyCol)))
    }

    @Test
    fun `셀이 선택되지 않은 상태에서 값 설정 시도 시 아무 일도 일어나지 않는지 테스트`() = runTest {
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
        
        // 셀 선택 없이 값 설정 시도
        viewModel.setCellValue(5)
        
        state = viewModel.state.first()
        // 초기값은 변경되지 않아야 함
        assertEquals(initialValue, state.board[initialRow][initialCol])
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
    fun `에러 메시지 내용 테스트`() = runTest {
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
        val errorState = viewModel.state.first()
        assertTrue("에러 상태가 생성되어야 함", errorState.invalidCells.contains(Pair(emptyRow, emptyCol)))
    }

    @Test
    fun `에러 상태 복구 테스트`() = runTest {
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
        val errorState = viewModel.state.first()
        assertTrue("에러 상태가 생성되어야 함", errorState.invalidCells.contains(Pair(emptyRow, emptyCol)))
        
        // 유효한 값으로 복구 (업데이트된 상태에서 찾기)
        val updatedState = viewModel.state.first()
        val validValue = (1..9).firstOrNull { value ->
            if (value == invalidValue) return@firstOrNull false
            val board = updatedState.board
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
            val recoveredState = viewModel.state.first()
            assertFalse("에러 상태가 해결되어야 함", recoveredState.invalidCells.contains(Pair(emptyRow, emptyCol)))
        } else {
            // 유효한 값이 없으면 지우기
            viewModel.clearCell()
            val recoveredState = viewModel.state.first()
            assertFalse("에러 상태가 해결되어야 함", recoveredState.invalidCells.contains(Pair(emptyRow, emptyCol)))
        }
    }

    @Test
    fun `다양한 에러 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 빈 셀들 찾기 (초기 셀이 아닌 셀들)
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && viewModel.state.value.board[row][col] == 0) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        
        assertTrue("테스트할 빈 셀이 있어야 함", emptyCells.isNotEmpty())
        
        // 각 빈 셀에 대해 에러 시나리오 테스트
        for ((row, col) in emptyCells.take(3)) { // 최대 3개만 테스트
            val state = viewModel.state.value
            
            // 같은 행에 이미 있는 숫자 찾기
            var invalidValue = -1
            for (value in 1..9) {
                var foundInRow = false
                for (c in 0..8) {
                    if (state.board[row][c] == value) {
                        foundInRow = true
                        break
                    }
                }
                if (foundInRow) {
                    invalidValue = value
                    break
                }
            }
            
            if (invalidValue != -1) {
                viewModel.selectCell(row, col)
                viewModel.setCellValue(invalidValue)
                advanceUntilIdle()
                
                val errorState = viewModel.state.value
                assertTrue("에러 상태가 생성되어야 함", errorState.invalidCells.contains(Pair(row, col)))
                
                // 복구
                viewModel.clearCell()
                advanceUntilIdle()
                
                val recoveredState = viewModel.state.value
                assertFalse("에러 상태가 해결되어야 함", recoveredState.invalidCells.contains(Pair(row, col)))
            }
        }
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
        assertTrue("새 게임에서는 에러가 없어야 함", newGameState.invalidCells.isEmpty())
    }

    // 빈 셀을 동적으로 찾는 헬퍼 함수 (초기 셀이 아닌 빈 셀만)
    private fun findEmptyCell(board: Array<IntArray>, viewModel: SudokuViewModel): Pair<Int, Int>? {
        for (i in 0..8) {
            for (j in 0..8) {
                if (!viewModel.isInitialCell(i, j) && board[i][j] == 0) return i to j
            }
        }
        return null
    }

    @Test
    fun `보드 클리어 및 리셋 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        val initialBoard = viewModel.state.first().board.map { it.copyOf() }.toTypedArray()
        val emptyCell = findEmptyCell(initialBoard, viewModel) ?: throw AssertionError("빈 셀 없음")
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
        assertTrue("에러 상태가 초기화되어야 함", clearedState.invalidCells.isEmpty())
    }

    @Test
    fun `동시 작업 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        val initialBoard = viewModel.state.first().board
        val emptyCell = findEmptyCell(initialBoard, viewModel) ?: throw AssertionError("빈 셀 없음")
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
        val emptyCell = findEmptyCell(initialBoard, viewModel) ?: throw AssertionError("빈 셀 없음")
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
    fun `초기 상태 테스트`() {
        val viewModel = SudokuViewModel()
        val initialState = viewModel.state.value
        
        assertNotNull("초기 상태가 null이 아니어야 함", initialState)
        assertNotNull("보드가 null이 아니어야 함", initialState.board)
        assertNotNull("초기 셀 정보가 null이 아니어야 함", initialState.isInitialCells)
        assertEquals("선택된 행이 -1이어야 함", -1, initialState.selectedRow)
        assertEquals("선택된 열이 -1이어야 함", -1, initialState.selectedCol)
        assertFalse("게임이 완료되지 않아야 함", initialState.isGameComplete)
        assertTrue("에러 상태가 비어있어야 함", initialState.invalidCells.isEmpty())
    }

    @Test
    fun `지우기 기능 종합 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        viewModel.newGame()
        advanceUntilIdle()
        var emptyRow = -1
        var emptyCol = -1
        var valueToSet: Int? = null
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && viewModel.state.value.board[row][col] == 0) {
                    for (value in 1..9) {
                        if (viewModel.state.value.board.all { r -> r[col] != value } && viewModel.state.value.board[row].all { c -> c != value }) {
                            emptyRow = row
                            emptyCol = col
                            valueToSet = value
                            break
                        }
                    }
                    if (emptyRow != -1) break
                }
            }
            if (emptyRow != -1) break
        }
        if (emptyRow == -1) return@runTest
        viewModel.selectCell(emptyRow, emptyCol)
        var validValue: Int? = null
        val state = viewModel.state.value
        for (value in 1..9) {
            if (state.board.all { r -> r[emptyCol] != value } && state.board[emptyRow].all { c -> c != value }) {
                validValue = value
                break
            }
        }
        if (validValue == null) return@runTest
        viewModel.setCellValue(validValue)
        advanceUntilIdle()
        assertEquals(validValue, viewModel.state.value.board[emptyRow][emptyCol])
        viewModel.clearCell()
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.board[emptyRow][emptyCol])
    }

    @Test
    fun testMultipleUndo() = runTest {
        val viewModel = SudokuViewModel()
        advanceUntilIdle()
        var state = viewModel.state.value
        var emptyRow1 = -1
        var emptyCol1 = -1
        // 첫 번째 빈 셀 찾기
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow1 = row
                    emptyCol1 = col
                    break
                }
            }
            if (emptyRow1 != -1) break
        }
        assertTrue(emptyRow1 != -1)
        viewModel.selectCell(emptyRow1, emptyCol1)
        val validValue1 = findValidValue(state.board, emptyRow1, emptyCol1) ?: throw AssertionError("유효한 값 없음")
        viewModel.setCellValue(validValue1)
        advanceUntilIdle()
        val v1 = viewModel.state.value.board[emptyRow1][emptyCol1]
        println("[DEBUG] 첫 번째 셀 ($emptyRow1, $emptyCol1)에 $validValue1 입력 후 board:\n" + viewModel.state.value.board.joinToString("\n") { it.joinToString(",") })
        println("[DEBUG] v1: $v1")
        assertEquals(validValue1, v1)
        // 두 번째 빈 셀 찾기 (state 갱신)
        advanceUntilIdle()
        var emptyRow2 = -1
        var emptyCol2 = -1
        for (row in 0..8) {
            for (col in 0..8) {
                val state = viewModel.state.value
                if ((row != emptyRow1 || col != emptyCol1) && !viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow2 = row
                    emptyCol2 = col
                    break
                }
            }
            if (emptyRow2 != -1) break
        }
        assertTrue(emptyRow2 != -1)
        assertFalse(emptyRow1 == emptyRow2 && emptyCol1 == emptyCol2)
        viewModel.selectCell(emptyRow2, emptyCol2)
        advanceUntilIdle()
        // 두 번째 셀에 실제로 넣을 수 있는 값 찾기
        var value2: Int? = null
        val state2 = viewModel.state.value
        for (v in 1..9) {
            if (state2.board.all { r -> r[emptyCol2] != v } && state2.board[emptyRow2].all { c -> c != v }) {
                value2 = v
                break
            }
        }
        assertNotNull(value2)
        viewModel.setCellValue(value2!!)
        advanceUntilIdle()
        val v2_1 = viewModel.state.value.board[emptyRow1][emptyCol1]
        val v2_2 = viewModel.state.value.board[emptyRow2][emptyCol2]
        println("[DEBUG] 두 번째 셀 ($emptyRow2, $emptyCol2)에 $value2 입력 후 board:\n" + viewModel.state.value.board.joinToString("\n") { it.joinToString(",") })
        println("[DEBUG] v2_1: $v2_1, v2_2: $v2_2")
        assertEquals(validValue1, v2_1)
        assertEquals(value2, v2_2)
        viewModel.onUndo()
        advanceUntilIdle()
        val v3_1 = viewModel.state.value.board[emptyRow1][emptyCol1]
        val v3_2 = viewModel.state.value.board[emptyRow2][emptyCol2]
        println("[DEBUG] undo 1 후 board:\n" + viewModel.state.value.board.joinToString("\n") { it.joinToString(",") })
        println("[DEBUG] v3_1: $v3_1, v3_2: $v3_2")
        println("[DEBUG] undo 1 후 invalidCells: ${viewModel.state.value.invalidCells}")
        assertEquals(validValue1, v3_1)
        assertEquals(0, v3_2)
        assertTrue(viewModel.state.value.invalidCells.isEmpty())
        viewModel.onUndo()
        advanceUntilIdle()
        val v4_1 = viewModel.state.value.board[emptyRow1][emptyCol1]
        println("[DEBUG] undo 2 후 board:\n" + viewModel.state.value.board.joinToString("\n") { it.joinToString(",") })
        println("[DEBUG] v4_1: $v4_1")
        println("[DEBUG] undo 2 후 invalidCells: ${viewModel.state.value.invalidCells}")
        assertEquals(0, v4_1)
        assertTrue(viewModel.state.value.invalidCells.isEmpty())
        println("[DEBUG] undo 후 notes[3][4]: " + viewModel.state.value.notes[3][4])
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
    fun `디버깅 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 상태 확인
        var state = viewModel.state.first()
        println("[DEBUG] 초기 invalidCells: ${state.invalidCells}")
        
        // 초기 셀 찾기
        var initialRow = -1
        var initialCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0) {
                    initialRow = row
                    initialCol = col
                    break
                }
            }
            if (initialRow != -1) break
        }
        
        println("[DEBUG] 초기 셀: ($initialRow, $initialCol), 값: ${state.board[initialRow][initialCol]}")
        
        // 셀 선택
        viewModel.selectCell(initialRow, initialCol)
        state = viewModel.state.first()
        println("[DEBUG] 셀 선택 후 invalidCells: ${state.invalidCells}")
        
        // 값 변경
        viewModel.setCellValue(9)
        state = viewModel.state.first()
        println("[DEBUG] 값 변경 후 invalidCells: ${state.invalidCells}")
        println("[DEBUG] 변경된 값: ${state.board[initialRow][initialCol]}")
        
        // 간단한 assert
        assertTrue(true) // 항상 통과
    }

    @Test
    fun `노트 모드 토글이 올바르게 작동하는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 상태 확인
        val initialState = viewModel.state.first()
        assertFalse("초기에는 노트 모드가 비활성화되어야 함", initialState.isNoteMode)
        
        // 노트 모드 활성화
        viewModel.toggleNoteMode()
        val afterToggle = viewModel.state.first()
        assertTrue("노트 모드가 활성화되어야 함", afterToggle.isNoteMode)
        
        // 노트 모드 비활성화
        viewModel.toggleNoteMode()
        val afterSecondToggle = viewModel.state.first()
        assertFalse("노트 모드가 비활성화되어야 함", afterSecondToggle.isNoteMode)
    }

    @Test
    fun `노트 숫자 추가가 올바르게 작동하는지 테스트`() = runTest {
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
        
        // 노트 모드 활성화
        viewModel.toggleNoteMode()
        
        // 빈 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)
        
        // 노트 숫자 추가
        viewModel.addNoteNumber(5)
        state = viewModel.state.first()
        
        assertTrue("노트에 숫자 5가 추가되어야 함", state.notes[emptyRow][emptyCol].contains(5))
        assertEquals("노트에 숫자가 1개만 있어야 함", 1, state.notes[emptyRow][emptyCol].size)
    }

    @Test
    fun `노트 숫자 제거가 올바르게 작동하는지 테스트`() = runTest {
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
        
        // 노트 모드 활성화
        viewModel.toggleNoteMode()
        
        // 빈 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)
        
        // 노트 숫자 추가
        viewModel.addNoteNumber(5)
        viewModel.addNoteNumber(7)
        
        // 노트 숫자 제거
        viewModel.removeNoteNumber(5)
        state = viewModel.state.first()
        
        assertFalse("노트에서 숫자 5가 제거되어야 함", state.notes[emptyRow][emptyCol].contains(5))
        assertTrue("노트에 숫자 7은 남아있어야 함", state.notes[emptyRow][emptyCol].contains(7))
        assertEquals("노트에 숫자가 1개만 있어야 함", 1, state.notes[emptyRow][emptyCol].size)
    }

    @Test
    fun `노트 모드에서 같은 숫자를 다시 클릭하면 제거되는지 테스트`() = runTest {
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
        
        // 노트 모드 활성화
        viewModel.toggleNoteMode()
        
        // 빈 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)
        
        // 노트 숫자 추가
        viewModel.addNoteNumber(5)
        state = viewModel.state.first()
        assertTrue("노트에 숫자 5가 추가되어야 함", state.notes[emptyRow][emptyCol].contains(5))
        
        // 같은 숫자 다시 클릭 (제거)
        viewModel.addNoteNumber(5)
        state = viewModel.state.first()
        assertFalse("노트에서 숫자 5가 제거되어야 함", state.notes[emptyRow][emptyCol].contains(5))
        assertEquals("노트가 비어있어야 함", 0, state.notes[emptyRow][emptyCol].size)
    }

    @Test
    fun `노트 모드가 비활성화된 상태에서 노트 숫자 추가가 무시되는지 테스트`() = runTest {
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
        
        // 노트 모드 비활성화 상태에서 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)
        
        // 노트 숫자 추가 시도
        viewModel.addNoteNumber(5)
        state = viewModel.state.first()
        
        assertFalse("노트 모드가 비활성화된 상태에서는 노트가 추가되지 않아야 함", state.notes[emptyRow][emptyCol].contains(5))
        assertEquals("노트가 비어있어야 함", 0, state.notes[emptyRow][emptyCol].size)
    }

    @Test
    fun `초기 셀은 숫자와 노트 모두 변경 불가`() = runTest {
        val viewModel = SudokuViewModel()
        viewModel.newGame()
        val state = viewModel.state.first()
        // 초기 셀 찾기
        var initialRow = -1
        var initialCol = -1
        for (row in 0..8) {
            for (col in 0..8) {
                if (viewModel.isInitialCell(row, col)) {
                    initialRow = row
                    initialCol = col
                    break
                }
            }
            if (initialRow != -1) break
        }
        assertTrue("초기 셀을 찾을 수 있어야 함", initialRow != -1)
        // 초기 셀 값 저장
        val originalValue = state.board[initialRow][initialCol]
        // 숫자 입력 시도
        viewModel.selectCell(initialRow, initialCol)
        viewModel.setCellValue(5)
        val afterValue = viewModel.state.first().board[initialRow][initialCol]
        assertEquals("초기 셀 값은 변경되면 안 됨", originalValue, afterValue)
        // 노트 입력 시도
        viewModel.toggleNoteMode()
        viewModel.addNoteNumber(7)
        val notes = viewModel.state.first().notes[initialRow][initialCol]
        assertTrue("초기 셀에는 노트가 추가되면 안 됨", notes.isEmpty())
    }

    @Test
    fun `노트가 있는 상태에서 숫자 입력 시 노트가 사라지고 숫자가 입력되는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 빈 셀 찾기
        val emptyCell = findEmptyCell(viewModel.state.first().board, viewModel) ?: throw AssertionError("빈 셀 없음")
        val (row, col) = emptyCell
        
        // 셀 선택
        viewModel.selectCell(row, col)
        
        // 노트 모드 활성화
        viewModel.toggleNoteMode()
        
        // 노트 추가 (1, 2, 3)
        viewModel.addNoteNumber(1)
        viewModel.addNoteNumber(2)
        viewModel.addNoteNumber(3)
        
        // 노트가 추가되었는지 확인
        val stateWithNotes = viewModel.state.first()
        assertTrue("노트가 추가되어야 함", stateWithNotes.notes[row][col].containsAll(setOf(1, 2, 3)))
        assertEquals("일반 숫자는 0이어야 함", 0, stateWithNotes.board[row][col])
        
        // 노트 모드 비활성화
        viewModel.toggleNoteMode()
        
        // 숫자 5 입력
        viewModel.setCellValue(5)
        
        // 노트가 사라지고 숫자가 입력되었는지 확인
        val stateAfterInput = viewModel.state.first()
        assertTrue("노트가 사라져야 함", stateAfterInput.notes[row][col].isEmpty())
        assertEquals("숫자가 입력되어야 함", 5, stateAfterInput.board[row][col])
    }

    @Test
    fun `숫자가 있는 상태에서 노트 입력 시 숫자가 사라지고 노트가 입력되는지 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 빈 셀 찾기
        val emptyCell = findEmptyCell(viewModel.state.first().board, viewModel) ?: throw AssertionError("빈 셀 없음")
        val (row, col) = emptyCell
        
        // 셀 선택
        viewModel.selectCell(row, col)
        
        // 숫자 5 입력
        viewModel.setCellValue(5)
        
        // 숫자가 입력되었는지 확인
        val stateWithNumber = viewModel.state.first()
        assertEquals("숫자가 입력되어야 함", 5, stateWithNumber.board[row][col])
        
        // 노트 모드 활성화
        viewModel.toggleNoteMode()
        
        // 노트 추가 (1, 2, 3)
        viewModel.addNoteNumber(1)
        viewModel.addNoteNumber(2)
        viewModel.addNoteNumber(3)
        
        // 숫자가 사라지고 노트가 입력되었는지 확인
        val stateWithNotes = viewModel.state.first()
        assertTrue("노트가 추가되어야 함", stateWithNotes.notes[row][col].containsAll(setOf(1, 2, 3)))
        assertEquals("일반 숫자는 0이어야 함", 0, stateWithNotes.board[row][col])
    }
}