package id.startapp.presentation.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.startapp.presentation.util.NumberFormatUtils

/**
 * iOS implementation of GoogleMapView
 *
 * Note: This is a placeholder. For iOS, you should use:
 * - Native MapKit with UIKit wrapper
 * - Or a third-party library like Balto or类似的
 *
 * TODO: Implement actual iOS map view using MapKit
 */
@Composable
actual fun GoogleMapView(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float,
    onLocationChanged: (Double, Double) -> Unit,
    showMyLocationButton: Boolean,
    draggable: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Map not available on iOS yet",
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Lat: ${NumberFormatUtils.formatDouble(latitude)}, Lng: ${NumberFormatUtils.formatDouble(longitude)}",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
