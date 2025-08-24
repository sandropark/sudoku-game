package com.sandro.new_sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 힌트 기능의 정확성을 실제 게임 시나리오에서 검증하는 테스트
 */
class HintAccuracyTest {

    private lateinit var game: SudokuGame

    @Before
    fun setUp() {
        game = SudokuGame()
    }

    @Test
    fun `실제 퍼즐에서 힌트가 올바른 정답을 제공하는지 테스트`() {
        // Given: 초기화된 게임 (퍼즐 상태)
        val board = game.getBoard()
        val solution = game.getSolution()
        
        // When & Then: 각 빈 셀에서 힌트가 해답과 일치하는지 확인
        var emptyCellsChecked = 0
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (board[row][col] == 0) { // 빈 셀인 경우
                    val hint = game.getHint(row, col)
                    val solutionValue = solution[row][col]
                    
                    assertEquals("빈 셀 ($row, $col)에서 힌트가 해답과 일치해야 함", 
                               solutionValue, hint)
                    emptyCellsChecked++
                }
            }
        }
        
        // 빈 셀이 충분히 있는지 확인 (퍼즐이 제대로 생성되었는지)
        assertTrue("테스트할 빈 셀이 있어야 함", emptyCellsChecked > 0)
        println("검증한 빈 셀 개수: $emptyCellsChecked")
    }

    @Test
    fun `초기 셀에서도 힌트가 올바른 정답을 제공하는지 테스트`() {
        // Given: 초기화된 게임
        val board = game.getBoard()
        val solution = game.getSolution()
        
        // When & Then: 초기에 채워진 셀에서도 힌트가 해답과 일치하는지 확인
        var initialCellsChecked = 0
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (board[row][col] != 0) { // 초기에 채워진 셀인 경우
                    val hint = game.getHint(row, col)
                    val solutionValue = solution[row][col]
                    val currentValue = board[row][col]
                    
                    // 힌트는 해답과 일치해야 함
                    assertEquals("초기 셀 ($row, $col)에서 힌트가 해답과 일치해야 함", 
                               solutionValue, hint)
                    
                    // 초기 셀의 값도 해답과 일치해야 함 (퍼즐 생성 검증)
                    assertEquals("초기 셀 ($row, $col)의 값이 해답과 일치해야 함", 
                               solutionValue, currentValue)
                    initialCellsChecked++
                }
            }
        }
        
        assertTrue("테스트할 초기 셀이 있어야 함", initialCellsChecked > 0)
        println("검증한 초기 셀 개수: $initialCellsChecked")
    }

    @Test
    fun `잘못된 값이 입력된 셀에서 힌트가 올바른 정답을 제공하는지 테스트`() {
        // Given: 게임이 초기화되고 임의의 빈 셀에 잘못된 값을 입력
        var targetRow = -1
        var targetCol = -1
        
        // 빈 셀 찾기
        val board = game.getBoard()
        outerLoop@ for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (board[row][col] == 0) {
                    targetRow = row
                    targetCol = col
                    break@outerLoop
                }
            }
        }
        
        assertTrue("테스트할 빈 셀을 찾아야 함", targetRow >= 0 && targetCol >= 0)
        
        val correctValue = game.getHint(targetRow, targetCol)
        
        // 정답이 아닌 다른 값을 찾아서 입력
        var wrongValue = -1
        for (value in 1..9) {
            if (value != correctValue) {
                // 해당 위치에 이 값이 유효하지 않다면 (충돌이 발생한다면) 사용
                game.setCell(targetRow, targetCol, value)
                if (!game.isCellValid(targetRow, targetCol)) {
                    wrongValue = value
                    break
                }
                game.setCell(targetRow, targetCol, 0) // 원복
            }
        }
        
        if (wrongValue == -1) {
            // 충돌하는 값을 찾지 못한 경우, 단순히 다른 값 사용
            wrongValue = if (correctValue == 1) 2 else 1
            game.setCell(targetRow, targetCol, wrongValue)
        }
        
        // When: 잘못된 값이 입력된 상태에서 힌트 요청
        val hint = game.getHint(targetRow, targetCol)
        
        // Then: 힌트는 올바른 정답을 반환해야 함
        assertEquals("잘못된 값이 입력된 셀에서도 힌트가 올바른 정답을 반환해야 함", 
                   correctValue, hint)
        assertFalse("힌트는 현재 잘못 입력된 값과 달라야 함", hint == wrongValue)
    }

    @Test
    fun `여러 번 게임을 생성해도 힌트가 항상 일관되게 작동하는지 테스트`() {
        // Given & When & Then: 여러 번 새 게임을 생성하고 힌트 검증
        for (gameIteration in 1..5) {
            game.generateNewGame()
            
            val board = game.getBoard()
            val solution = game.getSolution()
            
            // 각 셀에서 힌트가 해답과 일치하는지 확인
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    val hint = game.getHint(row, col)
                    val solutionValue = solution[row][col]
                    
                    assertEquals("게임 $gameIteration - 셀 ($row, $col)에서 힌트가 해답과 일치해야 함", 
                               solutionValue, hint)
                }
            }
        }
    }

    @Test
    fun `난이도별로 힌트가 올바르게 작동하는지 테스트`() {
        // Given & When & Then: 각 난이도에서 힌트 검증
        val difficulties = listOf(
            com.sandro.new_sudoku.ui.DifficultyLevel.EASY,
            com.sandro.new_sudoku.ui.DifficultyLevel.MEDIUM,
            com.sandro.new_sudoku.ui.DifficultyLevel.HARD
        )
        
        for (difficulty in difficulties) {
            game.generateNewGameWithDifficulty(difficulty)
            
            val board = game.getBoard()
            val solution = game.getSolution()
            
            // 해답이 유효한 스도쿠인지 확인
            validateSudokuSolution(solution, "난이도 ${difficulty.name}에서 생성된 해답")
            
            // 각 셀에서 힌트가 해답과 일치하는지 확인
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    val hint = game.getHint(row, col)
                    val solutionValue = solution[row][col]
                    
                    assertEquals("난이도 ${difficulty.name} - 셀 ($row, $col)에서 힌트가 해답과 일치해야 함", 
                               solutionValue, hint)
                }
            }
        }
    }

    private fun validateSudokuSolution(solution: Array<IntArray>, context: String) {
        // 각 행 검증
        for (row in 0 until 9) {
            val rowNumbers = solution[row].toSet()
            assertEquals("$context - 행 $row 에 1-9가 모두 있어야 함", 
                       setOf(1,2,3,4,5,6,7,8,9), rowNumbers)
        }
        
        // 각 열 검증
        for (col in 0 until 9) {
            val colNumbers = mutableSetOf<Int>()
            for (row in 0 until 9) {
                colNumbers.add(solution[row][col])
            }
            assertEquals("$context - 열 $col 에 1-9가 모두 있어야 함", 
                       setOf(1,2,3,4,5,6,7,8,9), colNumbers)
        }
        
        // 각 3x3 박스 검증
        for (boxRow in 0 until 3) {
            for (boxCol in 0 until 3) {
                val boxNumbers = mutableSetOf<Int>()
                for (row in boxRow * 3 until boxRow * 3 + 3) {
                    for (col in boxCol * 3 until boxCol * 3 + 3) {
                        boxNumbers.add(solution[row][col])
                    }
                }
                assertEquals("$context - 3x3 박스 ($boxRow, $boxCol) 에 1-9가 모두 있어야 함", 
                           setOf(1,2,3,4,5,6,7,8,9), boxNumbers)
            }
        }
    }
}