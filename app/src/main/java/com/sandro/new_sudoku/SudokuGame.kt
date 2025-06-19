package com.sandro.new_sudoku

class SudokuGame {
    private var board = Array(9) { IntArray(9) { 0 } }
    private var solution = Array(9) { IntArray(9) { 0 } }
    private var initialBoard = Array(9) { IntArray(9) { 0 } }
    
    init {
        generateNewGame()
    }
    
    fun getBoard(): Array<IntArray> = board.map { it.clone() }.toTypedArray()
    
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
        // 간단한 스도쿠 퍼즐 생성 (실제로는 더 복잡한 알고리즘 필요)
        val puzzle = arrayOf(
            intArrayOf(5,3,0,0,7,0,0,0,0),
            intArrayOf(6,0,0,1,9,5,0,0,0),
            intArrayOf(0,9,8,0,0,0,0,6,0),
            intArrayOf(8,0,0,0,6,0,0,0,3),
            intArrayOf(4,0,0,8,0,3,0,0,1),
            intArrayOf(7,0,0,0,2,0,0,0,6),
            intArrayOf(0,6,0,0,0,0,2,8,0),
            intArrayOf(0,0,0,4,1,9,0,0,5),
            intArrayOf(0,0,0,0,8,0,0,7,9)
        )
        
        board = puzzle.map { it.clone() }.toTypedArray()
        initialBoard = puzzle.map { it.clone() }.toTypedArray()
        solution = generateSolution()
    }
    
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