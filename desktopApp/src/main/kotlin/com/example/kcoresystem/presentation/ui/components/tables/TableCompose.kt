package com.dev.worker_management_crossplatform.presentation.ui.components.tables

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.worker_management_crossplatform.models.core.Grade
import com.dev.worker_management_crossplatform.models.system.FieldTable
import com.dev.worker_management_crossplatform.presentation.ui.components.elements.spacer.Space
import kotlinx.coroutines.launch


@Composable
fun TableCompose(expanded : Boolean = false){
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color.Black), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp).width(IntrinsicSize.Min), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            HeaderCompose(
                expanded,
                arrayListOf<FieldTable>(
                    FieldTable(label = "N", width = 35),
                    FieldTable(label = "NOM", width = 52),
                    FieldTable(label = "POSTNOM", width = 22),
                    FieldTable(label = "PRENOM", width = 12),
                    FieldTable(label = "GENRE", width = 5),
                    FieldTable(label = "ETAT-CIVIL", width = 5),
                    FieldTable(label = "DATE DE NAISSANCE", width = 5),
                    FieldTable(label = "FONCTION", width = 22),
                    FieldTable(label = "GRADE", width = 22),
                    FieldTable(label = "SERVICE", width = 12),
                    FieldTable(label = "QUALIFICATION", width = 5),
                    FieldTable(label = "ACTIONS", width = 68),
                )
            )
          BodyTableCompose()

        }
    }

}

@Composable
fun HeaderCompose(expanded: Boolean, titles: ArrayList<FieldTable> = arrayListOf<FieldTable>()){
    var i = 0
    val stateScroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    Column(Modifier.width(IntrinsicSize.Min).fillMaxWidth()) {
        HorizontalDivider(thickness = 2.dp, color = Color.White.copy(0.4f))
        Row(Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            //.horizontalScroll(stateScroll)
//            .draggable(orientation = Orientation.Horizontal, state = rememberDraggableState { delta->
//                scope.launch {
//                    stateScroll.scrollBy(-delta)
//                }
//            })
        ){
            VerticalDivider(thickness = 2.dp, color = Color.White.copy(0.4f))
            titles.forEach {
                i++
                Space(x = 8)
                Text(it.label, fontWeight = FontWeight.Bold, color = Color.White)
               // if (i != titles.size){
                    Space(x = if(expanded) it.width + 10 else it.width)
                    VerticalDivider(thickness = 2.dp, color = Color.White.copy(0.4f))
               // }
            }

           // VerticalDivider(thickness = 2.dp, color = Color.White.copy(0.4f))
        }
        HorizontalDivider(thickness = 2.dp, color = Color.White.copy(0.4f))
        //Modifier.width(1270.dp)

    }
}

@Composable
fun BodyTableCompose(items : ArrayList<Any> = arrayListOf<Any>()){
    var i = 0
    Column(Modifier) {
        HorizontalDivider(thickness = 1.dp)
        Row(Modifier.height(IntrinsicSize.Min)) {
            VerticalDivider(thickness = 2.dp)
            items.forEach {
                i++
                Space(x = 8)
                Text(it.toString(), fontWeight = FontWeight.Bold)
                if (i != items.size){
                    Space(x = 48)
                    VerticalDivider(thickness = 2.dp)
                }
            }
            Space(x = 34)
            VerticalDivider(thickness = 2.dp)
        }
        HorizontalDivider(Modifier.width(970.dp),thickness = 1.dp)
    }
}