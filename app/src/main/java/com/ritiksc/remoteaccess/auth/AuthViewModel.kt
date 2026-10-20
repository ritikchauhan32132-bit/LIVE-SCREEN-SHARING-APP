package com.ritiksc.remoteaccess.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritiksc.remoteaccess.common.Result
import com.ritiksc.remoteaccess.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState())
    val loginState: StateFlow<UiState<Unit>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<Unit>>(UiState())
    val registerState: StateFlow<UiState<Unit>> = _registerState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<UiState<Unit>>(UiState())
    val resetPasswordState: StateFlow<UiState<Unit>> = _resetPasswordState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val displayName: String?
        get() = authRepository.currentUser?.displayName

    val userEmail: String?
        get() = authRepository.currentUser?.email

    init {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                _isLoggedIn.value = user != null
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _loginState.value = UiState.error("Please enter your email")
            return
        }
        if (password.isBlank()) {
            _loginState.value = UiState.error("Please enter your password")
            return
        }

        viewModelScope.launch {
            _loginState.value = UiState.loading()
            when (val result = authRepository.login(email.trim(), password)) {
                is Result.Success -> {
                    _loginState.value = UiState.success(Unit)
                }
                is Result.Error -> {
                    _loginState.value = UiState.error(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String, displayName: String) {
        if (displayName.isBlank()) {
            _registerState.value = UiState.error("Please enter your name")
            return
        }
        if (email.isBlank()) {
            _registerState.value = UiState.error("Please enter your email")
            return
        }
        if (password.length < 6) {
            _registerState.value = UiState.error("Password must be at least 6 characters")
            return
        }
        if (password != confirmPassword) {
            _registerState.value = UiState.error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _registerState.value = UiState.loading()
            when (val result = authRepository.register(email.trim(), password, displayName.trim())) {
                is Result.Success -> {
                    _registerState.value = UiState.success(Unit)
                }
                is Result.Error -> {
                    _registerState.value = UiState.error(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = UiState.error("Please enter your email")
            return
        }

        viewModelScope.launch {
            _resetPasswordState.value = UiState.loading()
            when (val result = authRepository.sendPasswordReset(email.trim())) {
                is Result.Success -> {
                    _resetPasswordState.value = UiState.success(Unit)
                }
                is Result.Error -> {
                    _resetPasswordState.value = UiState.error(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _loginState.value = UiState()
        _registerState.value = UiState()
    }

    fun clearLoginError() {
        _loginState.value = UiState()
    }

    fun clearRegisterError() {
        _registerState.value = UiState()
    }

    fun clearResetError() {
        _resetPasswordState.value = UiState()
    }
}
