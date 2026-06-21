package com.krishifarms.mobile.core.navigation

object ProcurementRoutes {
    const val LIST = "procurement/list"
    const val DETAIL = "procurement/detail/{procurementId}"
    const val CREATE = "procurement/create"

    const val ARG_PROCUREMENT_ID = "procurementId"

    fun detail(procurementId: String): String = "procurement/detail/$procurementId"
}
