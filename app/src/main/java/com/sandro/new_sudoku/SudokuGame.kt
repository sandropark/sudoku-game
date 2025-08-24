package com.sandro.new_sudoku

class SudokuGame {
    private var board = Array(9) { IntArray(9) { 0 } }
    private var solution = Array(9) { IntArray(9) { 0 } }
    private var initialBoard = Array(9) { IntArray(9) { 0 } }

    companion object {
        const val BOARD_SIZE = 9
        const val MIN_VALUE = 0
        const val MAX_VALUE = 9
        const val CELLS_TO_REMOVE = 55
        const val MIN_CELLS_TO_KEEP = 26
    }

    init {
        generateNewGame()
    }

    fun getBoard(): Array<IntArray> = Array(BOARD_SIZE) { row -> board[row].clone() }

    fun getInitialBoard(): Array<IntArray> = Array(BOARD_SIZE) { row -> initialBoard[row].clone() }

    fun getSolution(): Array<IntArray> = Array(BOARD_SIZE) { row -> solution[row].clone() }

    fun getCell(row: Int, col: Int): Int {
        require(row in 0 until BOARD_SIZE && col in 0 until BOARD_SIZE) {
            "Invalid cell coordinates: ($row, $col). Must be between 0 and ${BOARD_SIZE - 1}"
        }
        return board[row][col]
    }

    fun setCell(row: Int, col: Int, value: Int): Boolean {
        // 배열 범위 체크
        if (row !in 0 until BOARD_SIZE || col !in 0 until BOARD_SIZE) {
            return false
        }

        // 값 범위 체크
        if (value !in MIN_VALUE..MAX_VALUE) {
            return false
        }

        // 숫자를 항상 입력 (유효성 검사와 관계없이)
        board[row][col] = value
        return true
    }

    fun isInitialCell(row: Int, col: Int): Boolean {
        require(row in 0 until BOARD_SIZE && col in 0 until BOARD_SIZE) {
            "Invalid cell coordinates: ($row, $col). Must be between 0 and ${BOARD_SIZE - 1}"
        }
        return initialBoard[row][col] != 0
    }

    fun getHint(row: Int, col: Int): Int {
        require(row in 0 until BOARD_SIZE && col in 0 until BOARD_SIZE) {
            "Invalid cell coordinates: ($row, $col). Must be between 0 and ${BOARD_SIZE - 1}"
        }
        return solution[row][col]
    }

