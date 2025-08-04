package com.sandro.new_sudoku.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class NoteState(
    val isNoteMode: Boolean = false,
    val notes: Array<Array<Set<Int>>> = Array(9) { Array(9) { emptySet() } }
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoteState

        if (isNoteMode != other.isNoteMode) return false
        if (!notes.contentDeepEquals(other.notes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isNoteMode.hashCode()
        result = 31 * result + notes.contentDeepHashCode()
        return result
    }
}

class NoteViewModel : ViewModel() {

    private val _state = MutableStateFlow(NoteState())
    val state: StateFlow<NoteState> = _state

    fun toggleNoteMode() {
        _state.value = _state.value.copy(
            isNoteMode = !_state.value.isNoteMode
        )
    }

    fun addNoteNumber(row: Int, col: Int, number: Int) {
        if (row !in 0..8 || col !in 0..8 || number !in 1..9) return

        val currentNotes = _state.value.notes
        val newNotes = Array(9) { i ->
            Array(9) { j ->
                when {
                    i == row && j == col -> currentNotes[i][j] + number
                    else -> currentNotes[i][j]
                }
            }
        }

        _state.value = _state.value.copy(notes = newNotes)
    }

    fun removeNoteNumber(row: Int, col: Int, number: Int) {
        if (row !in 0..8 || col !in 0..8 || number !in 1..9) return

        val currentNotes = _state.value.notes
        val newNotes = Array(9) { i ->
            Array(9) { j ->
                when {
                    i == row && j == col -> currentNotes[i][j] - number
                    else -> currentNotes[i][j]
                }
            }
        }

        _state.value = _state.value.copy(notes = newNotes)
    }

    fun clearCellNotes(row: Int, col: Int) {
        if (row !in 0..8 || col !in 0..8) return

        val currentNotes = _state.value.notes
        val newNotes = Array(9) { i ->
            Array(9) { j ->
                when {
                    i == row && j == col -> emptySet<Int>()
                    else -> currentNotes[i][j]
                }
            }
        }

        _state.value = _state.value.copy(notes = newNotes)
    }

    fun removeNotesForRelatedCells(row: Int, col: Int, number: Int, board: Array<IntArray>) {
        if (row !in 0..8 || col !in 0..8 || number !in 1..9) return

        val currentNotes = _state.value.notes
        val newNotes = Array(9) { i ->
            Array(9) { j ->
                currentNotes[i][j].toMutableSet()
            }
        }

        // 같은 행의 다른 셀들에서 해당 숫자의 노트 제거
        for (c in 0..8) {
            if (c != col && board[row][c] == 0) {
                newNotes[row][c].remove(number)
            }
        }

        // 같은 열의 다른 셀들에서 해당 숫자의 노트 제거
        for (r in 0..8) {
            if (r != row && board[r][col] == 0) {
                newNotes[r][col].remove(number)
            }
        }

        // 같은 3x3 박스의 다른 셀들에서 해당 숫자의 노트 제거
        val boxStartRow = (row / 3) * 3
        val boxStartCol = (col / 3) * 3
        for (r in boxStartRow until boxStartRow + 3) {
            for (c in boxStartCol until boxStartCol + 3) {
                if ((r != row || c != col) && board[r][c] == 0) {
                    newNotes[r][c].remove(number)
                }
            }
        }

        val finalNotes = Array(9) { i ->
            Array(9) { j ->
                newNotes[i][j].toSet()
            }
        }

        _state.value = _state.value.copy(notes = finalNotes)
    }

    fun getCellNotes(row: Int, col: Int): Set<Int> {
        if (row !in 0..8 || col !in 0..8) return emptySet()
        return _state.value.notes[row][col]
    }

    fun clearAllNotes() {
        _state.value = _state.value.copy(
            notes = Array(9) { Array(9) { emptySet() } }
        )
    }

    fun resetNotes() {
        _state.value = NoteState()
    }

    fun setNotesFromState(notes: Array<Array<Set<Int>>>) {
        val newNotes = Array(9) { i ->
            Array(9) { j ->
                notes[i][j].toSet()
            }
        }
        _state.value = _state.value.copy(notes = newNotes)
    }
}