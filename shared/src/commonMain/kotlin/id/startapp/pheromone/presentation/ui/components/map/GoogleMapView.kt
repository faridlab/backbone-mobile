package id.startapp.pheromone.presentation.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Google Maps View Component
 *
 * Expect declaration for platform-specific implementations.
 * On Android: Uses actual Google Maps with Maps Compose library
 * On iOS: Shows a placeholder (to be implemented with Apple Maps)
 *
 * @param modifier Modifier for the composable
 * @param latitude Initial latitude for the map camera
 * @param longitude Initial longitude for the map camera
 * @param zoom Initial zoom level (1-21)
 * @param onLocationChanged Callback when user selects a location (lat, lng)
 * @param showMyLocationButton Whether to show the "My Location" button
 * @param draggable Whether the map is interactive (drag/zoom)
 */
@Composable
expect fun GoogleMapView(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float = 15f,
    onLocationChanged: (latitude: Double, longitude: Double) -> Unit = { _, _ -> },
    showMyLocationButton: Boolean = true,
    draggable: Boolean = true
)

/**
 * Google Maps State for managing camera and markers
 */
data class GoogleMapState(
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val zoom: Float = 15f
) {
    /**
     * Update camera position
     */
    fun updatePosition(lat: Double, lng: Double, zoomLevel: Float = zoom): GoogleMapState {
        return copy(latitude = lat, longitude = lng, zoom = zoomLevel)
    }

    companion object {
        /**
         * Default Jakarta coordinates
         */
        fun jakarta() = GoogleMapState(
            latitude = -6.2088,
            longitude = 106.8456,
            zoom = 12f
        )
    }
}
