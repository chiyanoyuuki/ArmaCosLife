package com.armacos.life.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.armacos.life.domain.Bucket

/** Petit graphe à barres dessiné au Canvas (zéro dépendance externe). */
@Composable
fun BarChart(
    buckets: List<Bucket>,
    barColor: Color,
    modifier: Modifier = Modifier,
    goal: Double? = null,
) {
    val maxValue = (buckets.maxOfOrNull { it.value } ?: 0.0)
        .coerceAtLeast(goal ?: 0.0)
        .coerceAtLeast(1.0)
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        ) {
            if (buckets.isEmpty()) return@Canvas
            val n = buckets.size
            val gap = if (n > 1) size.width * 0.015f else 0f
            val barWidth = (size.width - gap * (n - 1)) / n
            buckets.forEachIndexed { i, b ->
                val barHeight = (b.value / maxValue * size.height).toFloat()
                    .coerceAtLeast(if (b.value > 0) 4f else 0f)
                val x = i * (barWidth + gap)
                drawRoundRect(
                    color = barColor.copy(alpha = if (b.count > 0) 1f else 0.22f),
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth * 0.18f),
                )
            }
            goal?.let { g ->
                if (g in 0.0..maxValue) {
                    val y = (size.height - g / maxValue * size.height).toFloat()
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            buckets.forEach { b ->
                Text(
                    text = b.label,
                    modifier = Modifier.weight(1f),
                    fontSize = 9.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
