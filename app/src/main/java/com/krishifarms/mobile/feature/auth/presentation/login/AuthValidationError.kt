package com.krishifarms.mobile.feature.auth.presentation.login

import androidx.annotation.StringRes
import com.krishifarms.mobile.R

enum class AuthValidationError(@StringRes val messageRes: Int) {
    MOBILE_REQUIRED(R.string.error_mobile_required),
    MOBILE_INVALID(R.string.error_mobile_invalid),
    PASSWORD_REQUIRED(R.string.error_password_required),
    PASSWORD_TOO_SHORT(R.string.error_password_too_short),
}
