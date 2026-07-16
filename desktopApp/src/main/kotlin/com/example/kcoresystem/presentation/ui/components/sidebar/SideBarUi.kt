package com.dev.worker_management_crossplatform.presentation.ui.components.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import com.dev.worker_management_crossplatform.presentation.ui.components.core.grid.Grid
import com.dev.worker_management_crossplatform.presentation.ui.components.elements.spacer.Space
import com.dev.worker_management_crossplatform.presentation.ui.components.elements.text.Title
import org.jetbrains.compose.resources.painterResource
import worker_management_crossplatform.composeapp.generated.resources.Res
import worker_management_crossplatform.composeapp.generated.resources.user_add

data class Route(var id : Int = 1, var name : String = ""){
    fun getRoute() = arrayOf<Route>(
        Route(1,"Acceuil"),
        Route(2,"Personnel"),
        Route(3,"Rapport"),
        Route(4,"Presence")
        )
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SideBarUi(value : Boolean = true){
    Grid(color = Color(0xFF2187F5).copy(0.7f), modifier = Modifier.fillMaxHeight(), size = if(value) 150 else 50) {
        Space(y = 10)
        Title("App Mosala", modifier = Modifier.padding(10.dp))
        Space(y = 10)
//                HorizontalDivider(Modifier.fillMaxWidth())
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Route().getRoute().forEach { it ->
                var active by remember { mutableStateOf(false) }
                Surface(modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .onPointerEvent(PointerEventType.Enter){active = true}
                    .onPointerEvent(PointerEventType.Exit){active = false},
                    color =  if(active) Color.White.copy(0.4f) else Color.Unspecified,
                    shape = RoundedCornerShape(8.dp)
                ){
                    Row(horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                        Space(x = 6)
                                Icon(
                                    painterResource(Res.drawable.user_add),
                                    contentDescription = "",
                                    modifier = Modifier.size(22.dp),
                                    tint = Color.White )
                                Space(x = 6)
                        Title(it.name)
                    }
                }
                Space(y = 3)
            }
        }
    }
}