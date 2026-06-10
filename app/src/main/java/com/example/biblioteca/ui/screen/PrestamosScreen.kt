package com.example.biblioteca.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.biblioteca.data.model.Prestamo
import com.example.biblioteca.ui.viewmodels.PrestamosViewModel
import com.example.biblioteca.ui.viewmodels.PrestamosState

@Composable
fun PrestamosScreen(
    viewModel: PrestamosViewModel
) {
    val estado by viewModel.prestamosState.collectAsState()
    var mensaje by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarMisPrestamos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mis Préstamos",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF2F4F4F),
            modifier = Modifier.align(Alignment.Start)
        )

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje, color = Color(0xFF2E7D32))
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (estado) {
            is PrestamosState.Loading -> {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = Color(0xFF2F4F4F))
            }
            is PrestamosState.Error -> {
                val errorMsg = (estado as PrestamosState.Error).message
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Error: $errorMsg", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.cargarMisPrestamos() }) {
                    Text("Reintentar")
                }
            }
            is PrestamosState.Success -> {
                val misPrestamos = (estado as PrestamosState.Success).prestamos

                if (misPrestamos.isEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Aún no tienes préstamos en tu historial.", color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(misPrestamos) { prestamo ->
                            ItemPrestamo(prestamo = prestamo) { idPrestamo ->
                                // TODO: Aquí más adelante abrirás un Dialog o navegarás a la pantalla del QR real
                                mensaje = "Mostrar QR del préstamo:\n$idPrestamo"
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemPrestamo(prestamo: Prestamo, onVerQRClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Préstamo #${prestamo.id?.take(8) ?: "Desconocido"}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF3E2723)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("ID Libro: ${prestamo.libroId}")

            prestamo.fechaSolicitud?.let { Text("Solicitado el: $it") }

            Spacer(modifier = Modifier.height(8.dp))

            val colorEstado = when (prestamo.estado.lowercase()) {
                "activo" -> Color(0xFF2E7D32) // Verde
                "pendiente" -> Color(0xFFE65100) // Naranja
                "devuelto" -> Color.Gray
                else -> Color.Black
            }

            Text(
                text = "Estado: ${prestamo.estado.uppercase()}",
                color = colorEstado,
                style = MaterialTheme.typography.bodyLarge
            )

            if (prestamo.estado.lowercase() in listOf("activo", "pendiente")) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { prestamo.id?.let { onVerQRClick(it) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F))
                ) {
                    Text("Ver Código QR")
                }
            }
        }
    }
}