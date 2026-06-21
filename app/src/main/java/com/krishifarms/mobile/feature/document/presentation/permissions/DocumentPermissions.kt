package com.krishifarms.mobile.feature.document.presentation.permissions

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

enum class DocumentPermissionRequest {
    CAMERA,
    GALLERY,
}

@Composable
fun rememberDocumentPermissionState(): DocumentPermissionState {
    val context = LocalContext.current
    return remember {
        DocumentPermissionState(
            hasCameraPermission = {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA,
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            },
            hasGalleryPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_IMAGES,
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                }
            },
        )
    }
}

class DocumentPermissionState(
    private val hasCameraPermission: () -> Boolean,
    private val hasGalleryPermission: () -> Boolean,
) {
    var pendingRequest by mutableStateOf<DocumentPermissionRequest?>(null)
        private set

    fun needsCameraPermission(): Boolean = !hasCameraPermission()

    fun needsGalleryPermission(): Boolean = !hasGalleryPermission()

    fun markPending(request: DocumentPermissionRequest) {
        pendingRequest = request
    }

    fun clearPending() {
        pendingRequest = null
    }

    fun permissionFor(request: DocumentPermissionRequest): String {
        return when (request) {
            DocumentPermissionRequest.CAMERA -> Manifest.permission.CAMERA
            DocumentPermissionRequest.GALLERY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            }
        }
    }
}
