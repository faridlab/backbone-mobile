package id.startapp.pheromone.presentation.ui.components.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue

@Composable
fun PhotoUploadArea(
    photos: List<String>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxPhotos: Int = 5,
    title: String? = null,
    subtitle: String? = null,
    height: Int = 120
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PheromoneBlue
            )
        }

        if (photos.isEmpty()) {
            // Empty state - dashed border upload area
            DashedUploadBox(
                onClick = onAddPhoto,
                height = height
            )
        } else {
            // Photo row with add button
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(photos) { photo ->
                    PhotoThumbnail(
                        photoUri = photo,
                        onRemove = { onRemovePhoto(photos.indexOf(photo)) }
                    )
                }

                if (photos.size < maxPhotos) {
                    item {
                        AddPhotoButton(onClick = onAddPhoto)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashedUploadBox(
    onClick: () -> Unit,
    height: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = PheromoneBlue.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                PheromoneBlue.copy(alpha = 0.03f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Add Photo",
                tint = PheromoneBlue.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Add Photo Evidence",
                style = MaterialTheme.typography.bodyMedium,
                color = PheromoneBlue.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photoUri: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE0E0E0))
    ) {
        // Placeholder for photo
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Photo",
            tint = Color.Gray,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.Center)
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPhotoButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = PheromoneBlue.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Add",
                tint = PheromoneBlue,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "ADD",
                style = MaterialTheme.typography.labelSmall,
                color = PheromoneBlue
            )
        }
    }
}
