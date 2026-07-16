package com.dev.worker_management_crossplatform.presentation.ui.pages.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.worker_management_crossplatform.presentation.ui.components.elements.spacer.Space
import com.example.kcoresystem.presentation.ui.components.sidebar.SideBarUi
import com.example.kcoresystem.presentation.ui.components.tables.TableCompose
import org.jetbrains.compose.resources.painterResource
import worker_management_crossplatform.composeapp.generated.resources.Res
import worker_management_crossplatform.composeapp.generated.resources.menu
import worker_management_crossplatform.composeapp.generated.resources.user_story

@Composable
fun Home(){

    val expanded = remember { mutableStateOf(true) }
    Column(Modifier.padding(10.dp)){
        Row {
            //SIDEBAR
            SideBarUi(expanded.value)
            Column(Modifier.padding(10.dp)) {
                //CONTENT Page
                Surface(modifier = Modifier.padding(10.dp).fillMaxWidth(),color = Color(0xFF2187F5).copy(0.77f), shape = RoundedCornerShape(12.dp)) {
                    Row( Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row {
                            IconButton(onClick = {
                                expanded.value = !expanded.value
                            }, colors = IconButtonDefaults.iconButtonColors(Color.White.copy(0.4f)) ){
                                Icon(painterResource(Res.drawable.menu), null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Space(x = 20)
                            Text("App Mosala", color = Color.White, fontSize = 34.sp)
                        }
                        Row {
                            Column(Modifier.padding(2.dp)) {
                                IconButton(onClick = {},  colors = IconButtonDefaults.iconButtonColors(Color.White.copy(0.4f))){
                                    Icon(painterResource(Res.drawable.user_story), null, tint = Color.White, modifier = Modifier.size(34.dp))
                                }
                                Text("@elieoko", color = Color.White)
                            }
                        }

                    }
                }
              //  PersonnelMain()

                TableCompose(!expanded.value)
            }
        }
    }
}