package com.sandro.new_sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SudokuScreen(
    modifier: Modifier = Modifier,
    viewModel: SudokuViewModel = viewModel(),
    onBackToMain: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar(onBackClick = onBackToMain)
        StatusBar(mistakeCount = state.mistakeCount)

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            key(
                state.notes.hashCode(),
                state.board.hashCode(),
                state.selectedRow,
                state.selectedCol
            ) {
                SudokuBoard(
                    board = state.board,
                    isInitialCells = state.isInitialCells,
                    selectedRow = state.selectedRow,
                    selectedCol = state.selectedCol,
                    invalidCells = state.invalidCells,
                    notes = state.notes,
                    isNoteMode = state.isNoteMode,
                    onCellClick = { row, col ->
                        viewModel.selectCell(row, col)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .testTag("sudoku_board")
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        ActionBar(viewModel)
        Spacer(Modifier.height(8.dp))
        NumberPad(
            isNoteMode = state.isNoteMode,
            onNumberClick = { number ->
                viewModel.setCellValue(number)
            },
            onNoteNumberClick = { number ->
                viewModel.addNoteNumber(number)
            },
            onClearClick = {
                viewModel.clearCell()
            },
            modifier = Modifier.testTag("number_pad")
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun TopBar(onBackClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기
        Box(
            Modifier
                .size(32.dp)
                .clickable { onBackClick() }
        ) {
            // 아이콘은 실제 프로젝트에 맞게 교체
            Text("←", fontSize = 20.sp, modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.weight(1f))
        Text("스도쿠", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.weight(1f))
        // 설정
        Box(Modifier.size(32.dp)) {
            Text("⚙️", fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun StatusBar(mistakeCount: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("전문가")
        Text("실수: $mistakeCount")
        Text("00:21") // 타이머
    }
}

@Composable
fun ActionBar(viewModel: SudokuViewModel) {
    val state by viewModel.state.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton("실행취소", testTag = "action_btn_실행취소", onClick = { viewModel.onUndo() })
        ActionButton("지우기", testTag = "action_btn_지우기", onClick = { viewModel.clearCell() })
        ActionButton(
            text = if (state.isNoteMode) "노트(ON)" else "노트",
            testTag = "action_btn_노트",
            onClick = { viewModel.toggleNoteMode() }
        )
        ActionButton("힌트", badgeCount = 1)
    }
}

@Composable
fun ActionButton(
    text: String,
    badgeCount: Int = 0,
    testTag: String? = null,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = if (onClick != null) {
                Modifier
                    .clickable { onClick() }
                    .semantics {
                        this.contentDescription = "$text 버튼"
                        this.role = Role.Button
                    }
            } else Modifier
        ) {
            // 실제 아이콘은 프로젝트에 맞게 교체
            Text("⬜", fontSize = 20.sp)
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
        if (badgeCount > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-4).dp)
                    .background(Color.Red, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text(badgeCount.toString(), color = Color.White, fontSize = 10.sp)
            }
        }
    }
} 