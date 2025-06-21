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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
        val cellSize = (maxWidth / 9f).coerceAtMost(maxHeight / 9f)
        
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
                        val boxIndex = (row / 3) * 3 + (col / 3)  // 0부터 8까지의 3x3 박스 인덱스
                        val isEvenBox = boxIndex % 2 == 0
                        val cellNotes = notes[row][col]
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
                            modifier = Modifier.testTag("cell_${row}_${col}")
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
    val backgroundColor = when {
        isSelected -> Color.Blue.copy(alpha = 0.3f)
        isEvenBox -> Color.Gray.copy(alpha = 0.1f)  // 짝수 번째 3x3 박스는 연한 회색 배경
        else -> Color.White  // 홀수 번째 3x3 박스는 흰색 배경
    }
    
    val textColor = when {
        isInitial -> Color.Black
        value == 0 -> Color.Gray
        isInvalid -> Color.Red
        else -> Color.Blue
    }
    
    val textWeight = if (isInitial) FontWeight.Bold else FontWeight.Normal
    
    Box(
        modifier = modifier
            .size(cellSize)
            .background(backgroundColor)
            .border(
                width = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(0.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                color = textColor,
                fontWeight = textWeight,
                fontSize = (cellSize.value * 0.4f).coerceAtLeast(12f).sp,
                textAlign = TextAlign.Center
            )
        } else if (notes.isNotEmpty()) {
            // 후보 숫자 3x3 그리드로 흐리게 표시 (조밀하게 배치하고 여백 추가)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp), // 바깥 여백 추가
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .size((cellSize.value * 0.85f).dp), // 전체 크기를 85%로 줄여서 여백 확보
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    for (noteRow in 0..2) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            for (noteCol in 0..2) {
                                val noteNum = noteRow * 3 + noteCol + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (notes.contains(noteNum)) {
                                        Text(
                                            text = noteNum.toString(),
                                            color = Color.Gray.copy(alpha = 0.8f),
                                            fontSize = (cellSize.value * 0.18f).coerceAtLeast(7f).sp, // 폰트 크기를 더 작게
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
    }
} 