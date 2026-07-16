package com.example.kcoresystem.presentation.ui.components.elements.spacer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Space(x: Int = 0, y: Int = 0) {
    when {
        x > 0 && y > 0 -> Spacer(Modifier.width(x.dp).height(y.dp))
        x > 0 -> Spacer(Modifier.width(x.dp))
        y > 0 -> Spacer(Modifier.height(y.dp))
        else -> Spacer(Modifier.width(x.dp).height(y.dp))
    }
}
