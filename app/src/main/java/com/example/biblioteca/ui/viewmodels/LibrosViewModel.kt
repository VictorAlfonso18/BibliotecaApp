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
import com.example.biblioteca.BuildConfig

// Definimos los estados posibles de la pantalla (Cargando, Éxito con datos o Error)
sealed class LibrosState {
    object Loading : LibrosState()
    data class Success(val libros: List<Libro>) : LibrosState()
    data class Error(val message: String) : LibrosState()
}

class LibrosViewModel: ViewModel() {

    private val libroRepository = LibroRepository()

    // El StateFlow maneja el estado de la UI de forma reactiva
    private val _librosState = MutableStateFlow<LibrosState>(LibrosState.Loading)
    val librosState: StateFlow<LibrosState> = _librosState.asStateFlow()

    // Al arrancar el ViewModel, cargamos el catálogo por defecto
    init {
        cargarCatalogo()
    }

    // Trae todos los libros desde Supabase
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

    // Busca libros en la base de datos según el nombre o autor tecleado
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

    // Aplica el filtro por categorías (por ejemplo, Ciencia ficción o Clásico)
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

    // Inserta un nuevo objeto Libro en la base de datos
    fun insertarLibro(libro: Libro) {
        viewModelScope.launch {
            _librosState.value = LibrosState.Loading
            val exito = libroRepository.insertarLibro(libro)
            if (exito) {
                cargarCatalogo() // Refrescamos la lista automáticamente
            } else {
                _librosState.value = LibrosState.Error("Error al guardar libro.")
            }
        }
    }

    // Actualiza los datos de un libro existente pasándole todos sus campos correspondientes
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

    // Elimina un libro del catálogo mediante su identificador único
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

    //  AQUÍ ESTÁ EL CAMBIO: Conectamos con el API de Gemini para autocompletar la sinopsis
    fun generarDescripcionConIA(
        titulo: String,
        autor: String,
        categoria: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Ejecutamos la petición HTTP en un hilo secundario (IO) para no congelar la pantalla
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = HttpClient(CIO)
                val apiKey = BuildConfig.GEMINI_API_KEY
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

                /* EXPLICACIÓN DEL CORTE: Aquí le indicamos explícitamente a Gemini en el prompt
                   que genere una sinopsis de MÁXIMO 180 CARACTERES. Hicimos esto porque las columnas
                   por defecto en las bases de datos (como VARCHAR) rechazan textos muy largos,
                   provocando errores al guardar si la IA se extiende demasiado.
                */
                val body = """
                {
                  "contents": [{
                    "parts": [{"text": "Eres un bibliotecario experto. Genera una sinopsis extremadamente breve (MÁXIMO 180 CARACTERES) para este libro. Debe ser un resumen directo y conciso. Solo devuelve la sinopsis, sin saludos ni comillas adicionales.\n\nTítulo: $titulo, Autor: $autor, Categoría: $categoria."}]
                  }]
                }
            """.trimIndent()

                val response: HttpResponse = client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }

                // Desglosamos el JSON de respuesta de Google para extraer únicamente el texto limpio
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

                // Regresamos al hilo principal de la UI para pintar el texto generado en la caja de descripción
                withContext(Dispatchers.Main) { onResult(texto) }

            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    onError("Error (${e.javaClass.simpleName}): ${e.message}")
                }
            }
        }
    }

    // Coordina la subida de la imagen a Storage y posteriormente guarda el registro en la base de datos
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

            // Si el administrador eligió una foto nueva, la subimos a Supabase Storage y obtenemos la URL pública
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

            // Evaluamos si es un libro nuevo (insertar) o una edición (actualizar)
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
                cargarCatalogo() // Refrescamos el feed de administración
            } else {
                _librosState.value = LibrosState.Error("Error al guardar el libro.")
            }
            onDone() // Notificamos al diálogo para cerrarse
        }
    }
}