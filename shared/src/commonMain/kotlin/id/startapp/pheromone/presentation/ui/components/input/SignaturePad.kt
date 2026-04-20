package id.startapp.pheromone.presentation.ui.components.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue

data class SignatureLine(
    val points: List<Offset>
)

@Composable
fun SignaturePad(
    onSignatureChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Customer Signature",
    clearText: String = "Clear",
    placeholder: String = "Sign here",
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 4f,
    height: Int = 200
) {
    val lines = remember { mutableStateListOf<SignatureLine>() }
    var currentPoints = remember { mutableStateListOf<Offset>() }
    var hasSigned by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    lines.clear()
                    currentPoints.clear()
                    hasSigned = false
                    onSignatureChanged(false)
                }
            ) {
                Text(
                    text = clearText,
                    color = PheromoneBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = Color(0xFFFAFAFA),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder text
            if (!hasSigned) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFBDBDBD)
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints.clear()
                                currentPoints.add(offset)
                                hasSigned = true
                                onSignatureChanged(true)
                            },
                            onDrag = { change, _ ->
                                currentPoints.add(change.position)
                            },
                            onDragEnd = {
                                lines.add(SignatureLine(currentPoints.toList()))
                                currentPoints.clear()
                            }
                        )
                    }
            ) {
                // Draw completed lines
                lines.forEach { line ->
                    if (line.points.size >= 2) {
                        val path = Path().apply {
                            moveTo(line.points.first().x, line.points.first().y)
                            for (i in 1 until line.points.size) {
                                lineTo(line.points[i].x, line.points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Draw current line being drawn
                if (currentPoints.size >= 2) {
                    val path = Path().apply {
                        moveTo(currentPoints.first().x, currentPoints.first().y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
