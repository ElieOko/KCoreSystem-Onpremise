package com.dev.worker_management_crossplatform.presentation.ui.components.elements.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomOutlinedTextFieldCompact(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable (() -> Unit))? = null,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default, // 👈 AJOUT ICI
) {
    var isFocused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (errorMessage != null) Color.Red else if (isFocused) Color(0xFF6200EE) else Color.Gray,
        label = "border-color"
    )

    val labelOffsetY by animateDpAsState(
        targetValue = if (isFocused || value.isNotEmpty()) (-10).dp else 6.dp,
        label = "label-offset"
    )

    Column(modifier = modifier.width(240.dp)) {
        Box(
            modifier = Modifier
                .height(34.dp)
                .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                .background(Color.White, RoundedCornerShape(6.dp))
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = when {
                    errorMessage != null -> Color.Red
                    isFocused -> Color(0xFF6200EE)
                    else -> Color.Gray
                },
                modifier = Modifier
                    .padding(start = if (leadingIcon != null) 36.dp else 12.dp)
                    .offset(y = labelOffsetY)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Box(modifier = Modifier.padding(end = 4.dp)) {
                        leadingIcon()
                    }
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        },
                    visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    cursorBrush = SolidColor(Color(0xFF6200EE)),
                    decorationBox = { innerTextField -> innerTextField() },
                    keyboardOptions = keyboardOptions // 👈 UTILISÉ ICI
                )


            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, start = 8.dp)
            )
        } else if (supportingText != null) {
            Text(
                text = supportingText,
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, start = 8.dp)
            )
        }
    }
}