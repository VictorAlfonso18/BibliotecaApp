package com.example.biblioteca.ui.screen.admin

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PrestamoAdmin(val id: Int, val usuario: String, val libro: String, val fecha: String, val estado: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPrestamosScreen() {
    val context = LocalContext.current
    val prestamos = listOf(
        PrestamoAdmin(1, "Juan Pérez", "Don Quijote", "01/06/2026", "Activo"),
        PrestamoAdmin(2, "María Gómez", "El Principito", "15/05/2026", "Atrasado"),
        PrestamoAdmin(3, "Lucas Díaz", "Harry Potter", "20/05/2026", "Activo")
    )

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }

    // Filtrado combinado
    val prestamosFiltrados = prestamos.filter { prestamo ->
        val coincideBusqueda = prestamo.usuario.contains(searchQuery, ignoreCase = true) ||
                prestamo.libro.contains(searchQuery, ignoreCase = true)
        val coincideFiltro = selectedFilter == "Todos" || prestamo.estado == selectedFilter
        coincideBusqueda && coincideFiltro
    }

    // Herramienta del sistema para crear y guardar un archivo real en el almacenamiento
    val guardarArchivoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    // Estructuramos las columnas del reporte separadas por comas
                    val csvHeader = "ID Prestamo,Usuario,Libro,Fecha,Estado\n"
                    val csvRows = prestamosFiltrados.joinToString("\n") { prestamo ->
                        "${prestamo.id},${prestamo.usuario},${prestamo.libro},${prestamo.fecha},${prestamo.estado}"
                    }

                    // Escribimos los bytes en el archivo físico del dispositivo
                    outputStream.write((csvHeader + csvRows).toByteArray())
                }
                Toast.makeText(context, "¡Reporte guardado con éxito en tu dispositivo!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al generar el archivo de Excel", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Control de Préstamos", fontSize = 24.sp, color = Color(0xFF2F4F4F))
        Spacer(modifier = Modifier.height(16.dp))

        // Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por usuario o libro...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filtros
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todos", "Activo", "Atrasado").forEach { estado ->
                FilterChip(
                    selected = selectedFilter == estado,
                    onClick = { selectedFilter = estado },
                    label = { Text(estado) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de descarga real
        Button(
            onClick = {
                // Lanza el buscador de carpetas del celular sugiriendo este nombre
                guardarArchivoLauncher.launch("Reporte_Prestamos.csv")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
        ) {
            Text("Guardar Reporte en Excel")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(prestamosFiltrados) { prestamo ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Préstamo #${prestamo.id}", style = MaterialTheme.typography.titleMedium)
                        Text("Usuario: ${prestamo.usuario}")
                        Text("Libro: ${prestamo.libro}")
                        Text("Fecha solicitud: ${prestamo.fecha}")
                        Text("Estado: ${prestamo.estado}", color = if (prestamo.estado == "Activo") Color(0xFF388E3C) else Color.Red)
                    }
                }
            }
        }
    }
}