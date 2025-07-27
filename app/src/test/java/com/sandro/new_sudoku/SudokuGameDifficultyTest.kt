package com.sandro.new_sudoku

import com.sandro.new_sudoku.ui.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuGameDifficultyTest {

    @Test
    fun `하 난이도는 35개 셀을 제거해야 한다`() {
        val game = SudokuGame()
        game.generateNewGameWithDifficulty(DifficultyLevel.EASY)

        val board = game.getBoard()
        val filledCells = board.sumOf { row -> row.count { it != 0 } }
        val expectedFilledCells = 81 - DifficultyLevel.EASY.cellsToRemove

        assertEquals(expectedFilledCells, filledCells)
    }

    @Test
    fun `중 난이도는 50개 셀을 제거해야 한다`() {
        val game = SudokuGame()
        game.generateNewGameWithDifficulty(DifficultyLevel.MEDIUM)

        val board = game.getBoard()
        val filledCells = board.sumOf { row -> row.count { it != 0 } }
        val expectedFilledCells = 81 - DifficultyLevel.MEDIUM.cellsToRemove

        assertEquals(expectedFilledCells, filledCells)
    }

    @Test
    fun `상 난이도는 65개 셀을 제거해야 한다`() {
        val game = SudokuGame()
        game.generateNewGameWithDifficulty(DifficultyLevel.HARD)

        val board = game.getBoard()
        val filledCells = board.sumOf { row -> row.count { it != 0 } }
        val expectedFilledCells = 81 - DifficultyLevel.HARD.cellsToRemove

        assertEquals(expectedFilledCells, filledCells)
    }

    @Test
    fun `난이도별로 서로 다른 수의 빈 셀을 가져야 한다`() {
        val easyGame = SudokuGame()
        val mediumGame = SudokuGame()
        val hardGame = SudokuGame()

        easyGame.generateNewGameWithDifficulty(DifficultyLevel.EASY)
        mediumGame.generateNewGameWithDifficulty(DifficultyLevel.MEDIUM)
        hardGame.generateNewGameWithDifficulty(DifficultyLevel.HARD)

        val easyEmptyCells = easyGame.getBoard().sumOf { row -> row.count { it == 0 } }
        val mediumEmptyCells = mediumGame.getBoard().sumOf { row -> row.count { it == 0 } }
        val hardEmptyCells = hardGame.getBoard().sumOf { row -> row.count { it == 0 } }

        assertTrue("쉬움 난이도가 가장 적은 빈 셀을 가져야 함", easyEmptyCells < mediumEmptyCells)
        assertTrue("어려움 난이도가 가장 많은 빈 셀을 가져야 함", mediumEmptyCells < hardEmptyCells)
    }
} 