package com.example.biblioteca.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biblioteca.data.model.Usuario
import com.example.biblioteca.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UsuariosState {
    object Loading : UsuariosState()
    data class Success(val usuarios: List<Usuario>) : UsuariosState()
    data class Error(val message: String) : UsuariosState()
}

class UsuariosViewModel : ViewModel() {

    private val usuarioRepository = UsuarioRepository()
    private val _usuariosState = MutableStateFlow<UsuariosState>(UsuariosState.Loading)
    val usuariosState: StateFlow<UsuariosState> = _usuariosState.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _usuariosState.value = UsuariosState.Loading
            try {
                val lista = usuarioRepository.obtenerTodosLosUsuarios()
                _usuariosState.value = UsuariosState.Success(lista)
            } catch (e: Exception) {
                _usuariosState.value = UsuariosState.Error(e.message ?: "Error al cargar los usuarios")
            }
        }
    }

    fun aprobarUsuario(idUsuario: String) {
        viewModelScope.launch {
            _usuariosState.value = UsuariosState.Loading
            try {
                val exito = usuarioRepository.validarUsuario(idUsuario)

                if (exito) {
                    cargarUsuarios()
                } else {
                    _usuariosState.value = UsuariosState.Error("No se pudo aprobar la verificación")
                }
            } catch (e: Exception) {
                _usuariosState.value = UsuariosState.Error(e.message ?: "Error al intentar aprobar al usuario")
            }
        }
    }
}