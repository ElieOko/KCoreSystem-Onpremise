package com.dev.worker_management_crossplatform.presentation.ui.components.core.grid

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MovableContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.dev.worker_management_crossplatform.presentation.ui.components.elements.spacer.Space
import com.dev.worker_management_crossplatform.presentation.ui.components.elements.text.Paragraphe
import com.dev.worker_management_crossplatform.presentation.ui.components.elements.text.Title
import org.jetbrains.compose.resources.painterResource
import worker_management_crossplatform.composeapp.generated.resources.Res
import worker_management_crossplatform.composeapp.generated.resources.blur


@Composable
fun Grid(
    size : Int = 250,
    color : Color = Color(0xFF2187F5).copy(0.77f),
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 0.dp),
    content: @Composable ColumnScope.() -> Unit
){
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        modifier = Modifier.width(size.dp)
    ) {
        Column(modifier) { content() }
    }
}

@Composable
fun GridDetail(){
    Card(
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2187F5).copy(0.77f)
        ),
        modifier = Modifier.width(250.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(17.dp)
        ) {
            Image(
                painterResource(Res.drawable.blur),
                contentDescription = "",
                modifier = Modifier
                    .size(190.dp)
                    .clip(RectangleShape),
                contentScale = ContentScale.Crop)
            Space(y=28)
            Column() {
                Title("Gestion Integrale des personnels")
                Space(y=12)
                Paragraphe("Suivre les differents processus de la gestion des personnels")
            }
            Space(y=34)
        }
    }
}