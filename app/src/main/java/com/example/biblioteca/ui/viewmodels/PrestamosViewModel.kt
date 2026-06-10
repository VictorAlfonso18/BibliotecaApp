package com.example.biblioteca.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biblioteca.data.model.Prestamo
import com.example.biblioteca.data.repository.AuthRepository
import com.example.biblioteca.data.repository.PrestamoRepository
import com.example.biblioteca.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PrestamosState {
    object Loading : PrestamosState() // Cargando
    data class Success(val prestamos: List<Prestamo>) : PrestamosState() // Estado exitoso con la lista adentro
    data class Error(val message: String) : PrestamosState() // Error
}

class PrestamosViewModel: ViewModel() {
    private val authRepository = AuthRepository()
    private val prestamoRepository = PrestamoRepository()
    private val usuarioRepository = UsuarioRepository()

    private val _prestamosState = MutableStateFlow<PrestamosState>(PrestamosState.Loading)
    val prestamosState: StateFlow<PrestamosState> = _prestamosState.asStateFlow()

    fun cargarMisPrestamos() {
        viewModelScope.launch {
            _prestamosState.value = PrestamosState.Loading

            val id = authRepository.getCurrentUserId()

            if (id == null) {
                _prestamosState.value = PrestamosState.Error("No se encontró sesión de usuario.")
                return@launch
            }

            val prestamos = prestamoRepository.obtenerMisPrestamos(id)
            _prestamosState.value = PrestamosState.Success(prestamos)
        }
    }

    fun solicitarNuevoPrestamo(idLibro: String) {
        viewModelScope.launch {
            _prestamosState.value = PrestamosState.Loading

            val miId = authRepository.getCurrentUserId() ?: run {
                _prestamosState.value = PrestamosState.Error("No se encontró sesión de usuario.")
                return@launch
            }

            val perfil = usuarioRepository.obtenerPerfil(miId)

            if (perfil == null || perfil.verificado == false){
                _prestamosState.value = PrestamosState.Error("Debes subir tu identificación primero.")
                return@launch
            }

            val nuevoPrestamo = Prestamo(usuarioId = miId, libroId = idLibro, estado = "pendiente")
            val exito = prestamoRepository.solicitarPrestamo(nuevoPrestamo)

            if (exito) {
                cargarMisPrestamos()
            } else {
                _prestamosState.value = PrestamosState.Error("Error al solicitar prestamo.")
            }
        }
    }

    fun cargarPrestamosPendientes() {
        viewModelScope.launch {
            _prestamosState.value = PrestamosState.Loading
            _prestamosState.value = PrestamosState.Success(prestamoRepository.obtenerPrestamosPendientes())
        }
    }

    fun procesarCodigoQR(idPrestamoEscaneado: String) {
        viewModelScope.launch {
            _prestamosState.value = PrestamosState.Loading

            val estadoActual = (prestamosState.value as? PrestamosState.Success)
                ?.prestamos
                ?.find { it.id == idPrestamoEscaneado }
                ?.estado

            val nuevoEstado = when (estadoActual) {
                "pendiente" -> "activo"
                "activo" -> "devuelto"
                else -> {
                    _prestamosState.value = PrestamosState.Error("Estado de préstamo no válido.")
                    return@launch
                }
            }

            val exito = prestamoRepository.actualizarEstadoPrestamo(idPrestamoEscaneado, nuevoEstado)

            if (exito) {
                cargarPrestamosPendientes()
            } else {
                _prestamosState.value = PrestamosState.Error("Error al procesar el préstamo.")
            }
        }
    }
}