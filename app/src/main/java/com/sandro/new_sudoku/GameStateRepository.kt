package com.sandro.new_sudoku

import android.content.Context
import android.content.SharedPreferences
import com.sandro.new_sudoku.ui.DifficultyLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Serializable version of SudokuState for persistence
 */
data class SerializableGameState(
    val board: List<List<Int>>,
    val isInitialCells: List<List<Boolean>>,
    val selectedRow: Int,
    val selectedCol: Int,
    val isGameComplete: Boolean,
    val notes: List<List<Set<Int>>>,
    val mistakeCount: Int,
    val isGameOver: Boolean,
    val elapsedTimeSeconds: Int,
    val isTimerRunning: Boolean,
    val highlightedNumber: Int,
    val completedNumbers: Set<Int>,
    val difficulty: DifficultyLevel,
    val initialBoard: List<List<Int>>,
    val solution: List<List<Int>>,
    val timestamp: Long = System.currentTimeMillis()
)

class GameStateRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "sudoku_game_state"
        private const val KEY_HAS_SAVED_GAME = "has_saved_game"
        private const val KEY_BOARD = "board"
        private const val KEY_INITIAL_CELLS = "initial_cells"
        private const val KEY_SELECTED_ROW = "selected_row"
        private const val KEY_SELECTED_COL = "selected_col"
        private const val KEY_IS_GAME_COMPLETE = "is_game_complete"
        private const val KEY_NOTES = "notes"
        private const val KEY_MISTAKE_COUNT = "mistake_count"
        private const val KEY_IS_GAME_OVER = "is_game_over"
        private const val KEY_ELAPSED_TIME = "elapsed_time"
        private const val KEY_IS_TIMER_RUNNING = "is_timer_running"
        private const val KEY_HIGHLIGHTED_NUMBER = "highlighted_number"
        private const val KEY_COMPLETED_NUMBERS = "completed_numbers"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_INITIAL_BOARD = "initial_board"
        private const val KEY_SOLUTION = "solution"
        private const val KEY_TIMESTAMP = "timestamp"
    }

    suspend fun saveGameState(gameState: SerializableGameState) = withContext(Dispatchers.IO) {
        with(prefs.edit()) {
            putBoolean(KEY_HAS_SAVED_GAME, true)
            putString(KEY_BOARD, serializeBoard(gameState.board))
            putString(KEY_INITIAL_CELLS, serializeBooleanBoard(gameState.isInitialCells))
            putInt(KEY_SELECTED_ROW, gameState.selectedRow)
            putInt(KEY_SELECTED_COL, gameState.selectedCol)
            putBoolean(KEY_IS_GAME_COMPLETE, gameState.isGameComplete)
            putString(KEY_NOTES, serializeNotes(gameState.notes))
            putInt(KEY_MISTAKE_COUNT, gameState.mistakeCount)
            putBoolean(KEY_IS_GAME_OVER, gameState.isGameOver)
            putInt(KEY_ELAPSED_TIME, gameState.elapsedTimeSeconds)
            putBoolean(KEY_IS_TIMER_RUNNING, gameState.isTimerRunning)
            putInt(KEY_HIGHLIGHTED_NUMBER, gameState.highlightedNumber)
            putString(KEY_COMPLETED_NUMBERS, gameState.completedNumbers.joinToString(","))
            putString(KEY_DIFFICULTY, gameState.difficulty.name)
            putString(KEY_INITIAL_BOARD, serializeBoard(gameState.initialBoard))
            putString(KEY_SOLUTION, serializeBoard(gameState.solution))
            putLong(KEY_TIMESTAMP, gameState.timestamp)
            apply()
        }
    }

    suspend fun loadGameState(): SerializableGameState? = withContext(Dispatchers.IO) {
        if (!hasSavedGame()) return@withContext null

        try {
            SerializableGameState(
                board = deserializeBoard(prefs.getString(KEY_BOARD, "") ?: ""),
                isInitialCells = deserializeBooleanBoard(
                    prefs.getString(KEY_INITIAL_CELLS, "") ?: ""
                ),
                selectedRow = prefs.getInt(KEY_SELECTED_ROW, -1),
                selectedCol = prefs.getInt(KEY_SELECTED_COL, -1),
                isGameComplete = prefs.getBoolean(KEY_IS_GAME_COMPLETE, false),
                notes = deserializeNotes(prefs.getString(KEY_NOTES, "") ?: ""),
                mistakeCount = prefs.getInt(KEY_MISTAKE_COUNT, 0),
                isGameOver = prefs.getBoolean(KEY_IS_GAME_OVER, false),
                elapsedTimeSeconds = prefs.getInt(KEY_ELAPSED_TIME, 0),
                isTimerRunning = prefs.getBoolean(KEY_IS_TIMER_RUNNING, false),
                highlightedNumber = prefs.getInt(KEY_HIGHLIGHTED_NUMBER, 0),
                completedNumbers = prefs.getString(KEY_COMPLETED_NUMBERS, "")
                    ?.split(",")
                    ?.filter { it.isNotEmpty() }
                    ?.map { it.toInt() }
                    ?.toSet() ?: emptySet(),
                difficulty = DifficultyLevel.valueOf(
                    prefs.getString(
                        KEY_DIFFICULTY,
                        DifficultyLevel.EASY.name
                    ) ?: DifficultyLevel.EASY.name
                ),
                initialBoard = deserializeBoard(prefs.getString(KEY_INITIAL_BOARD, "") ?: ""),
                solution = deserializeBoard(prefs.getString(KEY_SOLUTION, "") ?: ""),
                timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
            )
        } catch (e: Exception) {
            // If there's any error loading the saved state, clear it and return null
            clearSavedGame()
            null
        }
    }

    fun hasSavedGame(): Boolean {
        return prefs.getBoolean(KEY_HAS_SAVED_GAME, false)
    }

    suspend fun clearSavedGame() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }

    private fun serializeBoard(board: List<List<Int>>): String {
        return board.flatten().joinToString(",")
    }

    private fun deserializeBoard(data: String): List<List<Int>> {
        if (data.isEmpty()) return List(9) { List(9) { 0 } }
        val values = data.split(",").map { it.toInt() }
        return values.chunked(9)
    }

    private fun serializeBooleanBoard(board: List<List<Boolean>>): String {
        return board.flatten().map { if (it) "1" else "0" }.joinToString(",")
    }

    private fun deserializeBooleanBoard(data: String): List<List<Boolean>> {
        if (data.isEmpty()) return List(9) { List(9) { false } }
        val values = data.split(",").map { it == "1" }
        return values.chunked(9)
    }

    private fun serializeNotes(notes: List<List<Set<Int>>>): String {
        return notes.flatten().map { set ->
            set.joinToString("|")
        }.joinToString(";")
    }

    private fun deserializeNotes(data: String): List<List<Set<Int>>> {
        if (data.isEmpty()) return List(9) { List(9) { emptySet() } }
        val cellNotes = data.split(";").map { cellData ->
            if (cellData.isEmpty()) emptySet()
            else cellData.split("|").filter { it.isNotEmpty() }.map { it.toInt() }.toSet()
        }
        return cellNotes.chunked(9)
    }
}

