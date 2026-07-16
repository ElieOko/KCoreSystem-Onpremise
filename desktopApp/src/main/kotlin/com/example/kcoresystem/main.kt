package com.example.kcoresystem

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val nameDevise = InetAddress.getLocalHost().hostName
    val notification = rememberNotification("Version prod", "Reussie avec success")
    val trayState = rememberTrayState()
    Tray(
        state =  trayState,
        icon = painterResource(Res.drawable.compose_multiplatform),
        menu = {
            Item("version", onClick = {
                trayState.sendNotification(notification)
            })
        }
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "Mosala",
    ) {
        MenuBar {
            Menu("Fichier", 'F'){
                Item("Copy", onClick = {}, shortcut = KeyShortcut(Key.C, ctrl = true))
            }
            Menu("Config", 'F'){
                Menu("Appareil Associe", ){
                    Item(nameDevise.toString(), onClick = {}, shortcut = KeyShortcut(Key.C, ctrl = true))
                }
                Item("Serveur", onClick = {})
            }
            Menu("Report", 'F'){
                Item("Copy", onClick = {}, shortcut = KeyShortcut(Key.C, ctrl = true))
            }
            Menu("Apropos", 'F'){
                Item("Copy", onClick = {}, shortcut = KeyShortcut(Key.C, ctrl = true))
                Item("Documentation", onClick = {}, shortcut = KeyShortcut(Key.C, ctrl = true))
            }

        }
        //Home()
        Login()
//        AppDesktop()
    }
}