package com.example.biblioteca.ui.screen

import androidx.compose.foundation.Image // Importante para el logo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // Importante para jalar el drawable xml
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.biblioteca.ui.viewmodels.AuthViewModel
import com.example.biblioteca.ui.viewmodels.AuthState
import com.example.biblioteca.R
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegistro: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryEmail by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val rol = (authState as AuthState.Success).rol
            onLoginSuccess(rol)
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. TU LOGO EN VECTOR ARRIBA DEL INICIO DE SESIÓN
        Image(painter = painterResource(id = R.drawable.logoapp), contentDescription = "Logo", modifier = Modifier.size(130.dp).padding(bottom = 12.dp))

        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF2F4F4F)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5EFE6)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ingresa tus credenciales",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF3E2723)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                when (authState) {
                    is AuthState.Loading -> {
                        CircularProgressIndicator(color = Color(0xFF2F4F4F))
                    }
                    is AuthState.Error -> {
                        val error = (authState as AuthState.Error).message
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.iniciarSesion(correo, password) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                    else -> {
                        Button(
                            onClick = { viewModel.iniciarSesion(correo, password) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2F4F4F),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Ingresar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { showRecoveryDialog = true }) {
                    Text("¿Olvidaste tu contraseña?", color = Color.Gray)
                }

                Text(
                    text = "¿No tienes cuenta? Regístrate aquí",
                    color = Color(0xFF2F4F4F),
                    modifier = Modifier
                        .clickable { onNavigateToRegistro() }
                        .padding(8.dp)
                )
            }
            if (showRecoveryDialog) {
                AlertDialog(
                    onDismissRequest = { showRecoveryDialog = false },
                    title = { Text("Recuperar contraseña") },
                    text = {
                        Column {
                            Text("Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.", color = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = recoveryEmail,
                                onValueChange = { recoveryEmail = it },
                                label = { Text("Correo Electrónico") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (authState is AuthState.CorreoEnviado) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("¡Enlace enviado! Revisa tu bandeja de entrada.", color = Color(0xFF388E3C))
                            } else if (authState is AuthState.Error) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text((authState as AuthState.Error).message, color = Color.Red)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.recuperarPassword(recoveryEmail) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F)),
                            enabled = authState !is AuthState.Loading
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Text("Enviar enlace")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showRecoveryDialog = false
                            viewModel.resetState()
                        }) {
                            Text("Cancelar", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}