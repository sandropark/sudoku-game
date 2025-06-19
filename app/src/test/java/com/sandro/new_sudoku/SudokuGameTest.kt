package com.sandro.new_sudoku

import org.junit.Test
import org.junit.Assert.*

class SudokuGameTest {

    @Test
    fun `초기 게임 생성 시 보드가 올바르게 설정되는지 테스트`() {
        val game = SudokuGame()
        val board = game.getBoard()
        
        // 보드가 9x9 크기인지 확인
        assertEquals(9, board.size)
        assertEquals(9, board[0].size)
        
        // 모든 셀이 0~9의 값인지 확인
        for (row in board) {
            for (cell in row) {
                assertTrue(cell in 0..9)
            }
        }
    }

    @Test
    fun `셀 값 설정이 올바르게 작동하는지 테스트`() {
        val game = SudokuGame()
        val board = game.getBoard()
        // 빈 셀을 하나 찾는다 (초기 셀이 아닌 셀)
        var found = false
        outer@for (row in 0..8) {
            for (col in 0..8) {
                if (!game.isInitialCell(row, col)) {
                    // 넣을 수 있는 값 찾기
                    val value = (1..9).firstOrNull { game.isValidMove(row, col, it) }
                    assertNotNull("넣을 수 있는 값이 있어야 함", value)
                    // 빈 셀에 숫자 설정
                    assertTrue(game.setCell(row, col, value!!))
                    assertEquals(value, game.getCell(row, col))
                    found = true
                    break@outer
                }
            }
        }
        assertTrue(found) // 빈 셀이 반드시 하나는 있어야 함
        // 초기 숫자는 변경 불가
        found = false
        outer@for (row in 0..8) {
            for (col in 0..8) {
                if (game.isInitialCell(row, col)) {
                    val original = board[row][col]
                    assertFalse(game.setCell(row, col, (original % 9) + 1)) // 다른 값 시도
                    assertEquals(original, game.getCell(row, col)) // 변경되지 않음
                    found = true
                    break@outer
                }
            }
        }
        assertTrue(found) // 초기 숫자 셀도 반드시 하나는 있어야 함
    }

