package id.startapp.presentation.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.startapp.presentation.ui.theme.BackboneBlue

data class LineDataPoint(
    val label: String,
    val value: Float
)

@Composable
fun LineChart(
    dataPoints: List<LineDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = BackboneBlue,
    fillColor: Color = BackboneBlue.copy(alpha = 0.1f),
    chartHeight: Int = 160,
    strokeWidth: Float = 3f,
    showDots: Boolean = true,
    dotRadius: Float = 5f
) {
    if (dataPoints.isEmpty()) return

    val maxValue = dataPoints.maxOf { it.value }
    val minValue = dataPoints.minOf { it.value }
    val valueRange = if (maxValue == minValue) 1f else maxValue - minValue

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight.dp)
        ) {
            val paddingHorizontal = 16f
            val paddingVertical = 16f
            val chartWidth = size.width - paddingHorizontal * 2
            val chartHeightPx = size.height - paddingVertical * 2
            val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

            // Calculate points
            val points = dataPoints.mapIndexed { index, point ->
                val x = paddingHorizontal + index * stepX
                val y = paddingVertical + chartHeightPx - ((point.value - minValue) / valueRange) * chartHeightPx
                Offset(x, y)
            }

            // Draw fill gradient
            if (points.size >= 2) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, size.height - paddingVertical)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, size.height - paddingVertical)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor, Color.Transparent),
                        startY = 0f,
                        endY = size.height
                    )
                )
            }

            // Draw line
            if (points.size >= 2) {
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Draw dots
            if (showDots) {
                points.forEach { point ->
                    drawCircle(
                        color = lineColor,
                        radius = dotRadius,
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = dotRadius - 2f,
                        center = point
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dataPoints.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LineChartCard(
    title: String,
    subtitle: String? = null,
    dataPoints: List<LineDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = BackboneBlue
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Canvas(modifier = Modifier.height(8.dp).fillMaxWidth(0.02f)) {
                    drawCircle(color = lineColor, radius = 4f)
                }
                Text(
                    text = "Overall Score",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LineChart(
                dataPoints = dataPoints,
                lineColor = lineColor
            )
        }
    }
}
