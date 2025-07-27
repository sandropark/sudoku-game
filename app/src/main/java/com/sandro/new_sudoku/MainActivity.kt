package com.sandro.new_sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sandro.new_sudoku.ui.MainScreen
import com.sandro.new_sudoku.ui.MainScreenViewModel
import com.sandro.new_sudoku.ui.theme.SudokuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SudokuTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SudokuApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SudokuApp(
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel(),
    sudokuViewModel: SudokuViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf("main") }
    val mainScreenState by mainScreenViewModel.state.collectAsState()
    val sudokuState by sudokuViewModel.state.collectAsState()

    // 네비게이션 처리
    LaunchedEffect(mainScreenState.shouldNavigateToGame) {
        if (mainScreenState.shouldNavigateToGame) {
            currentScreen = "game"
            mainScreenViewModel.onNavigationCompleted()
        }
    }

    // 게임 종료 시 메인화면으로 이동
    LaunchedEffect(sudokuState.isGameOver) {
        if (sudokuState.isGameOver) {
            currentScreen = "main"
            // 게임이 끝났으므로 진행 중인 게임 상태 초기화
            mainScreenViewModel.resetGameProgress()
        }
    }

    when (currentScreen) {
        "main" -> {
            MainScreen(
                modifier = modifier,
                viewModel = mainScreenViewModel,
                onStartNewGame = { difficulty ->
                    sudokuViewModel.newGameWithDifficulty(difficulty)
                    currentScreen = "game"
                },
                onContinueGame = {
                    currentScreen = "game"
                }
            )
        }

        "game" -> {
            SudokuScreen(
                modifier = modifier,
                viewModel = sudokuViewModel,
                onBackToMain = {
                    currentScreen = "main"
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SudokuAppPreview() {
    SudokuTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SudokuApp(modifier = Modifier.padding(innerPadding))
        }
    }
}