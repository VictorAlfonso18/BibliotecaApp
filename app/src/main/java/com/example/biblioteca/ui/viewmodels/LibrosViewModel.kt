package com.example.biblioteca.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biblioteca.data.model.Libro
import com.example.biblioteca.data.repository.LibroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

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

    fun insertarLibro(libro: Libro) {
        viewModelScope.launch {
            _librosState.value = LibrosState.Loading

            val exito = libroRepository.insertarLibro(libro)

            if (exito) {
                cargarCatalogo()
            } else {
                _librosState.value = LibrosState.Error("Error al guardar libro.")
            }
        }
    }

    fun actualizarLibro(idLibro: String, titulo: String, autor: String, categoria: String, descripcion: String, urlPortada: String, disponible: Int) {
        viewModelScope.launch {
            _librosState.value = LibrosState.Loading

            val exito = libroRepository.actualizarLibro(idLibro, titulo, autor, categoria, descripcion, urlPortada, disponible)

            if (exito) {
                cargarCatalogo()
            } else {
                _librosState.value = LibrosState.Error("Error al actualizar libro.")
            }
        }
    }

    fun eliminarLibro(idLibro: String) {
        viewModelScope.launch {
            _librosState.value = LibrosState.Loading
            val exito = libroRepository.eliminarLibro(idLibro)

            if (exito) {
                cargarCatalogo()
            } else {
                _librosState.value = LibrosState.Error("No se pudo eliminar el libro")
            }
        }
    }

    fun generarDescripcionConIA(
        titulo: String,
        autor: String,
        categoria: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = HttpClient(CIO)
                val apiKey = ""
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

                val body = """
                {
                  "contents": [{
                    "parts": [{"text": "Eres un bibliotecario experto. Genera una sinopsis breve (máximo 1 párrafo) para este libro. Solo devuelve la sinopsis, sin saludos.\n\nTítulo: $titulo, Autor: $autor, Categoría: $categoria."}]
                  }]
                }
            """.trimIndent()

                val response: HttpResponse = client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }

                val json = Json { ignoreUnknownKeys = true }
                val responseText = response.bodyAsText()
                val jsonObj = json.parseToJsonElement(responseText).jsonObject
                val texto = jsonObj["candidates"]
                    ?.jsonArray?.get(0)
                    ?.jsonObject?.get("content")
                    ?.jsonObject?.get("parts")
                    ?.jsonArray?.get(0)
                    ?.jsonObject?.get("text")
                    ?.jsonPrimitive?.content
                    ?: throw Exception("Respuesta vacía de la IA")

                client.close()

                withContext(Dispatchers.Main) { onResult(texto) }

            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    onError("Error (${e.javaClass.simpleName}): ${e.message}")
                }
            }
        }
    }

    fun subirPortadaYGuardarLibro(
        idLibro: String?,
        datosLibro: Triple<String, String, String>,
        descripcion: String,
        urlPortadaActual: String,
        copias: Int,
        imagenBytes: ByteArray?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _librosState.value = LibrosState.Loading

            val urlFinal: String = if (imagenBytes != null) {
                val idParaStorage = if (idLibro.isNullOrBlank())
                    System.currentTimeMillis().toString()
                else
                    idLibro
                libroRepository.subirPortada(idParaStorage, imagenBytes) ?: urlPortadaActual
            } else {
                urlPortadaActual
            }

            val (titulo, autor, categoria) = datosLibro

            val exito = if (idLibro.isNullOrBlank()) {
                libroRepository.insertarLibro(
                    Libro(
                        titulo = titulo, autor = autor, categoria = categoria,
                        descripcion = descripcion, urlPortada = urlFinal, disponible = copias
                    )
                )
            } else {
                libroRepository.actualizarLibro(idLibro, titulo, autor, categoria, descripcion, urlFinal, copias)
            }

            if (exito) {
                cargarCatalogo()
            } else {
                _librosState.value = LibrosState.Error("Error al guardar el libro.")
            }
            onDone()
        }
    }
}
