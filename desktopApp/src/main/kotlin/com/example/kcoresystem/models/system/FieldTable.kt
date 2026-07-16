package com.dev.worker_management_crossplatform.models.system

import androidx.compose.ui.graphics.Color

data class FieldTable(
    val label       : String,
    val width       : Int ,
    var field       : String = "",
    var color       : Color = Color.White,
    val editable    : Boolean = false,
    val type        : String = "text"
)

open class SuperTypeDynamic{

    open fun convert(){

    }
}

class Transformer : SuperTypeDynamic(){

}