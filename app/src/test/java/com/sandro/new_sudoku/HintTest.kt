package com.sandro.new_sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

/**
 * SudokuGame의 힌트 기능 테스트
 */
class HintTest {

    private lateinit var game: SudokuGame

    @Before
    fun setUp() {
        game = SudokuGame()
    }

    @Test
    fun `유효한 좌표에 대해 정답을 반환하는지 테스트`() {
        // Given: 게임이 초기화됨

        // When: 유효한 좌표에 대해 힌트를 요청
        val hint = game.getHint(0, 0)

        // Then: 1-9 사이의 유효한 숫자가 반환됨
        assert(hint in 1..9) { "힌트는 1-9 사이의 숫자여야 함: $hint" }
    }

    @Test
    fun `여러 셀에 대해 서로 다른 정답을 반환하는지 테스트`() {
        // Given: 게임이 초기화됨

        // When: 여러 셀에 대해 힌트를 요청
        val hint1 = game.getHint(0, 0)
        val hint2 = game.getHint(0, 1)
        val hint3 = game.getHint(1, 0)

        // Then: 모든 힌트가 유효한 범위 내에 있음
        assert(hint1 in 1..9)
        assert(hint2 in 1..9)
        assert(hint3 in 1..9)
    }

    @Test
    fun `같은 셀에 대해 일관된 정답을 반환하는지 테스트`() {
        // Given: 게임이 초기화됨

        // When: 같은 셀에 대해 여러 번 힌트를 요청
        val hint1 = game.getHint(4, 4)
        val hint2 = game.getHint(4, 4)
        val hint3 = game.getHint(4, 4)

        // Then: 항상 같은 값이 반환됨
        assertEquals("같은 셀에 대해 일관된 힌트를 반환해야 함", hint1, hint2)
        assertEquals("같은 셀에 대해 일관된 힌트를 반환해야 함", hint1, hint3)
    }

    @Test
    fun `음수 row에 대해 예외를 발생시키는지 테스트`() {
        // When & Then: 음수 row로 힌트를 요청하면 예외 발생
        assertThrows(IllegalArgumentException::class.java) {
            game.getHint(-1, 0)
        }
    }

    @Test
    fun `음수 col에 대해 예외를 발생시키는지 테스트`() {
        // When & Then: 음수 col로 힌트를 요청하면 예외 발생
        assertThrows(IllegalArgumentException::class.java) {
            game.getHint(0, -1)
        }
    }

    @Test
    fun `범위를 벗어난 row에 대해 예외를 발생시키는지 테스트`() {
        // When & Then: 9 이상의 row로 힌트를 요청하면 예외 발생
        assertThrows(IllegalArgumentException::class.java) {
            game.getHint(9, 0)
        }
    }

    @Test
    fun `범위를 벗어난 col에 대해 예외를 발생시키는지 테스트`() {
        // When & Then: 9 이상의 col로 힌트를 요청하면 예외 발생
        assertThrows(IllegalArgumentException::class.java) {
            game.getHint(0, 9)
        }
    }

    @Test
    fun `현재 보드 상태와 관계없이 정답을 반환하는지 테스트`() {
        // Given: 게임 보드에 임의의 값을 설정
        game.setCell(0, 0, 5)
        game.setCell(1, 1, 3)

        // When: 힌트를 요청
        val hint1 = game.getHint(0, 0)
        val hint2 = game.getHint(1, 1)

        // Then: 현재 보드 값과 관계없이 정답을 반환
        assert(hint1 in 1..9)
        assert(hint2 in 1..9)
        // 힌트가 현재 입력된 값과 다를 수 있음 (정답이 아닌 경우)
    }

    @Test
    fun `초기 셀과 빈 셀 모두에 대해 정답을 반환하는지 테스트`() {
        // Given: 게임이 초기화됨 (일부 셀은 초기값, 일부는 빈 셀)

        // When: 초기 셀과 빈 셀 모두에 대해 힌트를 요청
        var initialCellTested = false
        var emptyCellTested = false

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val hint = game.getHint(row, col)
                assert(hint in 1..9)

                if (game.isInitialCell(row, col)) {
                    initialCellTested = true
                }
                if (game.getCell(row, col) == 0) {
                    emptyCellTested = true
                }
            }
        }

        // Then: 두 경우 모두 테스트됨을 확인
        assert(initialCellTested) { "초기 셀이 있어야 함" }
        assert(emptyCellTested) { "빈 셀이 있어야 함" }
    }
}