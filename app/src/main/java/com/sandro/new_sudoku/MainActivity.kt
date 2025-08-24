package com.sandro.new_sudoku

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.MobileAds
import com.sandro.new_sudoku.ui.MainScreen
import com.sandro.new_sudoku.ui.MainScreenViewModel
import com.sandro.new_sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var sudokuViewModel: SudokuViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // SplashScreen API 초기화 (super.onCreate 이전에 호출)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 스플래시 화면이 표시되는 조건을 설정할 수 있음
        // 예: 앱 초기화가 완료될 때까지 스플래시 유지
        // splashScreen.setKeepOnScreenCondition { false }

        // WebView 디버깅 활성화 (에뮬레이터에서 도움됨)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // AdMob 초기화 (백그라운드 스레드에서)
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {
                println("✅ MobileAds 초기화 완료")
            }
        }

        setContent {
            SudokuTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SudokuApp(
                        modifier = Modifier.padding(innerPadding),
                        onViewModelCreated = { viewModel ->
                            sudokuViewModel = viewModel
                        }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 앱이 백그라운드로 갈 때 타이머 일시정지
        sudokuViewModel?.pauseTimer()
    }

    override fun onStop() {
        super.onStop()
        // 앱이 보이지 않게 될 때 게임 상태 저장
        sudokuViewModel?.saveCurrentGameState()
    }

    override fun onResume() {
        super.onResume()
        // 앱이 다시 활성화될 때 타이머 재시작
        sudokuViewModel?.let { viewModel ->
            val currentState = viewModel.state.value
            if (!currentState.isGameComplete && !currentState.isGameOver && !currentState.isTimerRunning) {
                viewModel.resumeTimer()
            }
        }
    }
}

@Composable
fun SudokuApp(
    modifier: Modifier = Modifier,
    onViewModelCreated: (SudokuViewModel) -> Unit = {}
) {
    val context = LocalContext.current
    val mainScreenViewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(GameStateRepository(context))
    }
    // Activity를 LocalActivity에서 안전하게 찾기
    val activity = LocalActivity.current
    val sudokuViewModel: SudokuViewModel = viewModel {
        SudokuViewModel(GameStateRepository(context), activity ?: context)
    }

    // ViewModel을 Activity에 전달
    LaunchedEffect(sudokuViewModel) {
        onViewModelCreated(sudokuViewModel)
    }
    var currentScreen by remember { mutableStateOf("main") }
    val mainScreenState by mainScreenViewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // 메인화면에서 게임화면으로 네비게이션
    LaunchedEffect(mainScreenState.shouldNavigateToGame) {
        if (mainScreenState.shouldNavigateToGame) {
            currentScreen = "game"
            mainScreenViewModel.onNavigationCompleted()
        }
    }

    // 게임화면에서 메인화면으로 네비게이션 (난이도 변경 시)
    val sudokuState by sudokuViewModel.state.collectAsState()
    LaunchedEffect(sudokuState.shouldNavigateToMain) {
        if (sudokuState.shouldNavigateToMain) {
            currentScreen = "main"
            mainScreenViewModel.resetGameProgress()
            mainScreenViewModel.refreshGameState() // 게임 상태 새로고침
            // shouldNavigateToMain 상태 초기화
            sudokuViewModel.resetNavigationState()
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
                    // 저장된 게임을 복원한 후 게임 화면으로 이동
                    coroutineScope.launch {
                        if (sudokuViewModel.loadSavedGame()) {
                            currentScreen = "game"
                        }
                    }
                }
            )
        }

        "game" -> {
            SudokuScreen(
                modifier = modifier,
                viewModel = sudokuViewModel,
                onBackToMain = {
                    currentScreen = "main"
                    mainScreenViewModel.refreshGameState() // 게임 상태 새로고침
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