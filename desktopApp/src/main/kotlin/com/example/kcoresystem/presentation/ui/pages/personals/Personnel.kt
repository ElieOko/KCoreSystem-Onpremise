package com.dev.worker_management_crossplatform.presentation.ui.pages.personals

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Tab
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dev.worker_management_crossplatform.presentation.ui.components.elements.text.Title

@Composable
fun Personnel(){
    Column {
        Title("AQ")
        Title("AQ")
        Title("AQ")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonnelMain(){
    Column {
        PrimaryTabRow(selectedTabIndex = 0, containerColor = Color.Black){
            Tab(
                selected = true,
                onClick = {},
                modifier = Modifier,
                text = {
                    Title("Tab 1")
                }
            )
            Tab(
                selected = true,
                onClick = {},
                modifier = Modifier,
                text = {
                    Title("Tab 2")
                }
            )
            Tab(
                selected = true,
                onClick = {},
                modifier = Modifier,
                text = {
                    Title("Tab 3")
                }
            )
        }

    }
}
