package com.sandro.new_sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberPad(
    isNoteMode: Boolean,
    onNumberClick: (Int) -> Unit,
    onNoteNumberClick: (Int) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    completedNumbers: Set<Int> = emptySet()
) {
    Box(
        modifier = modifier.testTag("number_pad")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("number_pad_row"),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (number in 1..9) {
                val isCompleted = completedNumbers.contains(number)
                NumberButton(
                    number = number,
                    onClick = {
                        if (!isCompleted) {
                            if (isNoteMode) onNoteNumberClick(number) else onNumberClick(number)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    isCompleted = isCompleted
                )
            }
        }
    }
}

@Composable
fun NumberButton(
    number: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
) {
    val backgroundColor = if (isCompleted) Color.Gray else Color(0xFF1976D2)
    val borderColor = if (isCompleted) Color.Gray else Color(0xFF1976D2)
    val textColor = if (isCompleted) Color.White.copy(alpha = 0.6f) else Color.White

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .clickable(enabled = !isCompleted) {
                if (!isCompleted) onClick()
            }
            .testTag("number_btn_$number")
            .semantics {
                this.contentDescription = if (isCompleted) {
                    "숫자 $number 버튼 (완성됨)"
                } else {
                    "숫자 $number 버튼"
                }
                this.role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFFFFA500))
            .border(2.dp, Color.Gray)
            .clickable { onClick() }
            .testTag("action_btn_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
} 
