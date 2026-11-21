package com.uzuu.diuchat.feature.base.presentation.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helper object for creating temporary [FileProvider] URIs used by the camera capture intent.
 */
object MediaFileProvider {
    private const val AUTHORITY_SUFFIX = ".fileprovider"
    private const val CAMERA_DIR = "camera"
    private const val FILE_PREFIX = "diu_cam_"
    private const val FILE_SUFFIX = ".jpg"

    fun createCameraImageUri(context: Context): Uri {
        val cameraDir = File(context.cacheDir, CAMERA_DIR).apply { mkdirs() }
        val photoFile = File.createTempFile(FILE_PREFIX, FILE_SUFFIX, cameraDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}$AUTHORITY_SUFFIX",
            photoFile,
        )
    }
}

/**
 * Controller that exposes Photo Picker ([ActivityResultContracts.PickVisualMedia]) and
 * Camera Intent ([ActivityResultContracts.TakePicture]) launchers for Compose screens.
 */
@Stable
class MediaPickerController(
    private val context: Context,
    private val photoPickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    private val cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    private val onPrepareCameraUri: (Uri) -> Unit,
    private val onError: ((Throwable) -> Unit)?,
) {
    fun launchPhotoPicker() {
        runCatching {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }.onFailure { throwable ->
            onError?.invoke(throwable)
        }
    }

    fun launchCamera() {
        runCatching {
            val uri = MediaFileProvider.createCameraImageUri(context)
            onPrepareCameraUri(uri)
            cameraLauncher.launch(uri)
        }.onFailure { throwable ->
            onError?.invoke(throwable)
        }
    }
}

@Composable
fun rememberMediaPickerController(
    onSelectImage: (Uri) -> Unit,
    onError: ((Throwable) -> Unit)? = null,
): MediaPickerController {
    val context = LocalContext.current
    val currentOnSelectImage by rememberUpdatedState(onSelectImage)
    val currentOnError by rememberUpdatedState(onError)

    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri: Uri? ->
            if (uri != null) {
                currentOnSelectImage(uri)
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
        ) { success: Boolean ->
            val capturedUri = pendingCameraUri
            pendingCameraUri = null
            if (success && capturedUri != null) {
                currentOnSelectImage(capturedUri)
            }
        }

    return remember(context, photoPickerLauncher, cameraLauncher) {
        MediaPickerController(
            context = context,
            photoPickerLauncher = photoPickerLauncher,
            cameraLauncher = cameraLauncher,
            onPrepareCameraUri = { uri -> pendingCameraUri = uri },
            onError = currentOnError,
        )
    }
}
