package com.sandro.new_sudoku

import org.junit.Test
import org.junit.Assert.*

class SimpleTest {
    
    @Test
    fun `간단한 셀 설정 테스트`() {
        val game = SudokuGame()
        
        // 빈 셀 찾기 (초기 셀이 아닌 셀)
        val board = game.getBoard()
        var foundEmpty = false
        var emptyRow = -1
        var emptyCol = -1
        var valueToSet: Int? = null
        
        for (row in 0..8) {
            for (col in 0..8) {
                if (!game.isInitialCell(row, col)) {
                    // 넣을 수 있는 값 찾기
                    valueToSet = (1..9).firstOrNull { game.isValidMove(row, col, it) }
                    if (valueToSet != null) {
                        emptyRow = row
                        emptyCol = col
                        foundEmpty = true
                        break
                    }
                }
            }
            if (foundEmpty) break
        }
        
        assertTrue("빈 셀을 찾을 수 있어야 함", foundEmpty)
        assertNotNull("넣을 수 있는 값이 있어야 함", valueToSet)
        
        // 셀에 값 설정
        val success = game.setCell(emptyRow, emptyCol, valueToSet!!)
        assertTrue("셀 설정이 성공해야 함", success)
        
        // 값이 제대로 설정되었는지 확인
        val newBoard = game.getBoard()
        assertEquals("설정된 값이 올바르게 저장되어야 함", valueToSet, newBoard[emptyRow][emptyCol])
        
        // getCell 메서드로도 확인
        assertEquals("getCell로도 올바른 값을 가져와야 함", valueToSet, game.getCell(emptyRow, emptyCol))
    }
} 