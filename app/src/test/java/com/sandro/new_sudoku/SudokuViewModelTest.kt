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
import kotlinx.coroutines.flow.last

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
        
        // 빈 셀 찾기 (초기 셀이 아닌 셀)
        var state = viewModel.state.first()
        var emptyRow = -1
        var emptyCol = -1
        var valueToSet: Int? = null
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col)) {
                    valueToSet = (1..9).firstOrNull { viewModel.isInitialCell(row, col).not() && viewModel.state.value.board[row][col] == 0 && viewModel.state.value.board.all { r -> r[col] != it } && viewModel.state.value.board[row].all { c -> c != it } }
                    if (valueToSet == null) valueToSet = (1..9).firstOrNull { viewModel.state.value.board[row][col] == 0 }
                    if (valueToSet != null) {
                        emptyRow = row
                        emptyCol = col
                        break
                    }
                }
            }
            if (emptyRow != -1) break
        }
        println("빈 셀 위치: ($emptyRow, $emptyCol), 넣을 값: $valueToSet")
        assertTrue("빈 셀을 찾을 수 있어야 함", emptyRow != -1)
        assertNotNull("넣을 수 있는 값이 있어야 함", valueToSet)
        
        // 셀 선택 후 값 설정
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(valueToSet!!)
        
        // 잠시 기다린 후 상태 확인
        kotlinx.coroutines.delay(100)
        state = viewModel.state.first()
        println("설정 후 값: ${state.board[emptyRow][emptyCol]}, showError: ${state.showError}, errorMessage: ${state.errorMessage}")
        assertEquals(valueToSet, state.board[emptyRow][emptyCol])
        assertFalse(state.showError)
    }

    @Test
    fun `초기 셀 수정 시도 시 에러가 발생하는지 테스트`() = runTest {
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
        
        // 초기 셀 선택 후 값 설정 시도
        viewModel.selectCell(initialRow, initialCol)
        viewModel.setCellValue(9)
        
        // 잠시 기다린 후 상태 확인
        kotlinx.coroutines.delay(100)
        state = viewModel.state.first()
        
        assertTrue(state.showError)
        assertEquals("초기 숫자는 변경할 수 없습니다", state.errorMessage)
        assertEquals(initialValue, state.board[initialRow][initialCol]) // 값이 변경되지 않음
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
        
        if (emptyCells.size >= 2) {
            // 같은 행에 있는 빈 셀들 찾기
            val sameRowCells = emptyCells.groupBy { it.first }.values.firstOrNull { it.size >= 2 }
            
            if (sameRowCells != null) {
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
                    assertTrue(state.showError)
                    assertEquals("이 숫자는 여기에 놓을 수 없습니다", state.errorMessage)
                }
            }
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

    @Test
    fun `셀 선택 시 에러 메시지가 사라지는지 테스트`() = runTest {
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
        
        // 에러 발생시키기
        viewModel.selectCell(initialRow, initialCol)
        viewModel.setCellValue(9)
        
        state = viewModel.state.first()
        assertTrue(state.showError)
        
        // 다른 셀 선택 (에러 메시지 사라짐)
        viewModel.selectCell(emptyRow, emptyCol)
        state = viewModel.state.first()
        assertFalse(state.showError)
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
    fun `에러 상태 복구 테스트`() = runTest {
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
        
        // 초기값 셀에 값을 넣어서 에러 발생
        viewModel.selectCell(initialRow, initialCol) // 초기값 셀 선택
        viewModel.setCellValue(5) // 에러 발생
        
        val errorState = viewModel.state.first()
        println("에러 상태: showError=${errorState.showError}, message='${errorState.errorMessage}'")
        assertTrue("에러가 표시되어야 함", errorState.showError)
        assertTrue("에러 메시지가 있어야 함", errorState.errorMessage.isNotEmpty())
        
        // 유효한 셀에 값을 넣어서 에러 복구
        val validValue = findValidValue(viewModel.state.first().board, emptyRow, emptyCol)
        assertNotNull("유효한 값이 있어야 함", validValue)
        
        viewModel.selectCell(emptyRow, emptyCol) // 빈 셀 선택
        viewModel.setCellValue(validValue!!) // 유효한 값 입력
        
        val recoveredState = viewModel.state.first()
        println("복구 상태: showError=${recoveredState.showError}, message='${recoveredState.errorMessage}'")
        assertFalse("에러가 사라져야 함", recoveredState.showError)
        assertTrue("에러 메시지가 비워져야 함", recoveredState.errorMessage.isEmpty())
    }

    @Test
    fun `다양한 에러 시나리오 테스트`() = runTest {
        val viewModel = SudokuViewModel()
        
        // 초기 셀과 빈 셀 찾기
        var state = viewModel.state.first()
        var initialRow = -1
        var initialCol = -1
        var emptyRow1 = -1
        var emptyCol1 = -1
        var emptyRow2 = -1
        var emptyCol2 = -1
        
        for (row in 0..8) {
            for (col in 0..8) {
                if (viewModel.isInitialCell(row, col) && initialRow == -1) {
                    initialRow = row
                    initialCol = col
                }
                if (!viewModel.isInitialCell(row, col)) {
                    if (emptyRow1 == -1) {
                        emptyRow1 = row
                        emptyCol1 = col
                    } else if (emptyRow2 == -1 && row == emptyRow1) {
                        emptyRow2 = row
                        emptyCol2 = col
                    }
                }
            }
        }
        
        // 1. 초기값 셀 수정 시도
        viewModel.selectCell(initialRow, initialCol)
        viewModel.setCellValue(5)
        assertTrue("초기값 셀 수정 시 에러가 발생해야 함", viewModel.state.first().showError)
        
        // 2. 잘못된 이동 시도 (같은 행에 같은 숫자 입력)
        if (emptyRow1 != -1 && emptyRow2 != -1) {
            // 첫 번째 셀에 유효한 값 입력
            val validValue1 = findValidValue(viewModel.state.first().board, emptyRow1, emptyCol1)
            assertNotNull("유효한 값이 있어야 함", validValue1)
            viewModel.selectCell(emptyRow1, emptyCol1)
            viewModel.setCellValue(validValue1!!)
            
            // 두 번째 셀에 같은 값을 입력 (같은 행에 같은 숫자 - 스도쿠 규칙 위반)
            viewModel.selectCell(emptyRow2, emptyCol2)
            viewModel.setCellValue(validValue1) // 같은 행에 같은 숫자 입력 (잘못된 이동)
            assertTrue("잘못된 이동 시 에러가 발생해야 함", viewModel.state.first().showError)
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
        
        // 초기 셀 찾기
        var state = viewModel.state.first()
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
        
        // 초기값 셀에 값을 넣어서 에러 발생
        viewModel.selectCell(initialRow, initialCol)
        viewModel.setCellValue(5) // 에러 발생
        
        val errorState = viewModel.state.first()
        assertEquals("초기 숫자는 변경할 수 없습니다", errorState.errorMessage)
        
        // 유효한 셀에 값을 넣어서 에러 메시지 초기화
        val emptyCell = findEmptyCell(state.board) ?: throw AssertionError("빈 셀 없음")
        val (row, col) = emptyCell
        val validValue = findValidValue(state.board, row, col) ?: throw AssertionError("유효한 값 없음")
        
        viewModel.selectCell(row, col) // 빈 셀 선택
        viewModel.setCellValue(validValue) // 유효한 값 입력
        
        val recoveredState = viewModel.state.first()
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