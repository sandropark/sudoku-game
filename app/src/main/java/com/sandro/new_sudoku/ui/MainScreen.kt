package com.sandro.new_sudoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel(),
    onStartNewGame: (DifficultyLevel) -> Unit = { },
    onContinueGame: () -> Unit = { },
    hasGameInProgress: Boolean = false
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 로고
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("main_logo"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "스도쿠",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 메인 버튼들
        MainButtons(
            hasGameInProgress = hasGameInProgress || state.hasGameInProgress,
            onNewGameClick = { viewModel.showDifficultyPopup() },
            onContinueClick = {
                viewModel.continueGame()
                onContinueGame()
            }
        )

        // 난이도 선택 팝업
        if (state.showDifficultyPopup) {
            DifficultySelectionDialog(
                selectedDifficulty = state.selectedDifficulty,
                onDifficultySelected = { difficulty ->
                    viewModel.selectDifficultyAndStartGame(difficulty)
                    onStartNewGame(difficulty)
                },
                onDismiss = { viewModel.hideDifficultyPopup() }
            )
        }
    }
}

@Composable
fun DifficultySelector(
    selectedDifficulty: DifficultyLevel,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DifficultyLevel.values().forEach { difficulty ->
            DifficultyOption(
                difficulty = difficulty,
                isSelected = difficulty == selectedDifficulty,
                onSelected = { onDifficultySelected(difficulty) }
            )
        }
    }
}

@Composable
fun DifficultyOption(
    difficulty: DifficultyLevel,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val testTag = when (difficulty) {
        DifficultyLevel.EASY -> "difficulty_easy"
        DifficultyLevel.MEDIUM -> "difficulty_medium"
        DifficultyLevel.HARD -> "difficulty_hard"
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .selectable(
                selected = isSelected,
                onClick = onSelected,
                role = Role.RadioButton
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = difficulty.displayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = when (difficulty) {
                    DifficultyLevel.EASY -> "쉬움"
                    DifficultyLevel.MEDIUM -> "보통"
                    DifficultyLevel.HARD -> "어려움"
                },
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun MainButtons(
    hasGameInProgress: Boolean,
    onNewGameClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 새 게임 버튼
        Button(
            onClick = onNewGameClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_new_game_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "새 게임",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 이어하기 버튼 (게임이 진행 중일 때만 표시)
        if (hasGameInProgress) {
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("continue_game_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "이어하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DifficultySelectionDialog(
    selectedDifficulty: DifficultyLevel,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "난이도 선택",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("difficulty_dialog_title")
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.testTag("difficulty_selector")
            ) {
                DifficultyLevel.values().forEach { difficulty ->
                    DifficultyDialogOption(
                        difficulty = difficulty,
                        isSelected = difficulty == selectedDifficulty,
                        onSelected = { onDifficultySelected(difficulty) }
                    )
                }
            }
        },
        confirmButton = { },
        dismissButton = { },
        modifier = Modifier.testTag("difficulty_dialog")
    )
}

@Composable
fun DifficultyDialogOption(
    difficulty: DifficultyLevel,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val testTag = when (difficulty) {
        DifficultyLevel.EASY -> "difficulty_easy"
        DifficultyLevel.MEDIUM -> "difficulty_medium"
        DifficultyLevel.HARD -> "difficulty_hard"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = difficulty.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = when (difficulty) {
                        DifficultyLevel.EASY -> "쉬움 (46개 숫자)"
                        DifficultyLevel.MEDIUM -> "보통 (31개 숫자)"
                        DifficultyLevel.HARD -> "어려움 (16개 숫자)"
                    },
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            if (isSelected) {
                Text("✓", fontSize = 20.sp, color = textColor)
            }
        }
    }
} 