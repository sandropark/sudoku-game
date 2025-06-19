package com.sandro.new_sudoku

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.testTag

@Composable
fun SudokuScreen(
    modifier: Modifier = Modifier,
    viewModel: SudokuViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 에러 메시지 표시
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
            
            // 스도쿠 보드
            SudokuBoard(
                board = state.board,
                isInitialCells = state.isInitialCells,
                selectedRow = state.selectedRow,
                selectedCol = state.selectedCol,
                onCellClick = { row, col ->
                    viewModel.selectCell(row, col)
                },
                modifier = Modifier
                    .size(300.dp)
                    .testTag("sudoku_board")
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 숫자 패드
            NumberPad(
                onNumberClick = { number ->
                    viewModel.setCellValue(number)
                },
                onClearClick = {
                    viewModel.clearCell()
                },
                modifier = Modifier.testTag("number_pad")
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 액션 버튼들
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.newGame() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("새 게임")
                }
                
                Button(
                    onClick = { viewModel.solveGame() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("해답 보기")
                }
            }
        }
    }
} 