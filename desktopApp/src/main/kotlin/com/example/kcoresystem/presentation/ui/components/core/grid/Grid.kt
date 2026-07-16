package com.example.kcoresystem.presentation.ui.components.core.grid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    size: Int = 200,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.width(size.dp),
        color = color,
        shape = shape,
    ) {
        Column(content = content)
    }
}
