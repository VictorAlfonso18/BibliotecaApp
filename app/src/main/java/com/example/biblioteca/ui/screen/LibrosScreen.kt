package com.example.biblioteca.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.biblioteca.data.model.Libro
import com.example.biblioteca.ui.viewmodels.LibrosViewModel
import com.example.biblioteca.ui.viewmodels.LibrosState
import com.example.biblioteca.ui.viewmodels.PrestamosViewModel
import com.example.biblioteca.ui.viewmodels.PrestamosState
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrosScreen(
    viewModel: LibrosViewModel,
    prestamosViewModel: PrestamosViewModel
) {
    val estado by viewModel.librosState.collectAsState()
    val estadoPrestamo by prestamosViewModel.prestamosState.collectAsState()

    var mensaje by remember { mutableStateOf("") }
    var solicitudEnviada by remember { mutableStateOf(false) }

    var textoBusqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }

    LaunchedEffect(estadoPrestamo) {
        if (estadoPrestamo is PrestamosState.Error) {
            mensaje = (estadoPrestamo as PrestamosState.Error).message
            solicitudEnviada = false
        } else if (estadoPrestamo is PrestamosState.Success && solicitudEnviada) {
            mensaje = "¡Préstamo solicitado exitosamente!"
            solicitudEnviada = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Biblioteca Digital",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF2F4F4F),
            modifier = Modifier.align(Alignment.Start)
        )

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            val colorMensaje = if (mensaje.contains("Error") || mensaje.contains("Debes")) Color.Red else Color(0xFF2E7D32)
            Text(text = mensaje, color = colorMensaje)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            label = { Text("Buscar por nombre o autor...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (estado) {
            is LibrosState.Loading -> {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = Color(0xFF2F4F4F))
            }
            is LibrosState.Error -> {
                val errorMsg = (estado as LibrosState.Error).message
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Error al cargar: $errorMsg", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.cargarCatalogo() }, colors = ButtonDefaults.buttonColors(contentColor = Color.Black)) {
                    Text("Reintentar")
                }
            }
            is LibrosState.Success -> {
                val librosBD = (estado as LibrosState.Success).libros

                if (librosBD.isEmpty()) {
                    Text("No hay libros disponibles en el catálogo en este momento.")
                } else {

                    // ========================================================
                    // 🔥 MAGIA AQUÍ: GENERAR LAS ETIQUETAS DE FORMA DINÁMICA
                    // ========================================================
                    val listaCategorias = remember(librosBD) {
                        // 1. Agarramos las categorías de los libros, quitamos nulos o vacíos y limpiamos espacios.
                        val categoriasDeLaBD = librosBD.mapNotNull { libro ->
                            libro.categoria?.trim()?.lowercase()?.replaceFirstChar { it.uppercase() }
                        }.filter { it.isNotEmpty() }.distinct() // distinct() elimina repetidos

                        // 2. Le pegamos "Todos" al inicio de la lista dinámica
                        listOf("Todos") + categoriasDeLaBD.sorted()
                    }

                    // Si por alguna razón la categoría seleccionada ya no existe en la lista, la reseteamos a "Todos"
                    if (categoriaSeleccionada !in listaCategorias) {
                        categoriaSeleccionada = "Todos"
                    }

                    // PINTAR LOS CHIPS DINÁMICOS
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        items(listaCategorias) { categoria ->
                            FilterChip(
                                selected = categoriaSeleccionada == categoria,
                                onClick = { categoriaSeleccionada = categoria },
                                label = { Text(categoria) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2F4F4F),
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // FILTRADO CON LAS CATEGORÍAS DINÁMICAS
                    val librosFiltrados = librosBD.filter { libro ->
                        val coincideTexto = libro.titulo.contains(textoBusqueda, ignoreCase = true) ||
                                libro.autor.contains(textoBusqueda, ignoreCase = true)

                        val categoriaLibro = libro.categoria?.trim() ?: ""
                        val coincideCategoria = categoriaSeleccionada == "Todos" ||
                                categoriaLibro.equals(categoriaSeleccionada, ignoreCase = true)

                        coincideTexto && coincideCategoria
                    }

                    if (librosFiltrados.isEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("No se encontraron libros que coincidan.", color = Color.Gray)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(librosFiltrados) { libro ->
                                ItemLibro(libro = libro, onAgregarClick = {
                                    if (libro.id.isNullOrBlank()) {
                                        mensaje = "Error: Libro sin identificador válido."
                                    } else {
                                        prestamosViewModel.solicitarNuevoPrestamo(libro.id)
                                        solicitudEnviada = true
                                        mensaje = "Procesando solicitud para '${libro.titulo}'..."
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

// El componente ItemLibro se mantiene igual abajo...
@Composable
fun ItemLibro(libro: Libro, onAgregarClick: () -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize()
            .clickable { expandido = !expandido },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Card(
                modifier = Modifier.size(width = 80.dp, height = 110.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6D9886))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (!libro.urlPortada.isNullOrEmpty()) {
                        AsyncImage(model = libro.urlPortada, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(text = "PORTADA", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = libro.titulo, style = MaterialTheme.typography.titleMedium, color = Color(0xFF3E2723))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Autor: ${libro.autor}", color = Color(0xFF5D4037))
                val disponibilidad = if (libro.disponible > 0) "Disponible" else "Agotado"
                val colorDisponibilidad = if (libro.disponible > 0) Color(0xFF2E7D32) else Color.Red
                Text(text = "Estado: $disponibilidad", color = colorDisponibilidad)

                if (expandido) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = libro.descripcion ?: "Sin descripción disponible.", color = Color(0xFF424242))
                    Spacer(modifier = Modifier.height(10.dp))
                    if (libro.disponible > 0) {
                        Button(onClick = { onAgregarClick() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F), contentColor = Color.Black)) {
                            Text("Solicitar Préstamo")
                        }
                    }
                }
            }
        }
    }
}