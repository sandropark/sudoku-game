package com.sandro.new_sudoku

class SudokuGame {
    private var board = Array(9) { IntArray(9) { 0 } }
    private var solution = Array(9) { IntArray(9) { 0 } }
    private var initialBoard = Array(9) { IntArray(9) { 0 } }
    
    init {
        generateNewGame()
    }
    
    fun getBoard(): Array<IntArray> = Array(9) { row -> board[row].clone() }
    
    fun getCell(row: Int, col: Int): Int = board[row][col]
    
    fun setCell(row: Int, col: Int, value: Int): Boolean {
        if (initialBoard[row][col] != 0) return false // 초기 숫자는 변경 불가
        if (value < 0 || value > 9) return false // 0-9만 허용 (0은 빈 셀)
        
        // 0이 아닌 경우에만 스도쿠 규칙 검사
        if (value != 0 && !isValidMove(row, col, value)) return false
        
        board[row][col] = value
        return true
    }
    
    fun isInitialCell(row: Int, col: Int): Boolean = initialBoard[row][col] != 0
    
    fun isValidMove(row: Int, col: Int, value: Int): Boolean {
        if (value == 0) return true // 빈 셀은 항상 유효
        
        // 행 검사
        for (c in 0..8) {
            if (c != col && board[row][c] == value) return false
        }
        
        // 열 검사
        for (r in 0..8) {
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
        for (row in 0..8) {
            for (col in 0..8) {
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
        val puzzle = createPuzzleFromComplete(completeBoard)
        board = puzzle.map { it.clone() }.toTypedArray()
        initialBoard = puzzle.map { it.clone() }.toTypedArray()
    }
    
    // 완성된 스도쿠 보드 생성 (랜덤 변형)
    private fun generateRandomCompleteBoard(): Array<IntArray> {
        val baseBoards = listOf(
            arrayOf(
                intArrayOf(5,3,4,6,7,8,9,1,2),
                intArrayOf(6,7,2,1,9,5,3,4,8),
                intArrayOf(1,9,8,3,4,2,5,6,7),
                intArrayOf(8,5,9,7,6,1,4,2,3),
                intArrayOf(4,2,6,8,5,3,7,9,1),
                intArrayOf(7,1,3,9,2,4,8,5,6),
                intArrayOf(9,6,1,5,3,7,2,8,4),
                intArrayOf(2,8,7,4,1,9,6,3,5),
                intArrayOf(3,4,5,2,8,6,1,7,9)
            ),
            arrayOf(
                intArrayOf(8,1,2,7,5,3,6,4,9),
                intArrayOf(9,4,3,6,8,2,1,7,5),
                intArrayOf(6,7,5,4,9,1,2,8,3),
                intArrayOf(1,5,4,2,3,7,8,9,6),
                intArrayOf(3,6,9,8,4,5,7,2,1),
                intArrayOf(2,8,7,1,6,9,5,3,4),
                intArrayOf(5,2,1,9,7,4,3,6,8),
                intArrayOf(4,3,8,5,2,6,9,1,7),
                intArrayOf(7,9,6,3,1,8,4,5,2)
            ),
            arrayOf(
                intArrayOf(1,2,3,4,5,6,7,8,9),
                intArrayOf(4,5,6,7,8,9,1,2,3),
                intArrayOf(7,8,9,1,2,3,4,5,6),
                intArrayOf(2,3,1,5,6,4,8,9,7),
                intArrayOf(5,6,4,8,9,7,2,3,1),
                intArrayOf(8,9,7,2,3,1,5,6,4),
                intArrayOf(3,1,2,6,4,5,9,7,8),
                intArrayOf(6,4,5,9,7,8,3,1,2),
                intArrayOf(9,7,8,3,1,2,6,4,5)
            )
        )
        val baseBoard = baseBoards.random()
        return transformBoard(baseBoard)
    }
    
    private fun transformBoard(board: Array<IntArray>): Array<IntArray> {
        val transformed = board.map { it.clone() }.toTypedArray()
        
        // 랜덤하게 몇 번의 행/열 교환 수행
        repeat((1..3).random()) {
            when ((0..1).random()) {
                0 -> {
                    // 같은 박스 내에서 행 교환
                    val box = (0..2).random() * 3
                    val row1 = box + (0..2).random()
                    val row2 = box + (0..2).random()
                    if (row1 != row2) {
                        val temp = transformed[row1].clone()
                        transformed[row1] = transformed[row2]
                        transformed[row2] = temp
                    }
                }
                1 -> {
                    // 같은 박스 내에서 열 교환
                    val box = (0..2).random() * 3
                    val col1 = box + (0..2).random()
                    val col2 = box + (0..2).random()
                    if (col1 != col2) {
                        for (row in 0..8) {
                            val temp = transformed[row][col1]
                            transformed[row][col1] = transformed[row][col2]
                            transformed[row][col2] = temp
                        }
                    }
                }
            }
        }
        
        return transformed
    }
    
    private fun createPuzzleFromComplete(completeBoard: Array<IntArray>): Array<IntArray> {
        val puzzle = completeBoard.map { it.clone() }.toTypedArray()
        
        // 랜덤하게 셀을 제거 (약 60-70% 제거하여 적당한 난이도 유지)
        val cellsToRemove = (45..55).random() // 81개 셀 중 45-55개 제거
        
        val positions = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
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
    
    private fun generateRandomPuzzle(): Array<IntArray> = createPuzzleFromComplete(generateRandomCompleteBoard())
    
    private fun generateSolution(): Array<IntArray> {
        // 실제로는 스도쿠 해답을 생성하는 알고리즘이 필요
        // 지금은 간단한 예시 해답 사용
        return arrayOf(
            intArrayOf(5,3,4,6,7,8,9,1,2),
            intArrayOf(6,7,2,1,9,5,3,4,8),
            intArrayOf(1,9,8,3,4,2,5,6,7),
            intArrayOf(8,5,9,7,6,1,4,2,3),
            intArrayOf(4,2,6,8,5,3,7,9,1),
            intArrayOf(7,1,3,9,2,4,8,5,6),
            intArrayOf(9,6,1,5,3,7,2,8,4),
            intArrayOf(2,8,7,4,1,9,6,3,5),
            intArrayOf(3,4,5,2,8,6,1,7,9)
        )
    }
    
    fun solveGame() {
        board = solution.map { it.clone() }.toTypedArray()
    }
    
    fun clearBoard() {
        board = initialBoard.map { it.clone() }.toTypedArray()
    }
} 