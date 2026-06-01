package id.startapp.presentation.util

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current

    // Hold a reference to the temp file URI so the callback can read from it
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraImageFile = remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val uri = cameraImageUri.value
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) onImagePicked(bytes)
            }
        }
        // Clean up the temp file
        cameraImageFile.value?.delete()
        cameraImageUri.value = null
        cameraImageFile.value = null
    }

    // Permission launcher that launches camera on grant
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = cameraImageUri.value
            if (uri != null) {
                cameraLauncher.launch(uri)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) onImagePicked(bytes)
        }
    }

    return remember {
        ImagePickerLauncher(
            onLaunchCamera = {
                val tempFile = File.createTempFile(
                    "camera_photo_",
                    ".jpg",
                    context.cacheDir
                )
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
                cameraImageFile.value = tempFile
                cameraImageUri.value = uri
                // Request CAMERA permission first, then launch on grant
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onLaunchGallery = { galleryLauncher.launch("image/*") }
        )
    }
}
