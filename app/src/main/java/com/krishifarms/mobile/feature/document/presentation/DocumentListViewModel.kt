package com.krishifarms.mobile.feature.document.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.feature.document.domain.model.Document
import com.krishifarms.mobile.feature.document.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentListUiState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val canUpload: Boolean = false,
)

@HiltViewModel
class DocumentListViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    permissionManager: com.krishifarms.mobile.core.security.rbac.PermissionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DocumentListUiState(
            canUpload = com.krishifarms.mobile.core.security.rbac.ActionPermissions
                .from(permissionManager).document.canUpload,
        ),
    )
    val uiState: StateFlow<DocumentListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            documentRepository.observeByEntity(entityType = null, entityId = null)
                .collect { documents ->
                    _uiState.update { it.copy(documents = documents, isLoading = false) }
                }
        }
    }
}
