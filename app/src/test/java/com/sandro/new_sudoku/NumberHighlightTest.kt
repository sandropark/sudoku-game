package com.sandro.new_sudoku

import com.sandro.new_sudoku.helpers.SudokuTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberHighlightTest {

    @Test
    fun `숫자가 있는 셀 선택 시 같은 숫자들이 하이라이트되어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value

        // 숫자가 있는 셀 찾기 (초기 셀)
        var targetRow = -1
        var targetCol = -1
        var targetNumber = 0

        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0) {
                    targetRow = row
                    targetCol = col
                    targetNumber = state.board[row][col]
                    break
                }
            }
            if (targetRow != -1) break
        }

        assertTrue("테스트를 위한 숫자가 있는 셀이 존재해야 함", targetRow != -1)

        // 해당 셀 선택
        viewModel.selectCell(targetRow, targetCol)

        val updatedState = viewModel.state.value
        assertEquals("선택된 숫자가 설정되어야 함", targetNumber, updatedState.highlightedNumber)

        // 같은 숫자를 가진 셀들이 하이라이트되었는지 확인
        var highlightedCount = 0
        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] == targetNumber) {
                    assertTrue(
                        "같은 숫자($targetNumber)를 가진 셀($row, $col)이 하이라이트되어야 함",
                        updatedState.highlightedCells.contains(Pair(row, col))
                    )
                    highlightedCount++
                }
            }
        }

        assertTrue("하이라이트된 셀이 1개 이상이어야 함", highlightedCount > 0)
    }

    @Test
    fun `빈 셀 선택 시 하이라이트가 없어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value

        // 빈 셀 찾기
        var emptyRow = -1
        var emptyCol = -1

        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("테스트를 위한 빈 셀이 존재해야 함", emptyRow != -1)

        // 빈 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)

        val updatedState = viewModel.state.value
        assertEquals("빈 셀 선택 시 하이라이트된 숫자가 0이어야 함", 0, updatedState.highlightedNumber)
        assertTrue("빈 셀 선택 시 하이라이트된 셀이 없어야 함", updatedState.highlightedCells.isEmpty())
    }

    @Test
    fun `사용자가 입력한 숫자도 하이라이트되어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value

        // 빈 셀에 숫자 입력
        var emptyRow = -1
        var emptyCol = -1

        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("테스트를 위한 빈 셀이 존재해야 함", emptyRow != -1)

        // 빈 셀에 숫자 입력 (5 입력)
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(5)

        // 입력한 셀 다시 선택
        viewModel.selectCell(emptyRow, emptyCol)

        val updatedState = viewModel.state.value
        assertEquals("입력한 숫자가 하이라이트되어야 함", 5, updatedState.highlightedNumber)
        assertTrue(
            "입력한 셀 자체도 하이라이트되어야 함",
            updatedState.highlightedCells.contains(Pair(emptyRow, emptyCol))
        )
    }

    @Test
    fun `다른 셀 선택 시 하이라이트가 업데이트되어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value

        // 첫 번째 숫자가 있는 셀 찾기
        var firstRow = -1
        var firstCol = -1
        var firstNumber = 0

        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0) {
                    firstRow = row
                    firstCol = col
                    firstNumber = state.board[row][col]
                    break
                }
            }
            if (firstRow != -1) break
        }

        // 다른 숫자가 있는 셀 찾기
        var secondRow = -1
        var secondCol = -1
        var secondNumber = 0

        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0 &&
                    state.board[row][col] != firstNumber &&
                    (row != firstRow || col != firstCol)
                ) {
                    secondRow = row
                    secondCol = col
                    secondNumber = state.board[row][col]
                    break
                }
            }
            if (secondRow != -1) break
        }

        assertTrue("테스트를 위한 첫 번째 셀이 존재해야 함", firstRow != -1)
        assertTrue("테스트를 위한 두 번째 셀이 존재해야 함", secondRow != -1)
        assertNotEquals("두 셀의 숫자가 달라야 함", firstNumber, secondNumber)

        // 첫 번째 셀 선택
        viewModel.selectCell(firstRow, firstCol)
        val firstState = viewModel.state.value
        assertEquals("첫 번째 숫자가 하이라이트되어야 함", firstNumber, firstState.highlightedNumber)

        // 두 번째 셀 선택
        viewModel.selectCell(secondRow, secondCol)
        val secondState = viewModel.state.value
        assertEquals("두 번째 숫자가 하이라이트되어야 함", secondNumber, secondState.highlightedNumber)
        assertNotEquals(
            "하이라이트된 숫자가 변경되어야 함",
            firstState.highlightedNumber,
            secondState.highlightedNumber
        )
    }

    @Test
    fun `셀 선택 해제 시 하이라이트가 사라져야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        val state = viewModel.state.value

        // 숫자가 있는 셀 선택
        var targetRow = -1
        var targetCol = -1

        for (row in 0..8) {
            for (col in 0..8) {
                if (state.board[row][col] != 0) {
                    targetRow = row
                    targetCol = col
                    break
                }
            }
            if (targetRow != -1) break
        }

        // 셀 선택
        viewModel.selectCell(targetRow, targetCol)
        val selectedState = viewModel.state.value
        assertTrue("숫자가 하이라이트되어야 함", selectedState.highlightedNumber > 0)
        assertFalse("하이라이트된 셀이 있어야 함", selectedState.highlightedCells.isEmpty())

        // 셀 선택 해제
        viewModel.clearSelection()

        val clearedState = viewModel.state.value
        assertEquals("선택 해제 시 하이라이트된 숫자가 0이어야 함", 0, clearedState.highlightedNumber)
        assertTrue("선택 해제 시 하이라이트된 셀이 없어야 함", clearedState.highlightedCells.isEmpty())
    }

    @Test
    fun `숫자 입력 시 바로 같은 숫자들이 하이라이트되어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀 찾기
        var emptyRow = -1
        var emptyCol = -1

        val state = viewModel.state.value
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("테스트를 위한 빈 셀이 존재해야 함", emptyRow != -1)

        // 빈 셀 선택
        viewModel.selectCell(emptyRow, emptyCol)
        
        // 숫자 입력 (5를 입력)
        viewModel.setCellValue(5)

        val updatedState = viewModel.state.value
        
        // 입력한 숫자가 하이라이트되어야 함
        assertEquals("입력한 숫자가 하이라이트되어야 함", 5, updatedState.highlightedNumber)
        
        // 입력한 셀과 같은 숫자를 가진 모든 셀이 하이라이트되어야 함
        for (row in 0..8) {
            for (col in 0..8) {
                if (updatedState.board[row][col] == 5) {
                    assertTrue(
                        "숫자 5를 가진 셀($row, $col)이 하이라이트되어야 함",
                        updatedState.highlightedCells.contains(Pair(row, col))
                    )
                }
            }
        }
        
        // 최소한 입력한 셀은 하이라이트되어야 함
        assertTrue(
            "입력한 셀($emptyRow, $emptyCol)이 하이라이트되어야 함",
            updatedState.highlightedCells.contains(Pair(emptyRow, emptyCol))
        )
    }

    @Test
    fun `숫자 제거 시 하이라이트가 꺼져야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀에 숫자 입력하고 하이라이트 확인
        var emptyRow = -1
        var emptyCol = -1

        val state = viewModel.state.value
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("테스트를 위한 빈 셀이 존재해야 함", emptyRow != -1)

        // 빈 셀 선택 후 숫자 입력
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(7)

        val afterInputState = viewModel.state.value
        assertEquals("숫자 입력 후 하이라이트되어야 함", 7, afterInputState.highlightedNumber)
        assertFalse("숫자 입력 후 하이라이트된 셀이 있어야 함", afterInputState.highlightedCells.isEmpty())

        // 숫자 제거 (0으로 설정)
        viewModel.setCellValue(0)

        val afterRemovalState = viewModel.state.value
        assertEquals("숫자 제거 후 하이라이트가 꺼져야 함", 0, afterRemovalState.highlightedNumber)
        assertTrue("숫자 제거 후 하이라이트된 셀이 없어야 함", afterRemovalState.highlightedCells.isEmpty())
    }

    @Test
    fun `같은 숫자 토글 시 하이라이트가 적절히 처리되어야 한다`() {
        val viewModel = SudokuTestHelper.createTestViewModel()

        // 빈 셀에 숫자 입력
        var emptyRow = -1
        var emptyCol = -1

        val state = viewModel.state.value
        for (row in 0..8) {
            for (col in 0..8) {
                if (!viewModel.isInitialCell(row, col) && state.board[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                    break
                }
            }
            if (emptyRow != -1) break
        }

        assertTrue("테스트를 위한 빈 셀이 존재해야 함", emptyRow != -1)

        // 빈 셀 선택 후 숫자 입력 (3 입력)
        viewModel.selectCell(emptyRow, emptyCol)
        viewModel.setCellValue(3)

        val afterFirstInputState = viewModel.state.value
        assertEquals("첫 번째 입력 후 하이라이트되어야 함", 3, afterFirstInputState.highlightedNumber)
        assertFalse("첫 번째 입력 후 하이라이트된 셀이 있어야 함", afterFirstInputState.highlightedCells.isEmpty())
        assertEquals("셀에 3이 입력되어야 함", 3, afterFirstInputState.board[emptyRow][emptyCol])

        // 같은 숫자를 다시 입력 (토글로 제거됨)
        viewModel.setCellValue(3)

        val afterToggleState = viewModel.state.value
        assertEquals("토글 후 하이라이트가 꺼져야 함", 0, afterToggleState.highlightedNumber)
        assertTrue("토글 후 하이라이트된 셀이 없어야 함", afterToggleState.highlightedCells.isEmpty())
        assertEquals("토글 후 셀이 비어있어야 함", 0, afterToggleState.board[emptyRow][emptyCol])
    }
} 