    fun isCellValid(row: Int, col: Int): Boolean {
        require(row in 0 until BOARD_SIZE && col in 0 until BOARD_SIZE) {
            "Invalid cell coordinates: ($row, $col). Must be between 0 and ${BOARD_SIZE - 1}"
        }

        val value = board[row][col]
        if (value == 0) return true // 빈 셀은 항상 유효

        // 행 검사
        for (c in 0 until BOARD_SIZE) {
            if (c != col && board[row][c] == value) return false
        }

        // 열 검사
        for (r in 0 until BOARD_SIZE) {
            if (r != row && board[r][col] == value) return false
        }

        // 3x3 박스 검사
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != row || c != col) && board[r][c] == value) return false
            }
        }

        return true
    }

    fun isGameComplete(): Boolean {
        for (row in 0 until BOARD_SIZE) {
            for (col in 0 until BOARD_SIZE) {
                if (board[row][col] == 0) return false
            }
        }
        return true
    }

    fun generateNewGame() {
        // 완성된 스도쿠 보드 생성
        val completeBoard = generateRandomCompleteBoard()
        solution = completeBoard.map { it.clone() }.toTypedArray()
        // 퍼즐 생성(일부 셀만 0으로)
        val puzzle = createPuzzleFromComplete(completeBoard, CELLS_TO_REMOVE)
        board = puzzle.map { it.clone() }.toTypedArray()
        initialBoard = puzzle.map { it.clone() }.toTypedArray()
    }

    fun generateNewGameWithDifficulty(difficulty: com.sandro.new_sudoku.ui.DifficultyLevel) {
        // 완성된 스도쿠 보드 생성
        val completeBoard = generateRandomCompleteBoard()
        solution = completeBoard.map { it.clone() }.toTypedArray()
        // 퍼즐 생성(난이도에 따른 셀 제거)
        val puzzle = createPuzzleFromComplete(completeBoard, difficulty.cellsToRemove)
        board = puzzle.map { it.clone() }.toTypedArray()
        initialBoard = puzzle.map { it.clone() }.toTypedArray()
    }

    // 완성된 스도쿠 보드 생성 (백트래킹 기반 진짜 랜덤)
    private fun generateRandomCompleteBoard(): Array<IntArray> {
        val board = Array(9) { IntArray(9) { 0 } }

        // 첫 행을 랜덤으로 채우기 (최적화: 백트래킹 시간 단축)
        board[0] = (1..9).shuffled().toIntArray()

        // 백트래킹으로 나머지 채우기
        fillBoardRecursively(board, 1, 0)

        return board
    }

    // 백트래킹으로 보드 채우기
    private fun fillBoardRecursively(board: Array<IntArray>, row: Int, col: Int): Boolean {
        // 모든 셀 채움 완료
        if (row == 9) return true

        // 다음 셀 위치 계산
        val nextRow = if (col == 8) row + 1 else row
        val nextCol = if (col == 8) 0 else col + 1

        // 1-9를 랜덤 순서로 시도
        val numbers = (1..9).shuffled()

        for (num in numbers) {
            if (isValidPlacement(board, row, col, num)) {
                board[row][col] = num

                if (fillBoardRecursively(board, nextRow, nextCol)) {
                    return true
                }

                // 백트래킹: 실패 시 원복
                board[row][col] = 0
            }
        }

        return false
    }

    // 숫자 배치 유효성 검사
    private fun isValidPlacement(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // 행 검사
        if (board[row].contains(num)) return false

        // 열 검사
        for (r in 0 until 9) {
            if (board[r][col] == num) return false
        }

        // 3x3 박스 검사
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (board[r][c] == num) return false
            }
        }

        return true
    }

    private fun createPuzzleFromComplete(
        completeBoard: Array<IntArray>,
        cellsToRemove: Int = CELLS_TO_REMOVE
    ): Array<IntArray> {
        val puzzle = completeBoard.map { it.clone() }.toTypedArray()

        val positions = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until BOARD_SIZE) {
            for (col in 0 until BOARD_SIZE) {
                positions.add(Pair(row, col))
            }
        }
        positions.shuffle()

        for (i in 0 until cellsToRemove) {
            val (row, col) = positions[i]
            puzzle[row][col] = 0
        }

        return puzzle
    }

    private fun generateRandomPuzzle(): Array<IntArray> =
        createPuzzleFromComplete(generateRandomCompleteBoard())

    private fun generateSolution(): Array<IntArray> {
        // 실제로는 스도쿠 해답을 생성하는 알고리즘이 필요
        // 지금은 간단한 예시 해답 사용
        return arrayOf(
            intArrayOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
            intArrayOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
            intArrayOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
            intArrayOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
            intArrayOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
            intArrayOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
            intArrayOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
            intArrayOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
            intArrayOf(3, 4, 5, 2, 8, 6, 1, 7, 9)
        )
    }

    fun solveGame() {
        board = solution.map { it.clone() }.toTypedArray()
    }

    fun clearBoard() {
        board = initialBoard.map { it.clone() }.toTypedArray()
    }

    fun setBoard(newBoard: Array<IntArray>) {
        require(newBoard.size == BOARD_SIZE) { "Board must be ${BOARD_SIZE}x${BOARD_SIZE}" }
        require(newBoard.all { it.size == BOARD_SIZE }) { "All rows must have ${BOARD_SIZE} columns" }

        board = newBoard.map { it.copyOf() }.toTypedArray()
    }

    // 기존 테스트와의 호환성을 위한 메서드
    fun isValidMove(row: Int, col: Int, value: Int): Boolean {
        if (row !in 0 until BOARD_SIZE || col !in 0 until BOARD_SIZE) {
            return false
        }

        if (value == 0) return true // 빈 셀은 항상 유효

        // 행 검사
        for (c in 0 until BOARD_SIZE) {
            if (c != col && board[row][c] == value) return false
        }

        // 열 검사
        for (r in 0 until BOARD_SIZE) {
            if (r != row && board[r][col] == value) return false
        }

        // 3x3 박스 검사
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != row || c != col) && board[r][c] == value) return false
            }
        }

        return true
    }
} 