package com.krishifarms.mobile.feature.document.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.feature.document.domain.model.Document
import com.krishifarms.mobile.feature.document.domain.repository.DocumentRepository
import com.krishifarms.mobile.feature.document.navigation.DocumentRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentPreviewUiState(
    val document: Document? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class DocumentPreviewViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val documentId: String = savedStateHandle.get<String>(DocumentRoutes.ARG_DOCUMENT_ID)
        .orEmpty()

    private val _uiState = MutableStateFlow(DocumentPreviewUiState())
    val uiState: StateFlow<DocumentPreviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            documentRepository.observeDocument(documentId).collect { document ->
                _uiState.update { it.copy(document = document, isLoading = false) }
            }
        }
    }
}
