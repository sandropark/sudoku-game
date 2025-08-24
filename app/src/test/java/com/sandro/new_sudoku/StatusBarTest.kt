package com.sandro.new_sudoku

import com.sandro.new_sudoku.ui.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * StatusBar 컴포넌트 관련 테스트
 */
class StatusBarTest {

    @Test
    fun `난이도_한국어_변환_테스트`() {
        assertEquals("쉬움", difficultyToKorean(DifficultyLevel.EASY))
        assertEquals("보통", difficultyToKorean(DifficultyLevel.MEDIUM))
        assertEquals("어려움", difficultyToKorean(DifficultyLevel.HARD))
    }

    @Test
    fun `모든_난이도에_대한_한국어_변환_테스트`() {
        val allDifficulties = DifficultyLevel.values()
        val expectedKoreans = listOf("쉬움", "보통", "어려움")
        
        for ((index, difficulty) in allDifficulties.withIndex()) {
            assertEquals(
                "${difficulty.displayName} 난이도의 한국어 변환이 올바르지 않음",
                expectedKoreans[index],
                difficultyToKorean(difficulty)
            )
        }
    }
}