package com.example.biblioteca.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biblioteca.data.model.Usuario
import com.example.biblioteca.data.repository.AuthRepository
import com.example.biblioteca.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PerfilState {
    object Loading : PerfilState() // Cargando
    data class Success(val perfil: Usuario) : PerfilState() // Estado exitoso
    data class Error(val message: String) : PerfilState() // Error
}

class PerfilViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    private val _perfilState = MutableStateFlow<PerfilState>(PerfilState.Loading)
    val perfilState: StateFlow<PerfilState> = _perfilState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _perfilState.value = PerfilState.Loading

            val id = authRepository.getCurrentUserId()

            if (id == null) {
                _perfilState.value = PerfilState.Error("No se encontró sesión de usuario.")
                return@launch
            }

            val perfil = usuarioRepository.obtenerPerfil(id)

            if (perfil != null) {
                _perfilState.value = PerfilState.Success(perfil)
            } else {
                _perfilState.value = PerfilState.Error("No se pudo cargar el perfil.")
            }
        }
    }

    fun subirMiIdentificacion(fotoBytes: ByteArray) {
        viewModelScope.launch {
            _perfilState.value = PerfilState.Loading

            val miId = authRepository.getCurrentUserId() ?: run {
                _perfilState.value = PerfilState.Error("No se encontró sesión de usuario.")
                return@launch
            }

            val exito = usuarioRepository.subirIdentificacion(miId, fotoBytes)

            if (exito) {
                cargarPerfil()
            } else {
                _perfilState.value = PerfilState.Error("Error al subir la identificación.")
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}