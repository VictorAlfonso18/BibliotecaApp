package com.example.biblioteca.ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.biblioteca.data.model.Usuario
import com.example.biblioteca.ui.viewmodels.UsuariosViewModel
import com.example.biblioteca.ui.viewmodels.UsuariosState

@Composable
fun AdminUsuariosScreen(
    viewModel: UsuariosViewModel
) {
    val estado by viewModel.usuariosState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    var usuarioViendoId by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarUsuarios()
    }

    val usuariosReales = if (estado is UsuariosState.Success) {
        (estado as UsuariosState.Success).usuarios
    } else {
        emptyList()
    }

    val usuariosFiltrados = usuariosReales.filter { usuario ->
        (usuario.nombre ?: "").contains(searchQuery, ignoreCase = true) ||
                (usuario.correo ?: "").contains(searchQuery, ignoreCase = true)
    }

    usuarioViendoId?.let { usuarioSeleccionado ->
        Dialog(onDismissRequest = { usuarioViendoId = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Identificación",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF2F4F4F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = usuarioSeleccionado.nombre ?: "Usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!usuarioSeleccionado.urlIdentificacion.isNullOrEmpty()) {
                            AsyncImage(
                                model = usuarioSeleccionado.urlIdentificacion,
                                contentDescription = "Identificación del usuario",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = "El usuario no ha subido\nuna identificación",
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { usuarioViendoId = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Control de Usuarios", fontSize = 24.sp, color = Color(0xFF2F4F4F))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por nombre o correo...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (estado) {
            is UsuariosState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2F4F4F))
                }
            }
            is UsuariosState.Error -> {
                val errorMsg = (estado as UsuariosState.Error).message
                Text(text = "Error: $errorMsg", color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.cargarUsuarios() }) {
                    Text("Reintentar")
                }
            }
            is UsuariosState.Success -> {
                LazyColumn {
                    items(usuariosFiltrados) { usuario ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6)),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = usuario.nombre ?: "Usuario sin nombre",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF3E2723)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Correo: ${usuario.correo}")
                                Text("Rol: ${usuario.rol.uppercase()}")

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Estado: ${if (usuario.verificado) "Verificado" else "Pendiente"}",
                                    color = if (usuario.verificado) Color(0xFF388E3C) else Color.Red,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (!usuario.verificado) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = {
                                            usuarioViendoId = usuario
                                        }) {
                                            Text("Ver Identificación")
                                        }

                                        Button(onClick = {
                                            if (usuario.id.isNotBlank()) {
                                                viewModel.aprobarUsuario(usuario.id)
                                            }
                                        }) {
                                            Text("Aprobar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}