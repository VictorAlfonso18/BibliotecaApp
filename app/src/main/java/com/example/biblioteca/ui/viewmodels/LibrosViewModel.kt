package com.example.biblioteca.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biblioteca.data.model.Libro
import com.example.biblioteca.data.repository.LibroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LibrosState {
    object Loading : LibrosState() // Cargando
    data class Success(val libros: List<Libro>) : LibrosState() // Estado exitoso con la lista adentro
    data class Error(val message: String) : LibrosState() // Error
}

class LibrosViewModel: ViewModel() {

    private val libroRepository = LibroRepository()
    private val _librosState = MutableStateFlow<LibrosState>(LibrosState.Loading)
    val librosState: StateFlow<LibrosState> = _librosState.asStateFlow()

    init {
        cargarCatalogo()
    }

    fun cargarCatalogo() {
        viewModelScope.launch {
            _librosState.value = LibrosState.Loading
            try {
                val lista = libroRepository.obtenerTodosLosLibros()

                _librosState.value = LibrosState.Success(lista)
            } catch (e: Exception) {
                _librosState.value = LibrosState.Error(e.message ?: "Error al cargar el catálogo")
            }
        }
    }

    fun buscarLibro(query: String) {
        viewModelScope.launch {
            _librosState.value = LibrosState.Loading
            try {
                val resultado = libroRepository.buscarLibros(query)
                _librosState.value = LibrosState.Success(resultado)
            } catch (e: Exception) {
                _librosState.value = LibrosState.Error(e.message ?: "Error al buscar libro")
            }
        }
    }

    fun filtrarPorCategoria(categoria: String) {
        viewModelScope.launch {
            _librosState.value = LibrosState.Loading
            try {
                val resultado = libroRepository.filtrarPorCategoria(categoria)
                _librosState.value = LibrosState.Success(resultado)
            } catch (e: Exception) {
                _librosState.value = LibrosState.Error(e.message ?: "Error al filtrar por categoría")
            }
        }
    }
}