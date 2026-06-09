package com.example.biblioteca.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class Prestamo(
    val idPrestamo: Int,
    val idUsuario: Int,
    val idLibro: Int,
    val estado: String,
    val fechaSolicitud: String,
    val fechaPrestamo: String,
    val fechaDevolucion: String
)

@Composable
fun PrestamosScreen() {

    var mensaje by remember {
        mutableStateOf("")
    }

    val prestamos = listOf(
        Prestamo(
            1,
            1,
            1,
            "Activo",
            "01/06/2026",
            "02/06/2026",
            "16/06/2026"
        ),
        Prestamo(
            2,
            1,
            3,
            "Devuelto",
            "15/05/2026",
            "16/05/2026",
            "30/05/2026"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Mis Préstamos",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF2F4F4F)
        )

        if (mensaje.isNotEmpty()) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mensaje,
                color = Color(0xFF2E7D32)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {

            items(prestamos) { prestamo ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5EFE6)
                    ),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Préstamo #${prestamo.idPrestamo}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("ID Usuario: ${prestamo.idUsuario}")
                        Text("ID Libro: ${prestamo.idLibro}")
                        Text("Fecha solicitud: ${prestamo.fechaSolicitud}")
                        Text("Fecha préstamo: ${prestamo.fechaPrestamo}")
                        Text("Fecha devolución: ${prestamo.fechaDevolucion}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Estado: ${prestamo.estado}",
                            color =
                                if (prestamo.estado == "Activo")
                                    Color(0xFF2E7D32)
                                else
                                    Color.Gray
                        )

                        if (prestamo.estado == "Activo") {

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    mensaje =
                                        "Préstamo #${prestamo.idPrestamo} devuelto"
                                }
                            ) {
                                Text("Devolver")
                            }
                        }
                    }
                }
            }
        }
    }
}