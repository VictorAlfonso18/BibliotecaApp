package com.example.biblioteca.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.biblioteca.ui.viewmodels.AuthViewModel
import com.example.biblioteca.ui.viewmodels.AuthState

@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onRegistroSuccess: () -> Unit
) {

    val authState by viewModel.authState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    var errorLocal by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegistroSuccess()
            viewModel.resetState()
        } else if (authState is AuthState.Error) {
            errorLocal = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF2F4F4F),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Formulario de Registro",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF3E2723)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre Completo") },
                    placeholder = { Text("Juan Pérez") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo Electrónico") },
                    placeholder = { Text("juan@correo.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = { confirmarPassword = it },
                    label = { Text("Confirmar Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (errorLocal != null) {
                    Text(text = errorLocal!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (authState) {
                    is AuthState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF2F4F4F))
                        }
                    }
                    is AuthState.Error -> {
                        val errorMsg = (authState as AuthState.Error).message
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        BotonRegistrar(nombre, correo, password, confirmarPassword, viewModel) { error ->
                            errorLocal = error
                        }
                    }
                    else -> {
                        BotonRegistrar(nombre, correo, password, confirmarPassword, viewModel) { error ->
                            errorLocal = error
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotonRegistrar(
    nombre: String,
    correo: String,
    pass: String,
    confirmaPass: String,
    viewModel: AuthViewModel,
    onError: (String?) -> Unit
) {
    Button(
        onClick = {
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

            when {
                nombre.isBlank() || correo.isBlank() || pass.isBlank() -> {
                    onError("¡Ups! Parece que olvidaste llenar algunos campos.")
                }
                !correo.matches(emailRegex) -> {
                    onError("Por favor, ingresa un correo electrónico válido.")
                }
                pass.length < 6 -> {
                    onError("Por tu seguridad, la contraseña debe tener al menos 6 caracteres.")
                }
                pass != confirmaPass -> {
                    onError("Las contraseñas no coinciden. ¡Revisalas de nuevo!")
                }
                else -> {
                    onError(null)
                    viewModel.registrarse(nombre, correo, pass)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F))
    ) {
        Text("Registrarme")
    }
}