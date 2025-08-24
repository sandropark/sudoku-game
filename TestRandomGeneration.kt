import com.sandro.new_sudoku.SudokuGame

fun main() {
    println("진짜 랜덤 스도쿠 생성 테스트")
    println("=" * 50)

    val boards = mutableSetOf<String>()
    val testCount = 10

    for (i in 1..testCount) {
        val game = SudokuGame()
        val board = game.getBoard()

        // 보드를 문자열로 변환하여 중복 확인
        val boardString = board.joinToString { row ->
            row.joinToString("")
        }
        boards.add(boardString)

        println("\n테스트 $i:")
        println("첫 번째 행: ${board[0].joinToString(" ")}")
        println("완성된 셀 개수: ${board.flatten().count { it != 0 }}")
    }

    println("\n=" * 50)
    println("결과: $testCount 개 중 ${boards.size} 개의 고유한 보드 생성됨")
    println("다양성: ${(boards.size.toDouble() / testCount * 100).toInt()}%")
}

main()
