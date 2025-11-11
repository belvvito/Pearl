package com.beutystore.pearl.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun register() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            // TODO: Реализовать вызов API
            // Пока просто имитируем успешную регистрацию
            kotlinx.coroutines.delay(1000)
            _state.value = AuthState.Success
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}