package id.startapp.pheromone.presentation.ui.components.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue

@Composable
fun CameraViewfinder(
    onGalleryClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Align Barcode within frame",
    subtitle: String = "The scanner will detect automatically"
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        // Camera placeholder background
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Scan frame
            Canvas(
                modifier = Modifier
                    .size(250.dp)
            ) {
                val frameSize = size.minDimension
                val cornerLength = frameSize * 0.15f
                val strokeWidth = 4f

                // Draw corner brackets
                val corners = listOf(
                    // Top-left
                    Pair(Offset(0f, cornerLength), Offset(0f, 0f)),
                    Pair(Offset(0f, 0f), Offset(cornerLength, 0f)),
                    // Top-right
                    Pair(Offset(frameSize - cornerLength, 0f), Offset(frameSize, 0f)),
                    Pair(Offset(frameSize, 0f), Offset(frameSize, cornerLength)),
                    // Bottom-left
                    Pair(Offset(0f, frameSize - cornerLength), Offset(0f, frameSize)),
                    Pair(Offset(0f, frameSize), Offset(cornerLength, frameSize)),
                    // Bottom-right
                    Pair(Offset(frameSize - cornerLength, frameSize), Offset(frameSize, frameSize)),
                    Pair(Offset(frameSize, frameSize), Offset(frameSize, frameSize - cornerLength))
                )

                corners.forEach { (start, end) ->
                    drawLine(
                        color = PheromoneBlue,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Inner subtle border
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.15f),
                    size = Size(frameSize, frameSize),
                    cornerRadius = CornerRadius(8f),
                    style = Stroke(width = 1f)
                )
            }
        }

        // Top instruction text
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        // Bottom action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onGalleryClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "GALLERY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Scanner button (center)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(PheromoneBlue.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Help button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onHelpClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "HELP",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
