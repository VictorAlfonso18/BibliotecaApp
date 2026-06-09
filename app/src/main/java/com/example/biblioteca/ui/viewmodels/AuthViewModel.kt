package com.example.biblioteca.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biblioteca.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados de la pantalla
sealed class AuthState {
    object Idle : AuthState() // Estado inicial
    object Loading : AuthState() // Cargando
    object Success : AuthState() // Login o Registro exitoso
    data class Error(val message: String) : AuthState() // Error
}

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Login
    fun iniciarSesion(correo: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val resultado = authRepository.iniciarSesion(correo, pass)

            if (resultado.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(resultado.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    // Función de Registro
    fun registrarse(nombre: String, correo: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val resultado = authRepository.registrarse(nombre, correo, pass)

            if (resultado.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(resultado.exceptionOrNull()?.message ?: "Error al registrar")
            }
        }
    }

    // Resetear el estado
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}