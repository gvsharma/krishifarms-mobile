package com.krishifarms.mobile.feature.document.presentation.capture

import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.util.DocumentFileManager
import com.krishifarms.mobile.core.util.camera.CameraXPreview
import com.krishifarms.mobile.core.util.camera.captureImage
import com.krishifarms.mobile.feature.document.domain.model.DocumentType
import com.krishifarms.mobile.feature.document.presentation.DocumentUploadEvent
import com.krishifarms.mobile.feature.document.presentation.DocumentUploadViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
    documentType: DocumentType,
    onBack: () -> Unit,
    onCaptured: (String) -> Unit,
    viewModel: DocumentUploadViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    LaunchedEffect(documentType) {
        viewModel.onDocumentTypeSelected(documentType)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DocumentUploadEvent.DocumentSaved -> onCaptured(event.documentId)
                is DocumentUploadEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.document_capture_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val capture = imageCapture ?: return@FloatingActionButton
                    if (isCapturing || uiState.isSaving) return@FloatingActionButton
                    isCapturing = true
                    scope.launch {
                        runCatching {
                            val captureFile = File(
                                File(context.filesDir, DocumentFileManager.DOCUMENTS_DIR),
                                "${documentType.storagePrefix}_capture_${System.currentTimeMillis()}.jpg",
                            ).apply { parentFile?.mkdirs() }
                            captureImage(capture, captureFile, context)
                            viewModel.onCameraImageCaptured(captureFile.absolutePath)
                        }.onFailure {
                            snackbarHostState.showSnackbar(
                                it.message ?: context.getString(R.string.common_error),
                            )
                        }
                        isCapturing = false
                    }
                },
            ) {
                Icon(Icons.Default.Camera, contentDescription = stringResource(R.string.document_capture_button))
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CameraXPreview(
                modifier = Modifier.fillMaxWidth(),
                onImageCaptureReady = { imageCapture = it },
            )
            if (isCapturing || uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
