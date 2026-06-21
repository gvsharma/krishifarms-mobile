package com.krishifarms.mobile.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.Constants
import com.krishifarms.mobile.core.common.Result
import com.krishifarms.mobile.core.security.session.SessionManager
import com.krishifarms.mobile.feature.auth.domain.repository.AuthRepository
import com.krishifarms.mobile.feature.auth.presentation.login.AuthValidationError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SessionState {
    data object Loading : SessionState
    data object Authenticated : SessionState
    data object Unauthenticated : SessionState
}

sealed interface LoginUiState {
    data class Form(
        val mobile: String = "",
        val password: String = "",
        val rememberLogin: Boolean = false,
        val mobileError: AuthValidationError? = null,
        val passwordError: AuthValidationError? = null,
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
    ) : LoginUiState

    data object Success : LoginUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Form())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    init {
        restoreSession()
    }

    fun onMobileChanged(mobile: String) {
        updateForm {
            copy(
                mobile = mobile.filter { it.isDigit() }.take(Constants.MOBILE_NUMBER_LENGTH),
                mobileError = null,
                errorMessage = null,
            )
        }
    }

    fun onPasswordChanged(password: String) {
        updateForm {
            copy(
                password = password,
                passwordError = null,
                errorMessage = null,
            )
        }
    }

    fun onRememberLoginChanged(rememberLogin: Boolean) {
        updateForm { copy(rememberLogin = rememberLogin) }
    }

    fun login() {
        val form = currentForm() ?: return
        val mobileError = validateMobile(form.mobile)
        val passwordError = validatePassword(form.password)

        if (mobileError != null || passwordError != null) {
            updateForm {
                copy(
                    mobileError = mobileError,
                    passwordError = passwordError,
                )
            }
            return
        }

        viewModelScope.launch {
            updateForm { copy(isSubmitting = true, errorMessage = null) }

            when (
                val result = authRepository.login(
                    mobile = form.mobile,
                    password = form.password,
                    rememberLogin = form.rememberLogin,
                )
            ) {
                is Result.Success -> {
                    _loginUiState.value = LoginUiState.Success
                    _sessionState.value = SessionState.Authenticated
                }

                is Result.Error -> {
                    updateForm {
                        copy(
                            isSubmitting = false,
                            errorMessage = result.message,
                        )
                    }
                }

                is Result.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            sessionManager.clear()
            _sessionState.value = SessionState.Unauthenticated
            _loginUiState.value = LoginUiState.Form()
        }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            _sessionState.value = SessionState.Loading
            when (val result = authRepository.restoreSession()) {
                is Result.Success -> {
                    if (result.data != null) {
                        _loginUiState.value = LoginUiState.Success
                        _sessionState.value = SessionState.Authenticated
                    } else {
                        _sessionState.value = SessionState.Unauthenticated
                    }
                }

                is Result.Error -> {
                    _sessionState.value = SessionState.Unauthenticated
                }

                is Result.Loading -> Unit
            }
        }
    }

    private fun validateMobile(mobile: String): AuthValidationError? {
        return when {
            mobile.isBlank() -> AuthValidationError.MOBILE_REQUIRED
            mobile.length != Constants.MOBILE_NUMBER_LENGTH ||
                !mobile.all { it.isDigit() } ||
                !mobile.matches(INDIAN_MOBILE_REGEX) -> AuthValidationError.MOBILE_INVALID
            else -> null
        }
    }

    private fun validatePassword(password: String): AuthValidationError? {
        return when {
            password.isBlank() -> AuthValidationError.PASSWORD_REQUIRED
            password.length < Constants.MIN_PASSWORD_LENGTH -> AuthValidationError.PASSWORD_TOO_SHORT
            else -> null
        }
    }

    private fun updateForm(transform: LoginUiState.Form.() -> LoginUiState.Form) {
        val current = currentForm() ?: return
        _loginUiState.value = current.transform()
    }

    private fun currentForm(): LoginUiState.Form? =
        _loginUiState.value as? LoginUiState.Form

    private companion object {
        val INDIAN_MOBILE_REGEX = Regex("^[6-9]\\d{9}$")
    }
}
