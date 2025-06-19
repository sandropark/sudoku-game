package com.sandro.new_sudoku

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SudokuScreen(
    modifier: Modifier = Modifier,
    viewModel: SudokuViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar()
        StatusBar()
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            SudokuBoard(
                board = state.board,
                isInitialCells = state.isInitialCells,
                selectedRow = state.selectedRow,
                selectedCol = state.selectedCol,
                onCellClick = { row, col ->
                    viewModel.selectCell(row, col)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .testTag("sudoku_board")
            )
        }
        Spacer(Modifier.height(8.dp))
        ActionBar()
        Spacer(Modifier.height(8.dp))
        NumberPad(
            onNumberClick = { number ->
                viewModel.setCellValue(number)
            },
            onClearClick = {
                viewModel.clearCell()
            },
            modifier = Modifier.testTag("number_pad")
        )
        Spacer(Modifier.height(8.dp))
        if (state.showError) {
            Text(
                text = state.errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("error_message")
            )
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기
        Box(Modifier.size(32.dp)) {
            // 아이콘은 실제 프로젝트에 맞게 교체
            Text("←", fontSize = 20.sp, modifier = Modifier.align(Alignment.Center))
        }
        // 팔레트(테마)
        Box(Modifier.size(32.dp)) {
            Text("🎨", fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.weight(1f))
        Text("데일리 챌린지", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.weight(1f))
        // 설정
        Box(Modifier.size(32.dp)) {
            Text("⚙️", fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("실수: 0")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🏆", fontSize = 14.sp)
            Text(" 11월.22")
        }
        Text("00:21")
    }
}

@Composable
fun ActionBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton("실행 취소")
        ActionButton("지우기")
        ActionButton("노트")
        ActionButton("힌트", badgeCount = 1)
    }
}

@Composable
fun ActionButton(text: String, badgeCount: Int = 0) {
    Box {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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