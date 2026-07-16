package com.dev.worker_management_crossplatform.presentation.ui.components.elements.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun Title(text: String, size : Int = 14, color: Color = Color.White, fontFamily: FontFamily = FontFamily.SansSerif, modifier : Modifier = Modifier){
    Text(text,
        fontSize = size.sp,
        color = color,
        fontWeight = FontWeight.Bold,
        fontFamily = fontFamily,
        textAlign = TextAlign.Center, modifier = modifier)
}