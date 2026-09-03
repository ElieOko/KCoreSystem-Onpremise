package com.schoolstats.desktop.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.presentation.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnalyticsScreen(viewModel: DashboardViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val stats = state.stats
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Analyses", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        BarChart(
            title = "Soumissions par statut",
            data = listOf(
                "Validées" to stats.schoolsValidated,
                "Soumises" to stats.schoolsPending,
                "Rejetées" to stats.schoolsRejected,
                "Non soumises" to stats.schoolsNotSubmitted,
            ),
        )
        BarChart(
            title = "Répartition Garçons / Filles",
            data = listOf("Garçons" to stats.totalBoys, "Filles" to stats.totalGirls),
            colors = listOf(Color(0xFF1565C0), Color(0xFFE91E63)),
        )
    }
}

@Composable
private fun BarChart(
    title: String,
    data: List<Pair<String, Int>>,
    colors: List<Color> = listOf(Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFFF57C00), Color(0xFFC62828)),
) {
    val max = data.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            val barWidth = size.width / (data.size * 2f)
            data.forEachIndexed { index, (_, value) ->
                val barHeight = (value.toFloat() / max) * size.height * 0.8f
                val left = index * (barWidth * 2) + barWidth * 0.5f
                drawRoundRect(
                    color = colors[index % colors.size],
                    topLeft = androidx.compose.ui.geometry.Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f),
                )
            }
        }
        data.forEach { (label, value) -> Text("$label: $value") }
    }
}
