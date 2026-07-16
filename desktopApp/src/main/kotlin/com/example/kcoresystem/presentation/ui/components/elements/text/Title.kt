package com.example.kcoresystem.presentation.ui.components.elements.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

@Composable
fun Title(
    text: String,
    size: Int = 16,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontFamily: FontFamily = FontFamily.Default,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontFamily = fontFamily,
    )
}
