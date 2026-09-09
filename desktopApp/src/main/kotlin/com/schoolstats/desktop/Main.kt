package com.schoolstats.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.schoolstats.data.sync.SyncManager
import com.schoolstats.di.appModules
import com.schoolstats.di.jvmModule
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

fun main() = application {
    startKoin { modules(appModules + jvmModule) }
    getKoin().get<SyncManager>().start()

    val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "KCoreSystem — Statistiques Scolaires",
    ) {
        AppDesktop()
    }
}
