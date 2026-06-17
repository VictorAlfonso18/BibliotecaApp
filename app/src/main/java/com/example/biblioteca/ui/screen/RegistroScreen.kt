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

    // Variables de los datos
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    // Variables para mostrar el error EN TIEMPO REAL de cada campo
    var nombreError by remember { mutableStateOf<String?>(null) }
    var correoError by remember { mutableStateOf<String?>(null) }
    var passError by remember { mutableStateOf<String?>(null) }
    var confirmaError by remember { mutableStateOf<String?>(null) }

    // Error general para el botón
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegistroSuccess()
            viewModel.resetState()
        } else if (authState is AuthState.Error) {
            errorGeneral = null
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

                // 1. CAMPO NOMBRE COMPLETO
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { newValue ->
                        nombre = newValue
                        // Validación en tiempo real: Solo letras y espacios
                        val regexNombre = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$".toRegex()
                        nombreError = if (!newValue.matches(regexNombre)) {
                            "Solo se aceptan letras y espacios."
                        } else {
                            null
                        }
                    },
                    label = { Text("Nombre Completo") },
                    placeholder = { Text("Juan Pérez") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = nombreError != null,
                    supportingText = { if (nombreError != null) Text(nombreError!!) }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 2. CAMPO CORREO ELECTRÓNICO
                OutlinedTextField(
                    value = correo,
                    onValueChange = { newValue ->
                        correo = newValue

                        // Extraemos lo que el usuario escribe después del '@'
                        val dominioEscrito = newValue.substringAfter("@", missingDelimiterValue = "")

                        correoError = when {
                            // 1. Bloquea espacios y símbolos raros al instante
                            !newValue.matches("^[a-zA-Z0-9@.]*$".toRegex()) -> {
                                "No se aceptan símbolos raros ni espacios."
                            }
                            // 2. Si ya puso el '@', verificamos letra por letra que esté escribiendo 'gmail.com'
                            newValue.contains("@") && dominioEscrito.isNotEmpty() && !"gmail.com".startsWith(dominioEscrito) -> {
                                "El dominio debe ser exactamente @gmail.com"
                            }
                            // 3. Si se pasa de largo escribiendo (ej. @gmail.coma)
                            newValue.contains("@") && dominioEscrito.length > 9 -> {
                                "El dominio debe ser exactamente @gmail.com"
                            }
                            else -> null
                        }
                    },
                    label = { Text("Correo Electrónico") },
                    placeholder = { Text("juan@gmail.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = correoError != null,
                    supportingText = { if (correoError != null) Text(correoError!!) }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 3. CAMPO CONTRASEÑA
                OutlinedTextField(
                    value = password,
                    onValueChange = { newValue ->
                        // Bloqueamos que no escriba más de 12 caracteres
                        if (newValue.length <= 12) {
                            password = newValue
                        }

                        passError = if (newValue.isNotEmpty() && newValue.length < 6) {
                            "Mínimo 6 caracteres."
                        } else if (newValue.length >= 12) {
                            "Alcanzaste el máximo de 12 caracteres."
                        } else {
                            null
                        }

                        // Si ya había escrito la confirmación y altera la original, revisamos de nuevo
                        if (confirmarPassword.isNotEmpty() && confirmarPassword != newValue) {
                            confirmaError = "Las contraseñas ya no coinciden."
                        } else if (confirmarPassword == newValue) {
                            confirmaError = null
                        }
                    },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = passError != null,
                    supportingText = { if (passError != null) Text(passError!!) }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 4. CAMPO CONFIRMAR CONTRASEÑA
                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = { newValue ->
                        if (newValue.length <= 12) {
                            confirmarPassword = newValue
                        }

                        confirmaError = if (newValue.isNotEmpty() && newValue != password) {
                            "Las contraseñas no coinciden."
                        } else {
                            null
                        }
                    },
                    label = { Text("Confirmar Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmaError != null,
                    supportingText = { if (confirmaError != null) Text(confirmaError!!) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (errorGeneral != null) {
                    Text(text = errorGeneral!!, color = MaterialTheme.colorScheme.error)
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
                        BotonRegistrar(nombre, correo, password, confirmarPassword, nombreError, correoError, passError, confirmaError, viewModel) { error ->
                            errorGeneral = error
                        }
                    }
                    else -> {
                        BotonRegistrar(nombre, correo, password, confirmarPassword, nombreError, correoError, passError, confirmaError, viewModel) { error ->
                            errorGeneral = error
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
    nombreError: String?,
    correoError: String?,
    passError: String?,
    confirmaError: String?,
    viewModel: AuthViewModel,
    onError: (String?) -> Unit
) {
    Button(
        onClick = {
            // El regex estricto final para asegurar la estructura del correo al momento de hacer click
            val regexCorreoFinal = "^[a-zA-Z0-9]+@gmail\\.com$".toRegex()

            when {
                nombre.isBlank() || correo.isBlank() || pass.isBlank() || confirmaPass.isBlank() -> {
                    onError("¡Ups! Parece que olvidaste llenar algunos campos.")
                }
                nombreError != null || correoError != null || passError != null || confirmaError != null -> {
                    onError("Por favor, corrige los errores en rojo antes de continuar.")
                }
                !correo.matches(regexCorreoFinal) -> {
                    onError("Asegúrate de que el correo termine exactamente en @gmail.com")
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