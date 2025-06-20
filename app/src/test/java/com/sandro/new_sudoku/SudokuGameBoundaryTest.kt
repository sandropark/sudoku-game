package com.sandro.new_sudoku

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class SudokuGameBoundaryTest {

    @Test
    fun `최소값 0 설정이 올바르게 작동하는지 테스트`() {
        val game = SudokuGame()
        val board = game.getBoard()
        var found = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] != 0) continue
                assertTrue(game.setCell(row, col, 0))
                assertEquals(0, game.getCell(row, col))
                found = true
                break
            }
            if (found) break
        }
        assertTrue(found)
    }

    @Test
    fun `최대값 9 설정이 올바르게 작동하는지 테스트`() {
        val game = SudokuGame()
        val board = game.getBoard()
        var found = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] != 0) continue
                if (game.isValidMove(row, col, 9)) {
                    assertTrue(game.setCell(row, col, 9))
                    assertEquals(9, game.getCell(row, col))
                } else {
                    assertFalse(game.setCell(row, col, 9))
                }
                found = true
                break
            }
            if (found) break
        }
        assertTrue(found)
    }

    @Test
    fun `범위를 벗어난 값 설정 시 거부되는지 테스트`() {
        val game = SudokuGame()
        
        // 10 이상의 값
        assertFalse(game.setCell(0, 2, 10))
        assertFalse(game.setCell(0, 2, 15))
        assertFalse(game.setCell(0, 2, 100))
        
        // 음수 값
        assertFalse(game.setCell(0, 2, -1))
        assertFalse(game.setCell(0, 2, -5))
        assertFalse(game.setCell(0, 2, -10))
    }

    @Test
    fun `모든 셀에 모든 숫자 설정이 가능한지 테스트`() {
        val game = SudokuGame()
        // 모든 빈 셀에 1-9 숫자 설정 테스트
        for (row in 0..8) {
            for (col in 0..8) {
                if (game.getCell(row, col) == 0) {
                    for (value in 1..9) {
                        // 유효한 이동인 경우에만 설정 가능
                        if (game.isValidMove(row, col, value)) {
                            assertTrue(game.setCell(row, col, value))
                            assertEquals(value, game.getCell(row, col))
                            // 테스트 후 초기화
                            game.setCell(row, col, 0)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `게임 완료 시 모든 셀이 채워지는지 테스트`() {
        val game = SudokuGame()
        // 해답으로 채우기
        game.solveGame()
        // 모든 셀이 0이 아닌지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                assertNotEquals(0, game.getCell(row, col))
            }
        }
        // 게임 완료 상태 확인
        assertTrue(game.isGameComplete())
    }

    @Test
    fun `초기 셀들이 올바르게 보호되는지 테스트`() {
        val game = SudokuGame()
        val board = game.getBoard()
        var found = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] != 0) {
                    val originalValue = game.getCell(row, col)
                    assertFalse(game.setCell(row, col, (originalValue % 9) + 1))
                    assertEquals(originalValue, game.getCell(row, col))
                    found = true
                }
            }
        }
        assertTrue(found)
    }

    @Test
    fun `빈 셀들에 대해서만 수정이 가능한지 테스트`() {
        val game = SudokuGame()
        // 모든 빈 셀에 대해 수정 시도
        for (row in 0..8) {
            for (col in 0..8) {
                if (game.getCell(row, col) == 0) {
                    if (game.isValidMove(row, col, 5)) {
                        assertTrue(game.setCell(row, col, 5))
                        assertEquals(5, game.getCell(row, col))
                        // 테스트 후 초기화
                        game.setCell(row, col, 0)
                    } else {
                        assertFalse(game.setCell(row, col, 5))
                    }
                }
            }
        }
    }

    @Test
    fun `스도쿠 규칙 위반 시 유효성 검사가 올바르게 작동하는지 테스트`() {
        val game = SudokuGame()
        val board = game.getBoard()
        var found = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (!game.isInitialCell(row, col)) {
                    // 유효한 값 찾기
                    val validValue = (1..9).firstOrNull { value ->
                        game.isValidMove(row, col, value)
                    }
                    if (validValue != null) {
                        game.setCell(row, col, validValue)
                        // 같은 행의 다른 빈 셀에 같은 값 시도
                        for (c in 0..8) {
                            if (c != col && board[row][c] == 0 && !game.isInitialCell(row, c)) {
                                assertFalse(game.isValidMove(row, c, validValue))
                                break
                            }
                        }
                        // 같은 열의 다른 빈 셀에 같은 값 시도
                        for (r in 0..8) {
                            if (r != row && board[r][col] == 0 && !game.isInitialCell(r, col)) {
                                assertFalse(game.isValidMove(r, col, validValue))
                                break
                            }
                        }
                        // 같은 3x3 박스의 다른 빈 셀에 같은 값 시도
                        val boxRow = (row / 3) * 3
                        val boxCol = (col / 3) * 3
                        for (r in boxRow until boxRow + 3) {
                            for (c in boxCol until boxCol + 3) {
                                if ((r != row || c != col) && board[r][c] == 0 && !game.isInitialCell(r, c)) {
                                    assertFalse(game.isValidMove(r, c, validValue))
                                    break
                                }
                            }
                        }
                        found = true
                        break
                    }
                }
            }
            if (found) break
        }
        assertTrue(found)
    }

    @Test
    fun `보드 초기화 후 상태가 올바르게 복원되는지 테스트`() {
        val game = SudokuGame()
        
        // 초기 상태 저장
        val initialBoard = game.getBoard()
        
        // 모든 빈 셀을 채우기 (유효한 값으로)
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
        
        // 보드 초기화
        game.clearBoard()
        
        // 초기 상태로 돌아갔는지 확인
        val clearedBoard = game.getBoard()
        assertArrayEquals(initialBoard, clearedBoard)
    }

    @Test
    fun `새 게임 생성 시 상태가 올바르게 초기화되는지 테스트`() {
        val game = SudokuGame()
        // 초기 상태 저장
        val initialBoard = game.getBoard()
        // 일부 셀 수정 (유효한 값으로)
        val validValue1 = (1..9).firstOrNull { value ->
            game.isValidMove(0, 2, value)
        }
        val validValue2 = (1..9).firstOrNull { value ->
            game.isValidMove(1, 1, value)
        }
        if (validValue1 != null) {
            game.setCell(0, 2, validValue1)
        }
        if (validValue2 != null) {
            game.setCell(1, 1, validValue2)
        }
        // 새 게임 생성
        game.generateNewGame()
        // 게임이 완료되지 않았는지 확인
        assertFalse(game.isGameComplete())
        // 모든 셀이 유효한 값인지 확인
        for (row in 0..8) {
            for (col in 0..8) {
                val value = game.getCell(row, col)
                assertTrue(value >= 0 && value <= 9)
            }
        }
    }

    @Test
    fun testInvalidIndexAccess() {
        val game = SudokuGame()
        // 잘못된 인덱스 접근 테스트
        assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            game.getCell(-1, 0)
        }
        assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            game.getCell(0, -1)
        }
        assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            game.getCell(9, 0)
        }
        assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            game.getCell(0, 9)
        }
    }

    @Test
    fun testInvalidValueRange() {
        val game = SudokuGame()
        val board = game.getBoard()
        var found = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    // 1-9는 허용되어야 함
                    for (v in 1..9) {
                        if (game.isValidMove(row, col, v)) {
                            assertTrue(game.setCell(row, col, v))
                            assertEquals(v, game.getCell(row, col))
                            game.setCell(row, col, 0)
                        } else {
                            assertFalse(game.setCell(row, col, v))
                        }
                    }
                    // 10 이상의 값
                    assertFalse(game.setCell(row, col, 10))
                    assertFalse(game.setCell(row, col, 15))
                    assertFalse(game.setCell(row, col, 100))
                    // 음수 값
                    assertFalse(game.setCell(row, col, -1))
                    assertFalse(game.setCell(row, col, -5))
                    assertFalse(game.setCell(row, col, -10))
                    found = true
                    break
                }
            }
            if (found) break
        }
        assertTrue(found)
    }

    @Test
    fun testInitialCellProtection() {
        val game = SudokuGame()
        val board = game.getBoard()
        var found = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] != 0) {
                    assertTrue("($row, $col)는 초기값 셀이어야 함", game.isInitialCell(row, col))
                    assertFalse("초기값 셀은 수정할 수 없어야 함", game.setCell(row, col, 5))
                    found = true
                }
            }
        }
        assertTrue(found)
    }

    @Test
    fun testEmptyBoardOperations() {
        val game = SudokuGame()
        // clearBoard() 후 초기 퍼즐 상태로 복원되는지 테스트
        val initialBoard = game.getBoard()
        game.clearBoard()
        val clearedBoard = game.getBoard()
        // 보드가 초기 퍼즐과 동일해야 함
        for (row in 0..8) {
            assertArrayEquals("clearBoard() 후 보드는 초기 퍼즐과 같아야 함", initialBoard[row], clearedBoard[row])
        }
        // 게임이 완료되지 않아야 함
        assertFalse("초기 퍼즐은 완료되지 않아야 함", game.isGameComplete())
    }

    @Test
    fun testFullBoardOperations() {
        val game = SudokuGame()
        // 모든 셀이 채워진 보드 테스트
        game.solveGame()
        // 모든 셀이 채워져야 함
        for (row in 0..8) {
            for (col in 0..8) {
                assertNotEquals("해결된 보드의 모든 셀은 0이 아니어야 함", 0, game.getCell(row, col))
            }
        }
        // 게임이 완료되어야 함
        assertTrue("해결된 보드는 완료되어야 함", game.isGameComplete())
    }

    @Test
    fun testConcurrentCellModifications() {
        val game = SudokuGame()
        val board = game.getBoard()
        var found = false
        for (row in 0..8) {
            for (col in 0..8) {
                if (!game.isInitialCell(row, col)) {
                    // 유효한 값 찾기
                    val validValue1 = (1..9).firstOrNull { value ->
                        game.isValidMove(row, col, value)
                    }
                    val validValue2 = (1..9).firstOrNull { value ->
                        value != validValue1 && game.isValidMove(row, col, value)
                    }
                    
                    assertNotNull("유효한 값이 있어야 함", validValue1)
                    
                    // 두 번째 유효한 값이 없으면 첫 번째 값으로만 테스트
                    if (validValue2 == null) {
                        // 같은 셀에 같은 값을 여러 번 설정
                        assertTrue("첫 번째 설정은 성공해야 함", game.setCell(row, col, validValue1!!))
                        assertTrue("같은 값으로 다시 설정해도 성공해야 함", game.setCell(row, col, validValue1))
                        // 현재 값 확인
                        assertEquals("설정된 값이 저장되어야 함", validValue1, game.getCell(row, col))
                    } else {
                        // 같은 셀에 여러 번 값을 설정
                        assertTrue("첫 번째 설정은 성공해야 함", game.setCell(row, col, validValue1!!))
                        assertTrue("같은 값으로 다시 설정해도 성공해야 함", game.setCell(row, col, validValue1))
                        assertTrue("다른 값으로 설정해도 성공해야 함", game.setCell(row, col, validValue2))
                        // 현재 값 확인
                        assertEquals("마지막 설정된 값이 저장되어야 함", validValue2, game.getCell(row, col))
                    }
                    found = true
                    break
                }
            }
            if (found) break
        }
        assertTrue("테스트할 수 있는 빈 셀을 찾을 수 있어야 함", found)
    }

    @Test
    fun testBoardStateConsistency() {
        val game = SudokuGame()
        // 보드 상태 일관성 테스트
        val originalBoard = game.getBoard()
        val board = game.getBoard()
        // 빈 셀을 찾아서 수정 (초기 셀이 아닌 셀)
        var found = false
        var targetRow = 0
        var targetCol = 0
        for (row in 0..8) {
            for (col in 0..8) {
                if (!game.isInitialCell(row, col)) {
                    targetRow = row
                    targetCol = col
                    val validValue = (1..9).firstOrNull { value ->
                        game.isValidMove(row, col, value)
                    }
                    if (validValue != null) {
                        game.setCell(row, col, validValue)
                        found = true
                        break
                    }
                }
            }
            if (found) break
        }
        if (found) {
            // 수정된 보드와 원본 보드가 달라야 함
            val modifiedBoard = game.getBoard()
            assertNotEquals("보드가 수정되어야 함", originalBoard, modifiedBoard)
            // 수정된 셀의 값 확인
            val expectedValue = (1..9).firstOrNull { value ->
                game.isValidMove(targetRow, targetCol, value)
            }
            assertNotNull("유효한 값이 있어야 함", expectedValue)
            assertEquals("수정된 셀의 값이 올바르게 저장되어야 함", expectedValue, modifiedBoard[targetRow][targetCol])
        }
    }

    @Test
    fun testInvalidMoveDetection() {
        val game = SudokuGame()
        // 잘못된 이동 감지 테스트
        val emptyCell = findEmptyCell(game)
        if (emptyCell != null) {
            val (row, col) = emptyCell
            // 같은 행에 이미 있는 숫자
            val existingNumber = findExistingNumberInRow(game, row)
            if (existingNumber != null) {
                assertFalse("같은 행에 있는 숫자는 놓을 수 없어야 함", game.isValidMove(row, col, existingNumber))
            }
            // 같은 열에 이미 있는 숫자
            val existingNumberInCol = findExistingNumberInCol(game, col)
            if (existingNumberInCol != null) {
                assertFalse("같은 열에 있는 숫자는 놓을 수 없어야 함", game.isValidMove(row, col, existingNumberInCol))
            }
        }
    }

    @Test
    fun testGameResetConsistency() {
        val game = SudokuGame()
        val initialBoard = game.getBoard()
        // 게임 진행 (유효한 값으로)
        val validValue1 = (1..9).firstOrNull { value ->
            game.isValidMove(0, 0, value)
        }
        val validValue2 = (1..9).firstOrNull { value ->
            game.isValidMove(1, 1, value)
        }
        if (validValue1 != null) {
            game.setCell(0, 0, validValue1)
        }
        if (validValue2 != null) {
            game.setCell(1, 1, validValue2)
        }
        // 새 게임 생성
        game.generateNewGame()
        val newBoard = game.getBoard()
        // 새 게임은 이전과 달라야 함
        assertNotEquals("새 게임은 이전 게임과 달라야 함", initialBoard, newBoard)
        // 새 게임도 유효한 스도쿠여야 함
        assertTrue("새 게임은 유효한 스도쿠여야 함", isValidSudoku(newBoard))
    }

    // 헬퍼 함수들
    private fun findEmptyCell(game: SudokuGame): Pair<Int, Int>? {
        for (row in 0..8) {
            for (col in 0..8) {
                if (game.getCell(row, col) == 0 && !game.isInitialCell(row, col)) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }

    private fun findExistingNumberInRow(game: SudokuGame, row: Int): Int? {
        for (col in 0..8) {
            val value = game.getCell(row, col)
            if (value != 0) {
                return value
            }
        }
        return null
    }

    private fun findExistingNumberInCol(game: SudokuGame, col: Int): Int? {
        for (row in 0..8) {
            val value = game.getCell(row, col)
            if (value != 0) {
                return value
            }
        }
        return null
    }

    private fun isValidSudoku(board: Array<IntArray>): Boolean {
        // 간단한 스도쿠 유효성 검사
        for (row in 0..8) {
            for (col in 0..8) {
                val value = board[row][col]
                if (value != 0) {
                    // 행 검사
                    for (c in 0..8) {
                        if (c != col && board[row][c] == value) return false
                    }
                    // 열 검사
                    for (r in 0..8) {
                        if (r != row && board[r][col] == value) return false
                    }
                }
            }
        }
        return true
    }
} 