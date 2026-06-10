package com.example.biblioteca.ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
                            libro.id?.let { idLibro ->
                                viewModel.eliminarLibro(idLibro)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        LibroDialog(
            libro = libroActual,
            onDismiss = { showDialog = false },
            onSave = { titulo, autor, categoria, descripcion, urlPortada, copias ->
                if (libroActual == null) {
                    val nuevoLibro = Libro(
                        titulo = titulo,
                        autor = autor,
                        categoria = categoria,
                        descripcion = descripcion,
                        urlPortada = urlPortada,
                        disponible = copias
                    )
                    viewModel.insertarLibro(nuevoLibro)
                } else {
                    libroActual?.id?.let { idLibro ->
                        viewModel.actualizarLibro(
                            idLibro = idLibro,
                            titulo = titulo,
                            autor = autor,
                            categoria = categoria,
                            descripcion = descripcion,
                            urlPortada = urlPortada,
                            disponible = copias
                        )
                    }
                }
                showDialog = false
            }
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
fun LibroDialog(libro: Libro?, onDismiss: () -> Unit, onSave: (String, String, String, String, String, Int) -> Unit) {
    var titulo by remember { mutableStateOf(libro?.titulo ?: "") }
    var autor by remember { mutableStateOf(libro?.autor ?: "") }
    var categoria by remember { mutableStateOf(libro?.categoria ?: "") } // NUEVO
    var descripcion by remember { mutableStateOf(libro?.descripcion ?: "") }
    var urlPortada by remember { mutableStateOf(libro?.urlPortada ?: "") } // NUEVO
    var copias by remember { mutableStateOf(libro?.disponible?.toString() ?: "1") }

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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = urlPortada, onValueChange = { urlPortada = it }, label = { Text("URL de la Portada") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = copias,
                    onValueChange = { copias = it },
                    label = { Text("Copias Disponibles") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cantidad = copias.toIntOrNull() ?: 0
                onSave(titulo, autor, categoria, descripcion, urlPortada, cantidad)
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}