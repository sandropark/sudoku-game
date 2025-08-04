package com.sandro.new_sudoku.base

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule

/**
 * UI 테스트를 위한 기본 클래스
 * 공통 설정과 헬퍼 메서드를 제공하여 중복 코드를 제거
 */
abstract class BaseUITest {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    /**
     * UI가 안정될 때까지 대기한다
     */
    protected fun waitForIdle() {
        composeTestRule.waitForIdle()
    }
}