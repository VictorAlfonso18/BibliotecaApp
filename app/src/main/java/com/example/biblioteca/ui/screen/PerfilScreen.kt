package com.example.biblioteca.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.biblioteca.ui.viewmodels.PerfilViewModel
import com.example.biblioteca.ui.viewmodels.PerfilState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onSignOutSuccess: () -> Unit
) {
    val estado by viewModel.perfilState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
    }

    val contexto = LocalContext.current

    val selectorDeFotos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = contexto.contentResolver.openInputStream(uri)
                val bytesDeLaFoto = inputStream?.readBytes()
                inputStream?.close()

                if (bytesDeLaFoto != null) {
                    viewModel.subirMiIdentificacion(bytesDeLaFoto)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mi Perfil",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF2F4F4F),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (estado) {
            is PerfilState.Loading -> {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = Color(0xFF2F4F4F))
            }
            is PerfilState.Error -> {
                val errorMsg = (estado as PerfilState.Error).message
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = errorMsg, color = MaterialTheme.colorScheme.error)

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onSignOutSuccess() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Regresar al Inicio")
                }
            }
            is PerfilState.Success -> {
                val perfil = (estado as PerfilState.Success).perfil

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        perfil.nombre?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF3E2723)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Correo: ${perfil.correo}")

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(text = "Rol: ${perfil.rol.replaceFirstChar { it.uppercase() }}")

                        Spacer(modifier = Modifier.height(4.dp))

                        if (perfil.verificado) {
                            Text(text = "Verificación: Aprobada", color = Color(0xFF2E7D32))
                        } else {
                            Text(text = "Verificación: Pendiente", color = Color.Red)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!perfil.verificado) {
                            Button(
                                onClick = {
                                    selectorDeFotos.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F))
                            ) {
                                Text("Subir Identificación")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.cerrarSesion()
                                onSignOutSuccess()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cerrar Sesión", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}