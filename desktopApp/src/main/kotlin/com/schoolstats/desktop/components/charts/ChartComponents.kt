package com.schoolstats.desktop.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.desktop.components.ChartPalette
import com.schoolstats.desktop.components.LegendDot
import com.schoolstats.desktop.components.SectionCard
import kotlin.math.min

data class ChartDatum(
    val label: String,
    val value: Float,
    val color: Color = ChartPalette.navy,
    val secondary: Float = 0f,
)

@Composable
fun GroupedBarChartCard(
    title: String,
    subtitle: String? = null,
    data: List<ChartDatum>,
    primaryLabel: String = "Garçons",
    secondaryLabel: String = "Filles",
    primaryColor: Color = ChartPalette.boys,
    secondaryColor: Color = ChartPalette.girls,
    modifier: Modifier = Modifier,
) {
    SectionCard(title, subtitle, modifier) {
        if (data.isEmpty() || data.all { it.value <= 0f && it.secondary <= 0f }) {
            Text("Aucune donnée à afficher", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            return@SectionCard
        }
        val max = data.maxOf { maxOf(it.value, it.secondary) }.coerceAtLeast(1f)
        Canvas(Modifier.fillMaxWidth().height(220.dp).padding(top = 8.dp)) {
            val groupWidth = size.width / data.size
            val barWidth = groupWidth * 0.28f
            val gap = barWidth * 0.18f
            val baseline = size.height - 28f
            val usable = baseline - 8f
            data.forEachIndexed { index, item ->
                val groupLeft = index * groupWidth + groupWidth * 0.22f
                val h1 = (item.value / max) * usable
                val h2 = (item.secondary / max) * usable
                drawRoundRect(
                    color = ChartPalette.grid,
                    topLeft = Offset(groupLeft - 4f, 0f),
                    size = Size(barWidth * 2 + gap + 8f, baseline),
                    cornerRadius = CornerRadius(10f, 10f),
                )
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(groupLeft, baseline - h1),
                    size = Size(barWidth, h1),
                    cornerRadius = CornerRadius(8f, 8f),
                )
                drawRoundRect(
                    color = secondaryColor,
                    topLeft = Offset(groupLeft + barWidth + gap, baseline - h2),
                    size = Size(barWidth, h2),
                    cornerRadius = CornerRadius(8f, 8f),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.forEach { Text(it.label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(primaryColor, primaryLabel)
            LegendDot(secondaryColor, secondaryLabel)
        }
    }
}

@Composable
fun DonutChartCard(
    title: String,
    centerLabel: String,
    data: List<ChartDatum>,
    modifier: Modifier = Modifier,
) {
    SectionCard(title, modifier = modifier) {
        val total = data.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0f) {
            Text("Aucune donnée à afficher", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            return@SectionCard
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Canvas(Modifier.height(180.dp).fillMaxWidth(0.42f)) {
                val stroke = 42f
                val diameter = min(size.minDimension - 8f, 168f)
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                var start = -90f
                data.forEach { item ->
                    val sweep = (item.value / total) * 360f
                    drawArc(
                        color = item.color,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = stroke, cap = StrokeCap.Butt),
                    )
                    start += sweep
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Text(centerLabel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                data.forEach { item ->
                    val pct = if (total <= 0f) 0f else item.value / total * 100f
                    LegendDot(item.color, "${item.label}  ${item.value.toInt()}  (${"%.0f".format(pct)}%)")
                }
            }
        }
    }
}

@Composable
fun HorizontalBarChartCard(
    title: String,
    data: List<ChartDatum>,
    modifier: Modifier = Modifier,
) {
    SectionCard(title, modifier = modifier) {
        val max = data.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
        if (data.isEmpty()) {
            Text("Aucune donnée à afficher", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            return@SectionCard
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            data.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Text(item.value.toInt().toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Canvas(Modifier.fillMaxWidth().height(12.dp)) {
                        drawRoundRect(
                            color = ChartPalette.grid,
                            size = size,
                            cornerRadius = CornerRadius(8f, 8f),
                        )
                        drawRoundRect(
                            color = item.color,
                            size = Size(size.width * (item.value / max), size.height),
                            cornerRadius = CornerRadius(8f, 8f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StackedSexBarCard(
    title: String,
    data: List<ChartDatum>,
    modifier: Modifier = Modifier,
) {
    SectionCard(title, modifier = modifier) {
        val max = data.maxOfOrNull { it.value + it.secondary }?.coerceAtLeast(1f) ?: 1f
        if (data.isEmpty()) {
            Text("Aucune donnée à afficher", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            return@SectionCard
        }
        Canvas(Modifier.fillMaxWidth().height(200.dp)) {
            val barWidth = size.width / (data.size * 1.8f)
            data.forEachIndexed { index, item ->
                val left = index * (barWidth * 1.8f) + barWidth * 0.4f
                val menH = (item.value / max) * size.height * 0.82f
                val womenH = (item.secondary / max) * size.height * 0.82f
                val base = size.height - 16f
                drawRoundRect(
                    color = ChartPalette.men,
                    topLeft = Offset(left, base - menH - womenH),
                    size = Size(barWidth, menH),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                drawRoundRect(
                    color = ChartPalette.women,
                    topLeft = Offset(left, base - womenH),
                    size = Size(barWidth, womenH),
                    cornerRadius = CornerRadius(6f, 6f),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.forEach { Text(it.label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(ChartPalette.men, "Hommes")
            LegendDot(ChartPalette.women, "Femmes")
        }
    }
}
