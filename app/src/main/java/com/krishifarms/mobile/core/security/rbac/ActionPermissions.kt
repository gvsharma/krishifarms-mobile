package com.krishifarms.mobile.core.security.rbac

data class FarmerActions(
    val canCreate: Boolean,
    val canUpdate: Boolean,
    val canDelete: Boolean,
)

data class ProcurementActions(
    val canCreate: Boolean,
    val canUpdate: Boolean,
    val canApprove: Boolean,
    val canDelete: Boolean,
)

data class ExpenseActions(
    val canCreate: Boolean,
    val canUpdate: Boolean,
    val canApprove: Boolean,
    val canDelete: Boolean,
)

data class WorkerActions(
    val canCreate: Boolean,
    val canUpdate: Boolean,
    val canDelete: Boolean,
    val canManageAttendance: Boolean,
)

data class WorkOrderActions(
    val canCreate: Boolean,
    val canUpdate: Boolean,
    val canComplete: Boolean,
)

data class DocumentActions(
    val canUpload: Boolean,
    val canDelete: Boolean,
)

data class AllActions(
    val farmer: FarmerActions,
    val procurement: ProcurementActions,
    val expense: ExpenseActions,
    val worker: WorkerActions,
    val workOrder: WorkOrderActions,
    val document: DocumentActions,
)

object ActionPermissions {
    fun from(permissionManager: PermissionManager): AllActions = AllActions(
        farmer = FarmerActions(
            canCreate = permissionManager.has(Permission.FARMER_CREATE),
            canUpdate = permissionManager.has(Permission.FARMER_UPDATE),
            canDelete = permissionManager.has(Permission.FARMER_DELETE),
        ),
        procurement = ProcurementActions(
            canCreate = permissionManager.has(Permission.PROCUREMENT_CREATE),
            canUpdate = permissionManager.has(Permission.PROCUREMENT_UPDATE),
            canApprove = permissionManager.has(Permission.PROCUREMENT_APPROVE),
            canDelete = permissionManager.has(Permission.PROCUREMENT_DELETE),
        ),
        expense = ExpenseActions(
            canCreate = permissionManager.has(Permission.EXPENSE_CREATE),
            canUpdate = permissionManager.has(Permission.EXPENSE_UPDATE),
            canApprove = permissionManager.has(Permission.EXPENSE_APPROVE),
            canDelete = permissionManager.has(Permission.EXPENSE_DELETE),
        ),
        worker = WorkerActions(
            canCreate = permissionManager.has(Permission.WORKER_CREATE),
            canUpdate = permissionManager.has(Permission.WORKER_UPDATE),
            canDelete = permissionManager.has(Permission.WORKER_DELETE),
            canManageAttendance = permissionManager.has(Permission.ATTENDANCE_UPDATE),
        ),
        workOrder = WorkOrderActions(
            canCreate = permissionManager.has(Permission.WORK_ORDER_CREATE),
            canUpdate = permissionManager.has(Permission.WORK_ORDER_UPDATE),
            canComplete = permissionManager.has(Permission.WORK_ORDER_COMPLETE),
        ),
        document = DocumentActions(
            canUpload = permissionManager.has(Permission.DOCUMENT_CREATE),
            canDelete = permissionManager.has(Permission.DOCUMENT_DELETE),
        ),
    )
}
