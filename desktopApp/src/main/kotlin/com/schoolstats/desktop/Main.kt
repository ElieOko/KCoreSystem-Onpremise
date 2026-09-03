package com.schoolstats.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.schoolstats.data.sync.SyncManager
import com.schoolstats.di.appModules
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

fun main() = application {
    startKoin { modules(appModules) }
    getKoin().get<SyncManager>().start()

    Window(
        onCloseRequest = ::exitApplication,
        title = "KCoreSystem — Statistiques Scolaires",
    ) {
        AppDesktop()
    }
}
