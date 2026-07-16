package com.dev.worker_management_crossplatform.presentation.ui.components.elements.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun Paragraphe(text : String, size : Int = 12, color: Color = Color.White, fontFamily: FontFamily = FontFamily.SansSerif, textAlign: TextAlign = TextAlign.Center){
    Text(text, fontSize = size.sp, style = TextStyle.Default.copy(
        lineBreak = LineBreak.Paragraph
    ), color = color, fontFamily = fontFamily, textAlign = textAlign,)
}