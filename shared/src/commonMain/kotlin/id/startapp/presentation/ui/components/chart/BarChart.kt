package id.startapp.presentation.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.BackboneBlueLight
import id.startapp.presentation.ui.theme.ChartGrowthGreen

data class BarDataPoint(
    val label: String,
    val value: Float,
    val isHighlighted: Boolean = false
)

@Composable
fun BarChart(
    dataPoints: List<BarDataPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = BackboneBlue,
    barColorLight: Color = BackboneBlueLight.copy(alpha = 0.4f),
    chartHeight: Int = 160,
    barCornerRadius: Float = 4f
) {
    val maxValue = dataPoints.maxOfOrNull { it.value } ?: 1f

    Column(modifier = modifier.fillMaxWidth()) {
        // Bars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight.dp)
        ) {
            val barCount = dataPoints.size
            val totalSpacing = size.width * 0.3f
            val barSpacing = totalSpacing / (barCount + 1)
            val barWidth = (size.width - totalSpacing) / barCount

            dataPoints.forEachIndexed { index, point ->
                val barHeight = if (maxValue > 0) (point.value / maxValue) * size.height * 0.9f else 0f
                val x = barSpacing + index * (barWidth + barSpacing)
                val y = size.height - barHeight

                drawRoundRect(
                    color = if (point.isHighlighted) barColor else barColorLight,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barCornerRadius, barCornerRadius)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dataPoints.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp
                    ),
                    color = if (point.isHighlighted) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun BarChartCard(
    title: String,
    dataPoints: List<BarDataPoint>,
    modifier: Modifier = Modifier,
    trendText: String? = null,
    trendColor: Color = ChartGrowthGreen,
    barColor: Color = BackboneBlue,
    barColorLight: Color = BackboneBlueLight.copy(alpha = 0.4f)
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (trendText != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = trendText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = trendColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BarChart(
                dataPoints = dataPoints,
                barColor = barColor,
                barColorLight = barColorLight
            )
        }
    }
}
