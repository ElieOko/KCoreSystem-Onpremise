package com.dev.worker_management_crossplatform.presentation.ui.components.elements.spacer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Space(x:Int=0,y:Int =0){
    Spacer(modifier = Modifier.height(y.dp).width(x.dp))
}