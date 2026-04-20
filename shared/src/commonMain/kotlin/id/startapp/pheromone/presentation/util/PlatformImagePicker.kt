package id.startapp.pheromone.presentation.util

import androidx.compose.runtime.Composable

/**
 * Platform-agnostic image picker launcher.
 *
 * On Android: uses ActivityResultContracts for camera (TakePicturePreview)
 * and gallery (GetContent).
 * On iOS: not yet implemented (no-op).
 */
class ImagePickerLauncher(
    private val onLaunchCamera: () -> Unit,
    private val onLaunchGallery: () -> Unit,
) {
    fun launchCamera() = onLaunchCamera()
    fun launchGallery() = onLaunchGallery()
}

/**
 * Remember an image picker launcher that returns raw image bytes
 * via [onImagePicked] when the user captures or selects a photo.
 */
@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher
