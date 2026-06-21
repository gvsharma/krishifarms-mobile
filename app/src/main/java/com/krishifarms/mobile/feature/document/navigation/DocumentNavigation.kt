package com.krishifarms.mobile.feature.document.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.krishifarms.mobile.feature.document.domain.model.DocumentType
import com.krishifarms.mobile.feature.document.presentation.capture.CameraCaptureScreen
import com.krishifarms.mobile.feature.document.presentation.list.DocumentListScreen
import com.krishifarms.mobile.feature.document.presentation.preview.DocumentPreviewScreen
import com.krishifarms.mobile.feature.document.presentation.upload.DocumentUploadScreen

object DocumentRoutes {
    const val UPLOAD = "document/upload"
    const val CAPTURE = "document/capture"
    const val PREVIEW = "document/preview/{documentId}"
    const val LIST = "document/list"

    const val ARG_DOCUMENT_ID = "documentId"
    const val ARG_DOCUMENT_TYPE = "documentType"
    const val ARG_LINKED_ENTITY_TYPE = "linkedEntityType"
    const val ARG_LINKED_ENTITY_ID = "linkedEntityId"

    fun preview(documentId: String) = "document/preview/$documentId"

    fun upload(
        documentType: DocumentType? = null,
        linkedEntityType: String? = null,
        linkedEntityId: String? = null,
    ): String {
        val params = buildList {
            documentType?.let { add("$ARG_DOCUMENT_TYPE=${it.name}") }
            linkedEntityType?.let { add("$ARG_LINKED_ENTITY_TYPE=$it") }
            linkedEntityId?.let { add("$ARG_LINKED_ENTITY_ID=$it") }
        }
        return if (params.isEmpty()) UPLOAD else "$UPLOAD?${params.joinToString("&")}"
    }

    fun capture(
        documentType: DocumentType,
        linkedEntityType: String? = null,
        linkedEntityId: String? = null,
    ): String {
        val params = buildList {
            add("$ARG_DOCUMENT_TYPE=${documentType.name}")
            linkedEntityType?.let { add("$ARG_LINKED_ENTITY_TYPE=$it") }
            linkedEntityId?.let { add("$ARG_LINKED_ENTITY_ID=$it") }
        }
        return "$CAPTURE?${params.joinToString("&")}"
    }
}

fun NavGraphBuilder.documentRoutes(navController: NavHostController) {
    composable(DocumentRoutes.UPLOAD) {
        DocumentUploadScreen(
            onBack = { navController.popBackStack() },
            onNavigateToCamera = { type, entityType, entityId ->
                navController.navigate(DocumentRoutes.capture(type, entityType, entityId))
            },
            onNavigateToPreview = { documentId ->
                navController.navigate(DocumentRoutes.preview(documentId))
            },
        )
    }

    composable(
        route = "${DocumentRoutes.UPLOAD}?${DocumentRoutes.ARG_DOCUMENT_TYPE}={${DocumentRoutes.ARG_DOCUMENT_TYPE}}&${DocumentRoutes.ARG_LINKED_ENTITY_TYPE}={${DocumentRoutes.ARG_LINKED_ENTITY_TYPE}}&${DocumentRoutes.ARG_LINKED_ENTITY_ID}={${DocumentRoutes.ARG_LINKED_ENTITY_ID}}",
        arguments = listOf(
            navArgument(DocumentRoutes.ARG_DOCUMENT_TYPE) {
                type = NavType.StringType
                defaultValue = DocumentType.RECEIPT.name
            },
            navArgument(DocumentRoutes.ARG_LINKED_ENTITY_TYPE) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(DocumentRoutes.ARG_LINKED_ENTITY_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) {
        DocumentUploadScreen(
            onBack = { navController.popBackStack() },
            onNavigateToCamera = { type, entityType, entityId ->
                navController.navigate(DocumentRoutes.capture(type, entityType, entityId))
            },
            onNavigateToPreview = { documentId ->
                navController.navigate(DocumentRoutes.preview(documentId))
            },
        )
    }

    composable(
        route = "${DocumentRoutes.CAPTURE}?${DocumentRoutes.ARG_DOCUMENT_TYPE}={${DocumentRoutes.ARG_DOCUMENT_TYPE}}&${DocumentRoutes.ARG_LINKED_ENTITY_TYPE}={${DocumentRoutes.ARG_LINKED_ENTITY_TYPE}}&${DocumentRoutes.ARG_LINKED_ENTITY_ID}={${DocumentRoutes.ARG_LINKED_ENTITY_ID}}",
        arguments = listOf(
            navArgument(DocumentRoutes.ARG_DOCUMENT_TYPE) {
                type = NavType.StringType
                defaultValue = DocumentType.RECEIPT.name
            },
            navArgument(DocumentRoutes.ARG_LINKED_ENTITY_TYPE) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(DocumentRoutes.ARG_LINKED_ENTITY_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { backStackEntry ->
        val typeName = backStackEntry.arguments?.getString(DocumentRoutes.ARG_DOCUMENT_TYPE)
        val documentType = runCatching { DocumentType.valueOf(typeName ?: DocumentType.RECEIPT.name) }
            .getOrDefault(DocumentType.RECEIPT)
        CameraCaptureScreen(
            documentType = documentType,
            onBack = { navController.popBackStack() },
            onCaptured = { documentId ->
                navController.popBackStack()
                navController.navigate(DocumentRoutes.preview(documentId))
            },
        )
    }

    composable(
        route = DocumentRoutes.PREVIEW,
        arguments = listOf(
            navArgument(DocumentRoutes.ARG_DOCUMENT_ID) { type = NavType.StringType },
        ),
    ) {
        DocumentPreviewScreen(onBack = { navController.popBackStack() })
    }

    composable(DocumentRoutes.LIST) {
        DocumentListScreen(
            onBack = { navController.popBackStack() },
            onUploadClick = { navController.navigate(DocumentRoutes.upload()) },
            onDocumentClick = { documentId ->
                navController.navigate(DocumentRoutes.preview(documentId))
            },
        )
    }
}
