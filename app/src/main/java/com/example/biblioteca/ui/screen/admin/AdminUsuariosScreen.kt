package com.example.biblioteca.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class UsuarioAdmin(val id: Int, val nombre: String, val correo: String, val estado: String)

@Composable
fun AdminUsuariosScreen() {
    val usuarios = listOf(
        UsuarioAdmin(1, "Juan Pérez", "juan@correo.com", "Activo"),
        UsuarioAdmin(2, "Ana López", "ana@correo.com", "Suspendido")
    )

    var searchQuery by remember { mutableStateOf("") }

    // Filtrar la lista en tiempo real por nombre o correo (Punto 3)
    val usuariosFiltrados = usuarios.filter {
        it.nombre.contains(searchQuery, ignoreCase = true) || it.correo.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Control de Usuarios", fontSize = 24.sp, color = Color(0xFF2F4F4F))
        Spacer(modifier = Modifier.height(16.dp))

        // Campo de búsqueda de usuarios
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por nombre o correo...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(usuariosFiltrados) { usuario ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("Correo: ${usuario.correo}")
                        Text("Estado: ${usuario.estado}", color = if (usuario.estado == "Activo") Color(0xFF388E3C) else Color.Red)
                    }
                }
            }
        }
    }
}