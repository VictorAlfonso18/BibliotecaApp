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

// Agregamos el parámetro "portada" al modelo de datos
data class LibroAdmin(val id: Int, var titulo: String, var autor: String, var disponible: Boolean, var portada: String)

@Composable
fun AdminLibrosScreen() {
    var libros by remember { mutableStateOf(listOf(
        LibroAdmin(1, "Don Quijote", "Miguel de Cervantes", true, "Don Quijote"),
        LibroAdmin(2, "El Principito", "Antoine de Saint-Exupéry", false, "Principito")
    )) }

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var libroActual by remember { mutableStateOf<LibroAdmin?>(null) }

    // Filtro en tiempo real por título o autor (Punto 3)
    val librosFiltrados = libros.filter {
        it.titulo.contains(searchQuery, ignoreCase = true) || it.autor.contains(searchQuery, ignoreCase = true)
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

            // Barra de búsqueda (Punto 3)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por título o autor...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(librosFiltrados) { libro ->
                    LibroAdminItem(
                        libro = libro,
                        onEdit = {
                            libroActual = libro
                            showDialog = true
                        },
                        onDelete = {
                            libros = libros.filter { it.id != libro.id }
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
            onSave = { titulo, autor, disponible, portada ->
                if (libroActual == null) {
                    val nuevoId = (libros.maxOfOrNull { it.id } ?: 0) + 1
                    libros = libros + LibroAdmin(nuevoId, titulo, autor, disponible, portada)
                } else {
                    libros = libros.map {
                        if (it.id == libroActual!!.id) it.copy(titulo = titulo, autor = autor, disponible = disponible, portada = portada) else it
                    }
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun LibroAdminItem(libro: LibroAdmin, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(80.dp).background(Color(0xFF759F84), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Muestra el nombre personalizado de la portada editada
                Text(libro.portada, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(libro.titulo, style = MaterialTheme.typography.titleMedium, color = Color(0xFF3E2723))
                Text("Autor: ${libro.autor}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Estado: ${if (libro.disponible) "Disponible" else "No disponible"}",
                    color = if (libro.disponible) Color(0xFF388E3C) else Color.Red,
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
fun LibroDialog(libro: LibroAdmin?, onDismiss: () -> Unit, onSave: (String, String, Boolean, String) -> Unit) {
    var titulo by remember { mutableStateOf(libro?.titulo ?: "") }
    var autor by remember { mutableStateOf(libro?.autor ?: "") }
    var disponible by remember { mutableStateOf(libro?.disponible ?: true) }
    var portada by remember { mutableStateOf(libro?.portada ?: "") } // Input de Portada

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (libro == null) "Agregar Libro" else "Editar Libro") },
        text = {
            Column {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = autor, onValueChange = { autor = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = portada, onValueChange = { portada = it }, label = { Text("Texto/Ruta de Portada") }, placeholder = { Text("Ej. Don Quijote Portada") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = disponible, onCheckedChange = { disponible = it })
                    Text(if (disponible) "Disponible" else "No disponible")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(titulo, autor, disponible, portada) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}