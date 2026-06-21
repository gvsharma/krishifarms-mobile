package com.krishifarms.mobile.core.navigation

object ExpenseRoutes {
    const val LIST = "expense/list"
    const val DETAIL = "expense/detail/{expenseId}"
    const val FORM = "expense/create"

    const val ARG_EXPENSE_ID = "expenseId"

    fun detail(expenseId: String): String = "expense/detail/$expenseId"
}
