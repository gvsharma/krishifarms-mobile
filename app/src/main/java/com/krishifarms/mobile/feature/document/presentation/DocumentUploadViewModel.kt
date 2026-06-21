package com.krishifarms.mobile.feature.document.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.feature.document.domain.model.Document
import com.krishifarms.mobile.feature.document.domain.model.DocumentType
import com.krishifarms.mobile.feature.document.domain.repository.AddDocumentInput
import com.krishifarms.mobile.feature.document.domain.repository.DocumentRepository
import com.krishifarms.mobile.feature.document.navigation.DocumentRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentUploadUiState(
    val documentType: DocumentType = DocumentType.RECEIPT,
    val linkedEntityType: String? = null,
    val linkedEntityId: String? = null,
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showSourcePicker: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface DocumentUploadEvent {
    data class DocumentSaved(val documentId: String) : DocumentUploadEvent
    data class ShowError(val message: String) : DocumentUploadEvent
}

@HiltViewModel
class DocumentUploadViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialType = savedStateHandle.get<String>(DocumentRoutes.ARG_DOCUMENT_TYPE)
        ?.let { runCatching { DocumentType.valueOf(it) }.getOrNull() }
        ?: DocumentType.RECEIPT
    private val linkedEntityType = savedStateHandle.get<String>(DocumentRoutes.ARG_LINKED_ENTITY_TYPE)
    private val linkedEntityId = savedStateHandle.get<String>(DocumentRoutes.ARG_LINKED_ENTITY_ID)

    private val _uiState = MutableStateFlow(
        DocumentUploadUiState(
            documentType = initialType,
            linkedEntityType = linkedEntityType,
            linkedEntityId = linkedEntityId,
        ),
    )
    val uiState: StateFlow<DocumentUploadUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DocumentUploadEvent>()
    val events: SharedFlow<DocumentUploadEvent> = _events.asSharedFlow()

    init {
        observeDocuments()
    }

    private fun observeDocuments() {
        viewModelScope.launch {
            val flow = if (linkedEntityType != null || linkedEntityId != null) {
                documentRepository.observeByEntity(linkedEntityType, linkedEntityId)
            } else {
                documentRepository.observeByType(initialType)
            }
            flow.collect { documents ->
                _uiState.update { it.copy(documents = documents, isLoading = false) }
            }
        }
    }

    fun onDocumentTypeSelected(type: DocumentType) {
        _uiState.update { it.copy(documentType = type) }
    }

    fun onAddDocumentClick() {
        _uiState.update { it.copy(showSourcePicker = true) }
    }

    fun onDismissSourcePicker() {
        _uiState.update { it.copy(showSourcePicker = false) }
    }

    fun onGalleryImageSelected(uri: android.net.Uri) {
        saveDocument(sourceUri = uri)
    }

    fun onCameraImageCaptured(path: String) {
        saveDocument(sourcePath = path)
    }

    private fun saveDocument(
        sourceUri: android.net.Uri? = null,
        sourcePath: String? = null,
    ) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showSourcePicker = false, errorMessage = null) }
            documentRepository.addDocument(
                AddDocumentInput(
                    type = state.documentType,
                    sourceUri = sourceUri,
                    sourcePath = sourcePath,
                    linkedEntityType = state.linkedEntityType,
                    linkedEntityId = state.linkedEntityId,
                ),
            ).onSuccess { documentId ->
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(DocumentUploadEvent.DocumentSaved(documentId))
            }.onFailure { error ->
                val message = error.message ?: "Unable to save document"
                _uiState.update { it.copy(isSaving = false, errorMessage = message) }
                _events.emit(DocumentUploadEvent.ShowError(message))
            }
        }
    }

    fun retryUpload(documentId: String) {
        viewModelScope.launch {
            documentRepository.uploadDocument(documentId)
        }
    }
}
