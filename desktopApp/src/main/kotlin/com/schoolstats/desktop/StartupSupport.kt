package com.schoolstats.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import org.koin.compose.currentKoinScope

fun Throwable.fullMessage(): String = buildString {
    var current: Throwable? = this@fullMessage
    var depth = 0
    while (current != null && depth < 8) {
        if (depth > 0) appendLine()
        append(current::class.simpleName ?: "Error")
        append(": ")
        append(current.message ?: "(sans message)")
        current = current.cause
        depth++
    }
}

@Composable
inline fun <reified T : ViewModel> rememberSafeViewModel(): Result<T> {
    val scope = currentKoinScope()
    return remember(scope.id) {
        runCatching { scope.get<T>() }
    }
}

@Composable
fun StartupErrorScreen(error: Throwable) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "KCoreSystem n'a pas pu démarrer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                "L'application a rencontré une erreur d'initialisation. " +
                    "Le mode démo (demo@local / demo) reste disponible après correction.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(error.fullMessage(), style = MaterialTheme.typography.bodySmall)
        }
    }
}
