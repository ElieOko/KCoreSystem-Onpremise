package com.schoolstats.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.schoolstats.data.local.JvmDatabaseDriverFactory
import com.schoolstats.data.sync.SyncManager
import com.schoolstats.di.appModules
import com.schoolstats.di.jvmModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

fun main() {
    JvmDatabaseDriverFactory.prepareNativeLibraries()
    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "KCoreSystem — Statistiques Scolaires",
        ) {
            KoinApplication(
                application = { modules(appModules + jvmModule) },
            ) {
                ProvideDesktopViewModelStore {
                    DesktopRoot()
                }
            }
        }
    }
}

@Composable
private fun DesktopRoot() {
    val syncManager: SyncManager = koinInject()
    LaunchedEffect(syncManager) {
        runCatching { syncManager.start() }
    }
    AppDesktop()
}

@Composable
private fun ProvideDesktopViewModelStore(content: @Composable () -> Unit) {
    val store = remember { ViewModelStore() }
    val owner = remember(store) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = store
        }
    }
    DisposableEffect(store) {
        onDispose { store.clear() }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content = content)
}
