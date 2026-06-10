package com.example.biblioteca.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biblioteca.data.model.Libro
import com.example.biblioteca.ui.viewmodels.LibrosViewModel
import com.example.biblioteca.ui.viewmodels.LibrosState
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AdminLibrosScreen(viewModel: LibrosViewModel) {
    val estado by viewModel.librosState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var libroActual by remember { mutableStateOf<Libro?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarCatalogo()
    }

    val librosReales = if (estado is LibrosState.Success) {
        (estado as LibrosState.Success).libros
    } else {
        emptyList()
    }

    val librosFiltrados = librosReales.filter {
        it.titulo.contains(searchQuery, ignoreCase = true) ||
                it.autor.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    libroActual = null
                    showDialog = true
                },
                containerColor = Color(0xFF3F51B5)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)) {
            Text("Gestión de Libros", fontSize = 24.sp, color = Color(0xFF2F4F4F))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por título o autor...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (estado is LibrosState.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            LazyColumn {
                items(librosFiltrados) { libro ->
                    LibroAdminItem(
                        libro = libro,
                        onEdit = {
                            libroActual = libro
                            showDialog = true
                        },
                        onDelete = {
                            if (!libro.id.isNullOrBlank()) {
                                viewModel.eliminarLibro(libro.id)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        LibroDialog(
            viewModel = viewModel,
            libro = libroActual,
            onDismiss = { showDialog = false },
            onSave = { _, _, _, _, _, _ -> showDialog = false }
        )
    }
}

@Composable
fun LibroAdminItem(libro: Libro, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier.size(width = 80.dp, height = 110.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6D9886))
            ) {
                if (!libro.urlPortada.isNullOrEmpty()) {
                    AsyncImage(
                        model = libro.urlPortada,
                        contentDescription = "Portada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("PORTADA", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(libro.titulo, style = MaterialTheme.typography.titleMedium, color = Color(0xFF3E2723))
                Text("Autor: ${libro.autor}", style = MaterialTheme.typography.bodyMedium)
                Text("Categoría: ${libro.categoria}", style = MaterialTheme.typography.bodyMedium)

                val hayCopias = libro.disponible > 0
                Text(
                    text = "Estado: ${if (hayCopias) "${libro.disponible} copias" else "Agotado"}",
                    color = if (hayCopias) Color(0xFF388E3C) else Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Button(onClick = onEdit, modifier = Modifier.height(36.dp)) { Text("Editar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.height(36.dp)) { Text("Eliminar") }
                }
            }
        }
    }
}

@Composable
fun LibroDialog(
    viewModel: LibrosViewModel,
    libro: Libro?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Int) -> Unit
) {
    var titulo by remember { mutableStateOf(libro?.titulo ?: "") }
    var autor by remember { mutableStateOf(libro?.autor ?: "") }
    var categoria by remember { mutableStateOf(libro?.categoria ?: "") }
    var descripcion by remember { mutableStateOf(libro?.descripcion ?: "") }
    var urlPortada by remember { mutableStateOf(libro?.urlPortada ?: "") }
    var copias by remember { mutableStateOf(libro?.disponible?.toString() ?: "1") }

    var generandoIA by remember { mutableStateOf(false) }
    var errorIA by remember { mutableStateOf("") }
    var imagenBytes by remember { mutableStateOf<ByteArray?>(null) }
    var guardando by remember { mutableStateOf(false) }

    val contexto = LocalContext.current

    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = contexto.contentResolver.openInputStream(uri)
                imagenBytes = inputStream?.readBytes()
                inputStream?.close()
                // Mostramos nombre local como preview
                urlPortada = uri.lastPathSegment ?: "imagen seleccionada"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (libro == null) "Agregar Libro" else "Editar Libro") },
        text = {
            Column {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = autor, onValueChange = { autor = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de portada
                Text("Portada", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Preview de la imagen actual o seleccionada
                    Card(
                        modifier = Modifier.size(width = 60.dp, height = 80.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6D9886))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (imagenBytes != null) {
                                // Imagen recién seleccionada de galería
                                val bitmap = remember(imagenBytes) {
                                    android.graphics.BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes!!.size)
                                }
                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "Portada seleccionada",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else if (!libro?.urlPortada.isNullOrEmpty()) {
                                // Portada existente en Supabase
                                AsyncImage(
                                    model = libro?.urlPortada,
                                    contentDescription = "Portada actual",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("SIN\nFOTO", color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            selectorFoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F))
                    ) {
                        Text(if (imagenBytes != null) "Cambiar imagen" else "Seleccionar de galería", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // IA para descripción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Descripción", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (titulo.isNotBlank() && autor.isNotBlank()) {
                        TextButton(
                            onClick = {
                                generandoIA = true
                                errorIA = ""
                                viewModel.generarDescripcionConIA(
                                    titulo = titulo, autor = autor, categoria = categoria,
                                    onResult = { descripcion = it; generandoIA = false },
                                    onError = { errorIA = it; generandoIA = false }
                                )
                            },
                            enabled = !generandoIA
                        ) {
                            if (generandoIA) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generando...")
                            } else {
                                Text("✨ Autocompletar con IA")
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = descripcion, onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5
                )
                if (errorIA.isNotEmpty()) {
                    Text(text = errorIA, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = copias, onValueChange = { copias = it },
                    label = { Text("Copias Disponibles") }, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isBlank() || autor.isBlank()) return@Button
                    guardando = true
                    val cantidad = copias.toIntOrNull() ?: 0
                    viewModel.subirPortadaYGuardarLibro(
                        idLibro = libro?.id,
                        datosLibro = Triple(titulo, autor, categoria),
                        descripcion = descripcion,
                        urlPortadaActual = libro?.urlPortada ?: "",
                        copias = cantidad,
                        imagenBytes = imagenBytes,
                        onDone = { guardando = false; onDismiss() }
                    )
                },
                enabled = !guardando
            ) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}