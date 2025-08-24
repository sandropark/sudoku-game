package com.sandro.new_sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RandomGenerationTest {

    @Test
    fun `진짜 랜덤 스도쿠가 생성되는지 테스트`() {
        // Given
        val boards = mutableSetOf<String>()
        val testCount = 10

        // When
        for (i in 1..testCount) {
            val game = SudokuGame()
            val board = game.getBoard()

            // 보드를 문자열로 변환하여 중복 확인
            val boardString = board.joinToString { row ->
                row.joinToString("")
            }
            boards.add(boardString)

            // 각 보드가 유효한 스도쿠인지 확인
            assertValidSudoku(board)
        }

        // Then
        println("생성된 고유한 보드 수: ${boards.size} / $testCount")
        assertTrue("최소 80% 이상은 서로 다른 보드여야 함", boards.size >= testCount * 0.8)
    }

    @Test
    fun `첫 번째 행이 매번 다르게 생성되는지 테스트`() {
        // Given
        val firstRows = mutableSetOf<String>()
        val testCount = 20

        // When
        for (i in 1..testCount) {
            val game = SudokuGame()
            val board = game.getBoard()
            val firstRow = board[0].joinToString("")
            firstRows.add(firstRow)
        }

        // Then
        println("고유한 첫 번째 행 수: ${firstRows.size} / $testCount")
        assertTrue("첫 번째 행이 다양하게 생성되어야 함", firstRows.size >= testCount * 0.9)
    }

    private fun assertValidSudoku(board: Array<IntArray>) {
        // 행 검증
        for (row in board) {
            val nonZeroValues = row.filter { it != 0 }
            assertEquals("행에 중복된 값이 없어야 함", nonZeroValues.size, nonZeroValues.toSet().size)
        }

        // 열 검증
        for (col in 0..8) {
            val columnValues = board.map { it[col] }.filter { it != 0 }
            assertEquals("열에 중복된 값이 없어야 함", columnValues.size, columnValues.toSet().size)
        }

        // 3x3 박스 검증
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val boxValues = mutableListOf<Int>()
                for (r in boxRow * 3 until (boxRow + 1) * 3) {
                    for (c in boxCol * 3 until (boxCol + 1) * 3) {
                        if (board[r][c] != 0) {
                            boxValues.add(board[r][c])
                        }
                    }
                }
                assertEquals("3x3 박스에 중복된 값이 없어야 함", boxValues.size, boxValues.toSet().size)
            }
        }
    }
}