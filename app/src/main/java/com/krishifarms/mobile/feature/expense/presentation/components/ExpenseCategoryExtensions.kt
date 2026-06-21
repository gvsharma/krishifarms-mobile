package com.krishifarms.mobile.feature.expense.presentation.components

import androidx.annotation.StringRes
import com.krishifarms.mobile.R
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.feature.expense.domain.model.PaymentMethod

@StringRes
fun ExpenseCategory.labelRes(): Int = when (this) {
    ExpenseCategory.FUEL -> R.string.expense_category_fuel
    ExpenseCategory.LABOUR -> R.string.expense_category_labour
    ExpenseCategory.REPAIRS -> R.string.expense_category_repairs
    ExpenseCategory.FERTILIZERS -> R.string.expense_category_fertilizers
    ExpenseCategory.SEEDS -> R.string.expense_category_seeds
    ExpenseCategory.TRANSPORT -> R.string.expense_category_transport
    ExpenseCategory.MISCELLANEOUS -> R.string.expense_category_miscellaneous
}

@StringRes
fun PaymentMethod.labelRes(): Int = when (this) {
    PaymentMethod.CASH -> R.string.expense_payment_cash
    PaymentMethod.UPI -> R.string.expense_payment_upi
    PaymentMethod.BANK_TRANSFER -> R.string.expense_payment_bank_transfer
    PaymentMethod.CHEQUE -> R.string.expense_payment_cheque
    PaymentMethod.OTHER -> R.string.expense_payment_other
}
