package com.auditory.trackoccupancy.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auditory.trackoccupancy.data.model.LoginRequest
import com.auditory.trackoccupancy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(login: String, password: String) {
        Log.d("LoginViewModel", "Starting login for user: $login")
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val request = LoginRequest(login, password)
            val result = authRepository.login(request)

            result.fold(
                onSuccess = { response ->
                    Log.d("LoginViewModel", "Login successful, status: ${response.status}")
                    _loginState.value = LoginState.Success
                },
                onFailure = { exception ->
                    Log.e("LoginViewModel", "Login failed", exception)
                    _loginState.value = LoginState.Error(exception.message ?: "Login failed")
                }
            )
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
