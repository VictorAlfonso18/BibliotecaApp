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
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val rol: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object CorreoEnviado : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun iniciarSesion(correo: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val resultado = authRepository.iniciarSesion(correo, pass)

                if (resultado.isFailure) {
                    val errorAmigable = traducirErrorSupabase(resultado.exceptionOrNull()?.message)
                    _authState.value = AuthState.Error(errorAmigable)
                    return@launch
                }

                val userId = authRepository.getCurrentUserId()

                if (userId != null) {
                    val perfil = usuarioRepository.obtenerPerfil(userId)
                    val rolDelUsuario = perfil?.rol ?: "cliente"
                    _authState.value = AuthState.Success(rolDelUsuario)
                } else {
                    _authState.value = AuthState.Error("No pudimos cargar tu perfil. Inténtalo más tarde.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(traducirErrorSupabase(e.message))
            }
        }
    }

    fun registrarse(nombre: String, correo: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val resultado = authRepository.registrarse(nombre, correo, pass)

                if (resultado.isSuccess) {
                    _authState.value = AuthState.Success("cliente")
                } else {
                    val errorAmigable = traducirErrorSupabase(resultado.exceptionOrNull()?.message)
                    _authState.value = AuthState.Error(errorAmigable)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(traducirErrorSupabase(e.message))
            }
        }
    }

    // Recuperacion de contraseña
    fun recuperarPassword(correo: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val resultado = authRepository.enviarCorreoRecuperacion(correo)
                if (resultado.isSuccess) {
                    _authState.value = AuthState.CorreoEnviado
                } else {
                    val errorAmigable = traducirErrorSupabase(resultado.exceptionOrNull()?.message)
                    _authState.value = AuthState.Error(errorAmigable)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Hubo un problema de conexión. Revisa tu internet.")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // Funcion auxiliar
    private fun traducirErrorSupabase(mensaje: String?): String {
        if (mensaje == null) return "Ocurrió un error inesperado."
        return when {
            mensaje.contains("Invalid login credentials") -> "El correo o la contraseña son incorrectos."
            mensaje.contains("already registered") -> "¡Ups! Este correo ya está registrado en BiblioNet."
            mensaje.contains("Password should be at least") -> "Tu contraseña es muy corta. Usa al menos 6 caracteres."
            mensaje.contains("Unable to validate email") -> "El formato del correo electrónico no es válido."
            else -> "Hubo un error al procesar tu solicitud. Inténtalo de nuevo."
        }
    }
}