/**
 * Extension functions to convert between SudokuState and SerializableGameState
 */
fun SudokuState.toSerializable(
    difficulty: DifficultyLevel,
    initialBoard: Array<IntArray>,
    solution: Array<IntArray>
): SerializableGameState {
    return SerializableGameState(
        board = this.board.map { it.toList() },
        isInitialCells = this.isInitialCells.map { it.toList() },
        selectedRow = this.selectedRow,
        selectedCol = this.selectedCol,
        isGameComplete = this.isGameComplete,
        notes = this.notes.map { row -> row.map { it } },
        mistakeCount = this.mistakeCount,
        isGameOver = this.isGameOver,
        elapsedTimeSeconds = this.elapsedTimeSeconds,
        isTimerRunning = this.isTimerRunning,
        highlightedNumber = this.highlightedNumber,
        completedNumbers = this.completedNumbers,
        difficulty = difficulty,
        initialBoard = initialBoard.map { it.toList() },
        solution = solution.map { it.toList() }
    )
}

fun SerializableGameState.toSudokuState(): SudokuState {
    return SudokuState(
        board = this.board.map { it.toIntArray() }.toTypedArray(),
        isInitialCells = this.isInitialCells.map { it.toBooleanArray() }.toTypedArray(),
        selectedRow = this.selectedRow,
        selectedCol = this.selectedCol,
        isGameComplete = this.isGameComplete,
        notes = this.notes.map { row -> row.map { it }.toTypedArray() }.toTypedArray(),
        mistakeCount = this.mistakeCount,
        isGameOver = this.isGameOver,
        elapsedTimeSeconds = this.elapsedTimeSeconds,
        isTimerRunning = this.isTimerRunning,
        highlightedNumber = this.highlightedNumber,
        completedNumbers = this.completedNumbers
    )
}