package com.sandro.new_sudoku

import com.sandro.new_sudoku.ui.DifficultyLevel
import com.sandro.new_sudoku.ui.MainScreenViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenTest {

    @Test
    fun `초기 상태는 하 난이도가 선택되어 있고 난이도 팝업은 숨겨져 있어야 한다`() = runTest {
        val viewModel = MainScreenViewModel()
        val state = viewModel.state.value

        assertEquals(DifficultyLevel.EASY, state.selectedDifficulty)
        assertFalse(state.hasGameInProgress)
        assertFalse(state.shouldNavigateToGame)
        assertFalse(state.showDifficultyPopup)
    }

    @Test
    fun `난이도를 변경할 수 있어야 한다`() = runTest {
        val viewModel = MainScreenViewModel()

        viewModel.selectDifficulty(DifficultyLevel.MEDIUM)
        assertEquals(DifficultyLevel.MEDIUM, viewModel.state.value.selectedDifficulty)

        viewModel.selectDifficulty(DifficultyLevel.HARD)
        assertEquals(DifficultyLevel.HARD, viewModel.state.value.selectedDifficulty)
    }

    @Test
    fun `새 게임 시작시 네비게이션 상태가 변경되어야 한다`() = runTest {
        val viewModel = MainScreenViewModel()

        viewModel.startNewGame()

        assertTrue(viewModel.state.value.shouldNavigateToGame)
    }

    @Test
    fun `이어하기 기능은 게임이 진행 중일 때만 가능해야 한다`() = runTest {
        val viewModel = MainScreenViewModel()

        // 초기에는 이어하기 불가능
        assertFalse(viewModel.state.value.hasGameInProgress)

        // 게임 시작 후 이어하기 가능
        viewModel.startNewGame()
        viewModel.markGameInProgress()
        assertTrue(viewModel.state.value.hasGameInProgress)
    }

    @Test
    fun `네비게이션 완료 후 상태가 초기화되어야 한다`() = runTest {
        val viewModel = MainScreenViewModel()

        viewModel.startNewGame()
        assertTrue(viewModel.state.value.shouldNavigateToGame)

        viewModel.onNavigationCompleted()
        assertFalse(viewModel.state.value.shouldNavigateToGame)
    }

    @Test
    fun `새 게임 버튼 클릭시 난이도 선택 팝업이 표시되어야 한다`() = runTest {
        val viewModel = MainScreenViewModel()

        // 초기에는 난이도 선택 팝업이 숨겨져 있음
        assertFalse(viewModel.state.value.showDifficultyPopup)

        // 새 게임 버튼 클릭
        viewModel.showDifficultyPopup()

        // 난이도 선택 팝업이 표시됨
        assertTrue(viewModel.state.value.showDifficultyPopup)
    }

    @Test
    fun `난이도 선택시 바로 게임이 시작되고 팝업이 숨겨져야 한다`() = runTest {
        val viewModel = MainScreenViewModel()

        // 난이도 선택 팝업 표시
        viewModel.showDifficultyPopup()
        assertTrue(viewModel.state.value.showDifficultyPopup)

        // 난이도 선택과 동시에 게임 시작
        viewModel.selectDifficultyAndStartGame(DifficultyLevel.MEDIUM)

        // 선택된 난이도 확인
        assertEquals(DifficultyLevel.MEDIUM, viewModel.state.value.selectedDifficulty)
        // 게임 시작 상태 확인
        assertTrue(viewModel.state.value.shouldNavigateToGame)
        // 팝업이 다시 숨겨짐
        assertFalse(viewModel.state.value.showDifficultyPopup)
    }

    @Test
    fun `팝업 외부 클릭시 난이도 선택 팝업이 숨겨져야 한다`() = runTest {
        val viewModel = MainScreenViewModel()

        // 난이도 선택 팝업 표시
        viewModel.showDifficultyPopup()
        assertTrue(viewModel.state.value.showDifficultyPopup)

        // 팝업 닫기
        viewModel.hideDifficultyPopup()

        // 팝업이 숨겨짐
        assertFalse(viewModel.state.value.showDifficultyPopup)
    }
} 