package id.startapp.pheromone.presentation.ui.components.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Android implementation of GoogleMapView using Maps Compose library
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
    val cameraPositionState = rememberCameraPositionState {
        CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), zoom)
    }

    // Track if initial position has been set
    var initialPositionSet by remember { mutableStateOf(false) }

    // Update camera position when props change
    LaunchedEffect(latitude, longitude, zoom) {
        if (!initialPositionSet) {
            val newLatLng = LatLng(latitude, longitude)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(newLatLng, zoom)
            initialPositionSet = true
        }
    }

    // Use local state for marker position
    var markerPosition by remember { mutableStateOf(LatLng(latitude, longitude)) }

    // Update marker when props change
    LaunchedEffect(latitude, longitude) {
        markerPosition = LatLng(latitude, longitude)
    }

    val mapProperties = remember(showMyLocationButton) {
        MapProperties(
            isMyLocationEnabled = showMyLocationButton
        )
    }

    val mapUiSettings = remember(draggable) {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = showMyLocationButton,
            mapToolbarEnabled = draggable,
            rotationGesturesEnabled = draggable,
            scrollGesturesEnabled = draggable,
            tiltGesturesEnabled = draggable,
            zoomGesturesEnabled = draggable
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapClick = { latLng ->
            if (draggable) {
                markerPosition = latLng
                onLocationChanged(latLng.latitude, latLng.longitude)
            }
        },
        onMapLongClick = { latLng ->
            if (draggable) {
                markerPosition = latLng
                onLocationChanged(latLng.latitude, latLng.longitude)
            }
        }
    )
}
