package com.example.biblioteca.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biblioteca.data.repository.AuthRepository
import com.example.biblioteca.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados de la pantalla
sealed class AuthState {
    object Idle : AuthState() // Estado inicial
    object Loading : AuthState() // Cargando
    data class Success(val rol: String) : AuthState() // Login o Registro exitoso
    data class Error(val message: String) : AuthState() // Error
}

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Login
    fun iniciarSesion(correo: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val resultado = authRepository.iniciarSesion(correo, pass)

                if (resultado.isFailure) {
                    _authState.value = AuthState.Error(resultado.exceptionOrNull()?.message ?: "Error al iniciar sesión")
                    return@launch
                }

                val userId = authRepository.getCurrentUserId()

                if (userId != null) {
                    val perfil = usuarioRepository.obtenerPerfil(userId)
                    val rolDelUsuario = perfil?.rol ?: "cliente"

                    _authState.value = AuthState.Success(rolDelUsuario)
                } else {
                    _authState.value = AuthState.Error("No se pudo identificar al usuario.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    // Función de Registro
    fun registrarse(nombre: String, correo: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val resultado = authRepository.registrarse(nombre, correo, pass)

                if (resultado.isSuccess) {
                    _authState.value = AuthState.Success("cliente")
                } else {
                    _authState.value = AuthState.Error(resultado.exceptionOrNull()?.message ?: "Error al registrar")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al registrarse")
            }
        }
    }

    // Resetear el estado
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}