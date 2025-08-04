package com.sandro.new_sudoku.helpers

import com.sandro.new_sudoku.SudokuViewModel

/**
 * 스도쿠 테스트를 위한 공통 헬퍼 클래스
 * 중복되는 테스트 로직을 재사용 가능한 메서드로 제공
 */
object SudokuTestHelper {

    /**
     * 테스트용 ViewModel을 생성한다
     * @param enableTestMode 테스트 모드 활성화 여부 (기본값: true - 타이머 자동 시작 방지)
     * @return 설정된 ViewModel 인스턴스
     */
    fun createTestViewModel(enableTestMode: Boolean = true): SudokuViewModel {
        val viewModel = SudokuViewModel()
        if (enableTestMode) {
            viewModel.isTestMode = true
        }
        return viewModel
    }

    /**
     * 타이머 관련 테스트용 ViewModel을 생성한다 (isTestMode = true)
     * @return 테스트 모드가 활성화된 ViewModel 인스턴스
     */
    fun createTimerTestViewModel(): SudokuViewModel {
        return createTestViewModel(enableTestMode = true)
    }

    /**
     * 편집 가능한 빈 셀을 찾는다 (초기 셀이 아닌 셀만)
     * @param board 현재 게임 보드
     * @param viewModel ViewModel 인스턴스 (초기 셀 확인용)
     * @return 빈 셀의 좌표 (row, col) 또는 null
     */
    fun findEmptyCell(board: Array<IntArray>, viewModel: SudokuViewModel): Pair<Int, Int>? {
        for (i in 0..8) {
            for (j in 0..8) {
                if (!viewModel.isInitialCell(i, j) && board[i][j] == 0) return i to j
            }
        }
        return null
    }

    /**
     * 지정된 위치에 유효한 숫자를 찾는다 (스도쿠 규칙 준수)
     * @param board 현재 게임 보드
     * @param row 행 인덱스
     * @param col 열 인덱스
     * @return 유효한 숫자 (1-9) 또는 null
     */
    fun findValidValue(board: Array<IntArray>, row: Int, col: Int): Int? {
        for (value in 1..9) {
            // 행 검사
            var validInRow = true
            for (c in 0..8) {
                if (c != col && board[row][c] == value) {
                    validInRow = false
                    break
                }
            }
            if (!validInRow) continue

            // 열 검사
            var validInCol = true
            for (r in 0..8) {
                if (r != row && board[r][col] == value) {
                    validInCol = false
                    break
                }
            }
            if (!validInCol) continue

            // 3x3 박스 검사
            val boxRow = (row / 3) * 3
            val boxCol = (col / 3) * 3
            var validInBox = true
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != row || c != col) && board[r][c] == value) {
                        validInBox = false
                        break
                    }
                }
                if (!validInBox) break
            }
            if (!validInBox) continue

            return value
        }
        return null
    }

    /**
     * 지정된 위치에 유효하지 않은 숫자를 찾는다 (스도쿠 규칙 위반)
     * @param board 현재 게임 보드
     * @param row 행 인덱스
     * @param col 열 인덱스
     * @return 유효하지 않은 숫자 (1-9) 또는 null
     */
    fun findInvalidValue(board: Array<IntArray>, row: Int, col: Int): Int? {
        for (value in 1..9) {
            // 행에서 충돌하는 값 찾기
            for (c in 0..8) {
                if (c != col && board[row][c] == value) {
                    return value
                }
            }

            // 열에서 충돌하는 값 찾기
            for (r in 0..8) {
                if (r != row && board[r][col] == value) {
                    return value
                }
            }

            // 3x3 박스에서 충돌하는 값 찾기
            val boxRow = (row / 3) * 3
            val boxCol = (col / 3) * 3
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != row || c != col) && board[r][c] == value) {
                        return value
                    }
                }
            }
        }
        return null
    }

    /**
     * 특정 셀을 유효한 상태로 복구한다
     * @param viewModel ViewModel 인스턴스
     * @param row 행 인덱스
     * @param col 열 인덱스
     * @param excludeValue 제외할 값 (이미 사용된 값)
     */
    fun recoverCell(viewModel: SudokuViewModel, row: Int, col: Int, excludeValue: Int? = null) {
        viewModel.selectCell(row, col)
        val validValue =
            findValidValueExcluding(viewModel.state.value.board, row, col, excludeValue)
        if (validValue != null) {
            viewModel.setCellValue(validValue)
        } else {
            viewModel.clearCell()
        }
    }

    /**
     * 특정 값을 제외하고 유효한 숫자를 찾는다
     */
    private fun findValidValueExcluding(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        excludeValue: Int?
    ): Int? {
        for (value in 1..9) {
            if (excludeValue != null && value == excludeValue) continue

            // 행 검사
            if (board[row].any { it == value && board[row].indexOf(it) != col }) continue

            // 열 검사  
            if ((0..8).any { r -> r != row && board[r][col] == value }) continue

            // 3x3 박스 검사
            val boxRow = (row / 3) * 3
            val boxCol = (col / 3) * 3
            var foundInBox = false
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if ((r != row || c != col) && board[r][c] == value) {
                        foundInBox = true
                        break
                    }
                }
                if (foundInBox) break
            }
            if (foundInBox) continue

            return value
        }
        return null
    }

    /**
     * 같은 행에 이미 존재하는 숫자를 찾는다 (에러 시나리오 테스트용)
     * @param board 현재 게임 보드
     * @param row 대상 행
     * @return 이미 존재하는 숫자 또는 null
     */
    fun findInvalidValueForRow(board: Array<IntArray>, row: Int): Int? {
        for (value in 1..9) {
            if (board[row].contains(value)) {
                return value
            }
        }
        return null
    }

    /**
     * 여러 개의 실수를 만드는 헬퍼 메서드 (UI 테스트용)
     * @param viewModel ViewModel 인스턴스
     * @param count 만들고자 하는 실수 개수
     * @return 실제로 만들어진 실수 개수
     */
    fun makeDistinctMistakes(viewModel: SudokuViewModel, count: Int): Int {
        val state = viewModel.state.value
        var mistakesMade = 0
        val usedCells = mutableSetOf<Pair<Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                if (mistakesMade >= count) break
                if (!viewModel.isInitialCell(row, col) &&
                    state.board[row][col] == 0 &&
                    !usedCells.contains(Pair(row, col))
                ) {
                    val wrongValue = findInvalidValueForRow(state.board, row)
                    if (wrongValue != null) {
                        viewModel.selectCell(row, col)
                        viewModel.setCellValue(wrongValue)
                        usedCells.add(Pair(row, col))
                        mistakesMade++
                    }
                }
            }
            if (mistakesMade >= count) break
        }
        return mistakesMade
    }

    /**
     * 보드의 깊은 복사본을 생성한다
     */
    fun deepCopyBoard(board: Array<IntArray>): Array<IntArray> {
        return board.map { it.copyOf() }.toTypedArray()
    }

    /**
     * 두 보드가 같은지 비교한다
     */
    fun boardsEqual(board1: Array<IntArray>, board2: Array<IntArray>): Boolean {
        if (board1.size != board2.size) return false
        for (i in board1.indices) {
            if (board1[i].size != board2[i].size) return false
            for (j in board1[i].indices) {
                if (board1[i][j] != board2[i][j]) return false
            }
        }
        return true
    }

    /**
     * 보드에 변경사항이 있는지 확인한다
     */
    fun hasChanges(originalBoard: Array<IntArray>, currentBoard: Array<IntArray>): Boolean {
        return !boardsEqual(originalBoard, currentBoard)
    }
}