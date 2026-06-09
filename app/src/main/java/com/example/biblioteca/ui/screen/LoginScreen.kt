package com.example.biblioteca.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginUser: () -> Unit,
    onLoginAdmin: () -> Unit,
    onNavigateToRegistro: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título con tu color Verde Oscuro
        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF2F4F4F)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta con tu estilo Crema/Arena y elevación de 6.dp
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Ingresa tus credenciales",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF3E2723)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de Correo
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo Electrónico") },
                    placeholder = { Text("juan@correo.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón para ingresar como Usuario
                Button(
                    onClick = { onLoginUser() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ingresar como Usuario")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para ingresar como Administrador
                Button(
                    onClick = { onLoginAdmin() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ingresar como Administrador")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Texto para saltar al Registro
                Text(
                    text = "¿No tienes cuenta? Regístrate aquí",
                    color = Color(0xFF2F4F4F),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { onNavigateToRegistro() }
                )
            }
        }
    }
}