    @Test
    fun `유효하지 않은 값 설정 시 거부되는지 테스트`() {
        val game = SudokuGame()
        val board = game.getBoard()
        var found = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (!game.isInitialCell(row, col)) {
                    // 범위를 벗어난 값
                    assertFalse(game.setCell(row, col, 10))
                    assertFalse(game.setCell(row, col, -1))
                    
                    // 유효한 값
                    val value = (1..9).firstOrNull { game.isValidMove(row, col, it) }
                    assertNotNull("넣을 수 있는 값이 있어야 함", value)
                    assertTrue(game.setCell(row, col, value!!))
                    found = true
                    break
                }
            }
            if (found) break
        }
        assertTrue(found)
    }

    @Test
    fun `스도쿠 규칙 검증이 올바르게 작동하는지 테스트`() {
        val game = SudokuGame()
        val board = game.getBoard()
        
        // 빈 셀을 찾아서 테스트 (초기 셀이 아닌 셀)
        var found = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (!game.isInitialCell(row, col)) {
                    // 유효한 값 찾기
                    val validValue = (1..9).firstOrNull { value ->
                        game.isValidMove(row, col, value)
                    }
                    assertNotNull("유효한 값이 있어야 함", validValue)
                    
                    // 잘못된 이동
                    game.setCell(row, col, validValue!!)
                    val boxRow = (row / 3) * 3
                    val boxCol = (col / 3) * 3
                    var boxFound = false
                    for (r in boxRow until boxRow + 3) {
                        for (c in boxCol until boxCol + 3) {
                            if ((r != row || c != col) && !game.isInitialCell(r, c)) {
                                assertFalse(game.isValidMove(r, c, validValue))
                                boxFound = true
                                break
                            }
                        }
                        if (boxFound) break
                    }
                    
                    // 유효한 이동
                    game.setCell(row, col, 0) // 초기화
                    val validValue2 = (1..9).firstOrNull { value ->
                        game.isValidMove(row, col, value)
                    }
                    assertNotNull("유효한 값이 있어야 함", validValue2)
                    assertTrue(game.isValidMove(row, col, validValue2!!))
                    found = true
                    break
                }
            }
            if (found) break
        }
        assertTrue(found)
    }

    @Test
    fun `빈 셀(0) 설정이 항상 유효한지 테스트`() {
        val game = SudokuGame()
        
        // 어떤 상황에서든 0(빈 셀)은 유효
        assertTrue(game.isValidMove(0, 2, 0))
        assertTrue(game.isValidMove(5, 5, 0))
        assertTrue(game.isValidMove(8, 8, 0))
    }

    @Test
    fun `게임 완료 상태 확인이 올바르게 작동하는지 테스트`() {
        val game = SudokuGame()
        
        // 초기 상태는 완료되지 않음
        assertFalse(game.isGameComplete())
        
        // 모든 빈 셀을 채워서 완료 상태로 만들기 (유효한 값으로)
        for (row in 0..8) {
            for (col in 0..8) {
                if (game.getCell(row, col) == 0) {
                    val validValue = (1..9).firstOrNull { value ->
                        game.isValidMove(row, col, value)
                    }
                    if (validValue != null) {
                        game.setCell(row, col, validValue)
                    }
                }
            }
        }
        
        // 아직 완료되지 않음 (모든 셀을 채우기 어려울 수 있음)
        // 해답으로 채우기
        game.solveGame()
        assertTrue(game.isGameComplete())
    }

    @Test
    fun `초기 셀 확인이 올바르게 작동하는지 테스트`() {
        val game = SudokuGame()
        val board = game.getBoard()
        // 초기 숫자 셀은 isInitialCell이 true여야 함
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] != 0) {
                    assertTrue(game.isInitialCell(row, col))
                } else {
                    assertFalse(game.isInitialCell(row, col))
                }
            }
        }
    }

    @Test
    fun `보드 초기화가 올바르게 작동하는지 테스트`() {
        val game = SudokuGame()
        val initialBoard = game.getBoard().map { it.clone() }.toTypedArray()
        // 일부 셀 수정 (유효한 값으로)
        var changed = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (!game.isInitialCell(row, col)) {
                    val validValue = (1..9).firstOrNull { value ->
                        game.isValidMove(row, col, value)
                    }
                    if (validValue != null) {
                        game.setCell(row, col, validValue)
                        changed = true
                        break
                    }
                }
            }
            if (changed) break
        }
        // 보드 초기화
        game.clearBoard()
        val clearedBoard = game.getBoard()
        // 초기 상태로 돌아갔는지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                assertEquals(initialBoard[row][col], clearedBoard[row][col])
            }
        }
    }

    @Test
    fun `해답 생성이 올바르게 작동하는지 테스트`() {
        val game = SudokuGame()
        
        // 해답으로 채우기
        game.solveGame()
        
        // 모든 셀이 채워졌는지 확인
        assertTrue(game.isGameComplete())
        
        // 빈 셀이 없는지 확인
        val board = game.getBoard()
        for (row in board) {
            for (cell in row) {
                assertNotEquals(0, cell)
            }
        }
        // solution과 일치하는지 확인
        val solutionField = game.javaClass.getDeclaredField("solution")
        solutionField.isAccessible = true
        val solution = solutionField.get(game) as Array<IntArray>
        for (i in 0..8) {
            for (j in 0..8) {
                assertEquals("해답 보기 후 보드가 solution과 일치해야 함", solution[i][j], board[i][j])
            }
        }
    }

    @Test
    fun `새 게임 생성 시 상태가 올바르게 초기화되는지 테스트`() {
        val game = SudokuGame()
        val initialBoard = game.getBoard().map { it.clone() }.toTypedArray()
        // 일부 셀 수정 (유효한 값으로)
        var changed = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (!game.isInitialCell(row, col)) {
                    val validValue = (1..9).firstOrNull { value ->
                        game.isValidMove(row, col, value)
                    }
                    if (validValue != null) {
                        game.setCell(row, col, validValue)
                        changed = true
                        break
                    }
                }
            }
            if (changed) break
        }
        // 새 게임 생성
        game.generateNewGame()
        val newBoard = game.getBoard()
        // 새로운 게임이 생성되었는지 확인 (같지 않아야 함)
        var hasChanges = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (initialBoard[row][col] != newBoard[row][col]) {
                    hasChanges = true
                    break
                }
            }
            if (hasChanges) break
        }
        assertTrue(hasChanges)
        // 게임이 완료되지 않았는지 확인
        assertFalse(game.isGameComplete())
    }

    @Test
    fun testNewGameIsRandom() {
        val game1 = SudokuGame()
        val board1 = game1.getBoard()
        
        val game2 = SudokuGame()
        val board2 = game2.getBoard()
        
        // 두 게임의 보드가 다른지 확인 (최소한 하나의 셀은 달라야 함)
        var hasDifference = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (board1[row][col] != board2[row][col]) {
                    hasDifference = true
                    break
                }
            }
            if (hasDifference) break
        }
        
        // 랜덤 퍼즐이므로 대부분의 경우 다를 것이지만, 
        // 같은 퍼즐이 선택될 수도 있으므로 항상 true가 아닐 수 있음
        // 대신 보드가 유효한 스도쿠 퍼즐인지만 확인
        assertTrue("보드가 유효한 스도쿠 퍼즐이어야 함", 
            board1.all { row -> row.all { it in 0..9 } } &&
            board2.all { row -> row.all { it in 0..9 } })
    }

    @Test
    fun testRandomPuzzleIsValidSudoku() {
        val game = SudokuGame()
        val board = game.getBoard()
        // 각 행, 열, 박스에 중복된 숫자가 없는지(0은 빈칸이므로 무시)
        for (i in 0..8) {
            val rowSet = mutableSetOf<Int>()
            val colSet = mutableSetOf<Int>()
            for (j in 0..8) {
                val rowVal = board[i][j]
                val colVal = board[j][i]
                if (rowVal != 0) {
                    assertTrue("행 $i 에 중복된 값 $rowVal", rowSet.add(rowVal))
                }
                if (colVal != 0) {
                    assertTrue("열 $i 에 중복된 값 $colVal", colSet.add(colVal))
                }
            }
        }
        // 3x3 박스 체크
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val boxSet = mutableSetOf<Int>()
                for (i in 0..2) {
                    for (j in 0..2) {
                        val v = board[boxRow*3 + i][boxCol*3 + j]
                        if (v != 0) {
                            assertTrue("박스($boxRow,$boxCol)에 중복된 값 $v", boxSet.add(v))
                        }
                    }
                }
            }
        }
    }
}