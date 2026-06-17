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
    object Loading : PrestamosState()
    data class Success(val prestamos: List<Prestamo>) : PrestamosState()
    data class Error(val message: String) : PrestamosState()
}

class PrestamosViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val prestamoRepository = PrestamoRepository()
    private val usuarioRepository = UsuarioRepository()

    private val _prestamosState = MutableStateFlow<PrestamosState>(PrestamosState.Loading)
    val prestamosState: StateFlow<PrestamosState> = _prestamosState.asStateFlow()

    // Carga los préstamos del alumno autenticado
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

    // Permite al alumno solicitar la reserva de un libro
    fun solicitarNuevoPrestamo(idLibro: String?) {
        viewModelScope.launch {
            _prestamosState.value = PrestamosState.Loading

            val miId = authRepository.getCurrentUserId() ?: run {
                _prestamosState.value = PrestamosState.Error("No se encontró sesión de usuario.")
                return@launch
            }

            val perfil = usuarioRepository.obtenerPerfil(miId)

            if (perfil == null || !perfil.verificado) {
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

    // Carga las solicitudes pendientes para validación del administrador
    fun cargarPrestamosPendientes() {
        viewModelScope.launch {
            _prestamosState.value = PrestamosState.Loading
            _prestamosState.value = PrestamosState.Success(prestamoRepository.obtenerPrestamosPendientes())
        }
    }

    // Carga el listado completo de registros históricos
    fun cargarTodosLosPrestamos() {
        viewModelScope.launch {
            _prestamosState.value = PrestamosState.Loading
            _prestamosState.value = PrestamosState.Success(prestamoRepository.obtenerTodosLosPrestamos())
        }
    }

    // Procesa el escaneo del código QR para avanzar en el ciclo de vida del préstamo
    fun procesarCodigoQR(idPrestamoEscaneado: String) {
        viewModelScope.launch {
            _prestamosState.value = PrestamosState.Loading

            val todosPrestamos = prestamoRepository.obtenerTodosLosPrestamos()
            val prestamo = todosPrestamos.find { it.id == idPrestamoEscaneado }

            if (prestamo == null) {
                _prestamosState.value = PrestamosState.Error("Préstamo no encontrado.")
                return@launch
            }

            val nuevoEstado = when (prestamo.estado) {
                "pendiente" -> "activo"
                "activo" -> "devuelto"
                else -> {
                    _prestamosState.value = PrestamosState.Error("Este préstamo ya fue devuelto o tiene un estado no válido.")
                    return@launch
                }
            }

            // Realiza la llamada al repositorio que se encarga de inyectar las marcas de tiempo automatizadas
            val exito = prestamoRepository.actualizarEstadoPrestamo(idPrestamoEscaneado, nuevoEstado)

            if (exito) {
                cargarTodosLosPrestamos()
            } else {
                _prestamosState.value = PrestamosState.Error("Error al actualizar el préstamo en la base de datos.")
            }
        }
    }
}