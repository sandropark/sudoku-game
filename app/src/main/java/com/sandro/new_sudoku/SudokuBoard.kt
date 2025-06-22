package com.sandro.new_sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SudokuBoard(
    board: Array<IntArray>,
    isInitialCells: Array<BooleanArray>,
    selectedRow: Int,
    selectedCol: Int,
    invalidCells: Set<Pair<Int, Int>>,
    notes: Array<Array<Set<Int>>> = Array(9) { Array(9) { emptySet() } },
    isNoteMode: Boolean = false,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.testTag("sudoku_board"),
        contentAlignment = Alignment.Center
    ) {
        val cellSize = remember(maxWidth, maxHeight) {
            (maxWidth / 9f).coerceAtMost(maxHeight / 9f)
        }
        
        // 박스 인덱스 계산을 최적화
        val boxIndices = remember {
            Array(9) { row ->
                Array(9) { col ->
                    (row / 3) * 3 + (col / 3)
                }
            }
        }
        
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            for (row in 0..8) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    for (col in 0..8) {
                        val isSelected = row == selectedRow && col == selectedCol
                        val value = board[row][col]
                        val isInitial = isInitialCells[row][col]
                        val isInvalid = invalidCells.contains(Pair(row, col))
                        val isEvenBox = boxIndices[row][col] % 2 == 0
                        val cellNotes = notes[row][col]
                        val cellTag = "cell_${row}_${col}" + if (!isInitial) "_editable" else ""

                        SudokuCell(
                            value = value,
                            isSelected = isSelected,
                            isInitial = isInitial,
                            isInvalid = isInvalid,
                            notes = cellNotes,
                            isNoteMode = isNoteMode,
                            onClick = { onCellClick(row, col) },
                            cellSize = cellSize,
                            isEvenBox = isEvenBox,
                            modifier = Modifier.testTag(cellTag)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SudokuCell(
    value: Int,
    isSelected: Boolean,
    isInitial: Boolean,
    isInvalid: Boolean,
    notes: Set<Int> = emptySet(),
    isNoteMode: Boolean = false,
    onClick: () -> Unit,
    cellSize: Dp,
    isEvenBox: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = remember(isSelected, isEvenBox) {
        when {
            isSelected -> Color.Blue.copy(alpha = 0.3f)
            isEvenBox -> Color.Gray.copy(alpha = 0.1f)
            else -> Color.White
        }
    }
    
    val textColor = remember(isInitial, value, isInvalid) {
        when {
            isInitial -> Color.Black
            value == 0 -> Color.Gray
            isInvalid -> Color.Red
            else -> Color.Blue
        }
    }
    
    val textWeight = remember(isInitial) {
        if (isInitial) FontWeight.Bold else FontWeight.Normal
    }
    
    val hasNotes = remember(notes) { notes.isNotEmpty() }
    val hasValue = remember(value) { value != 0 }
    
    // 접근성을 위한 contentDescription 생성
    val contentDescription = remember(value, isSelected, isInitial, isInvalid, hasNotes, notes) {
        buildString {
            when {
                hasNotes -> {
                    append("노트 셀, 후보 숫자: ")
                    append(notes.sorted().joinToString(", "))
                }
                hasValue -> {
                    append("숫자 셀, 값: $value")
                    if (isInitial) append(", 초기 숫자")
                    if (isInvalid) append(", 잘못된 숫자")
                }
                else -> append("빈 셀")
            }
            if (isSelected) append(", 선택됨")
        }
    }
    
    Box(
        modifier = modifier
            .size(cellSize)
            .background(backgroundColor)
            .border(
                width = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(0.dp)
            )
            .clickable { onClick() }
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        if (hasNotes) {
            // 노트 표시 최적화
            NotesGrid(
                notes = notes,
                cellSize = cellSize
            )
        } else if (hasValue) {
            // 일반 숫자 표시
            Text(
                text = value.toString(),
                color = textColor,
                fontWeight = textWeight,
                fontSize = (cellSize.value * 0.4f).coerceAtLeast(12f).sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NotesGrid(
    notes: Set<Int>,
    cellSize: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .size((cellSize.value * 0.85f).dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            for (noteRow in 0..2) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    for (noteCol in 0..2) {
                        val noteNum = remember(noteRow, noteCol) { noteRow * 3 + noteCol + 1 }
                        val isVisible = remember(notes, noteNum) { notes.contains(noteNum) }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVisible) {
                                Text(
                                    text = noteNum.toString(),
                                    color = Color.Gray.copy(alpha = 0.8f),
                                    fontSize = (cellSize.value * 0.18f).coerceAtLeast(7f).sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = (cellSize.value * 0.18f).coerceAtLeast(7f).sp,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    modifier = Modifier.testTag("note_${noteNum}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 