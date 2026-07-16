package com.example.kcoresystem.presentation.ui.pages.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.rememberNotification
import androidx.compose.ui.window.rememberTrayState
import com.example.kcoresystem.presentation.ui.components.elements.spacer.Space
import com.example.kcoresystem.presentation.ui.components.elements.text.Title
import org.jetbrains.compose.resources.painterResource

@Composable
fun Login() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) }
    val notification = rememberNotification("Connexion", "Reussie avec success")
    val trayState = rememberTrayState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8))
    ) {
        Column(Modifier.padding(28.dp).fillMaxWidth()) {
            Title("Kisalu", 28, color = Color(0xFF18192F), FontFamily.Cursive)
        }
        Space(y = 20)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            Row {
                Grid(color = Color.White, size = 410, shape =  RoundedCornerShape(topStart = 20.dp, topEnd = 0.dp, bottomEnd = 0.dp, bottomStart = 20.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(40.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Title("SaaS onpremise", color = Color.Black, size = 20)
                            Space(y = 5)
                            Paragraphe("Identifiez-vous pour gérer vos équipes, suivre les temps de travail et optimiser l'organisation quotidienne.",
                                color = Color.Black,
                                textAlign = TextAlign.Unspecified,
                                size = 12
                            )
                            Space(y = 15)
                            Text("* email ou nom d'utilisateur", fontSize = 14.sp)
                            Space(y = 5)
                            CustomOutlinedTextFieldCompact(
                                label = "",
                                value = email ,
                                onValueChange = {email = it},
                                leadingIcon = { Icon(painterResource(Res.drawable.email), contentDescription = "", modifier = Modifier.size(20.dp))}
                            )
                            Space(y=14)
                            Text("* mot de passe", fontSize = 14.sp)
                            Space(y = 5)
                            CustomOutlinedTextFieldCompact(
                                label = "",
                                value = password ,
                                onValueChange = {password = it},
                                leadingIcon = { Icon(painterResource(Res.drawable.password_), contentDescription = "", modifier = Modifier.size(20.dp))}
                            )
                            Space(y=24)

                            Button(
                                shape = RoundedCornerShape(12.dp),

                                onClick = {
                                    trayState.sendNotification(notification)
                                },
                                enabled = isActive,
                                modifier = Modifier.width(250.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E0C10))){
                                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                                    Icon(painterResource(Res.drawable.login), tint =  Color.White,contentDescription = "", modifier = Modifier.size(24.dp))
                                    Text("Se connecter")
                                    Space(x=24)
                                }
                            }
                            Space(y=24)
                            Row(Modifier.fillMaxWidth()) {
                                HorizontalDivider(modifier = Modifier.width(110.dp))
                                Space(12)
                                Text("Ou", modifier = Modifier.absoluteOffset(y= (-10).dp))
                                Space(12)
                                HorizontalDivider(modifier = Modifier.width(110.dp))
                            }
                            Space(y=10)
                            Button(
                                shape = RoundedCornerShape(12.dp),
                                onClick = {},
                                enabled = isActive,
                                modifier = Modifier.width(250.dp), colors = ButtonDefaults.buttonColors(containerColor =Color(0xFF2187F5))){
                                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                                    Icon(painterResource(Res.drawable.user_add), tint =  Color.White,contentDescription = "", modifier = Modifier.size(24.dp))
                                    Text("Ajouter un compte", color = Color.White)
                                    Space(x=24)
                                }
                            }
                        }
                    }
                }
                Grid(size = 200){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(17.dp)
                    ) {
                        Space(y=68)
                        Image(
                            painterResource(Res.drawable.blur),
                            contentDescription = "",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RectangleShape),
                            contentScale = ContentScale.Crop)
                        Space(y=38)
                        Column() {
                            Title("Gestion Integrale des personnels")
                            Space(y=12)
                            Paragraphe("Suivre les differents processus de la gestion des personnels")
                        }
                        Space(y=120)
                    }
                }
            }
        }

    }
}

@Composable
@Preview
fun LoginPrev(){
    Login()
}