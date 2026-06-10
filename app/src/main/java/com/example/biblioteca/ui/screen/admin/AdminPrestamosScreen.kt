package com.example.biblioteca.ui.screen.admin

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biblioteca.ui.viewmodels.PrestamosState
import com.example.biblioteca.ui.viewmodels.PrestamosViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPrestamosScreen(viewModel: PrestamosViewModel) {
    val context = LocalContext.current

    val estado by viewModel.prestamosState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("todos") }

    LaunchedEffect(Unit) {
        viewModel.cargarTodosLosPrestamos()
    }

    val escanerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.procesarCodigoQR(result.contents)
            Toast.makeText(context, "Procesando préstamo...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    val prestamosReales = if (estado is PrestamosState.Success) {
        (estado as PrestamosState.Success).prestamos
    } else {
        emptyList()
    }

    val prestamosFiltrados = prestamosReales.filter { prestamo ->
        val coincideBusqueda = prestamo.usuarioId.contains(searchQuery, ignoreCase = true) ||
                prestamo.libroId.contains(searchQuery, ignoreCase = true)
        val coincideFiltro = selectedFilter == "todos" || prestamo.estado.equals(selectedFilter, ignoreCase = true)
        coincideBusqueda && coincideFiltro
    }

    val guardarArchivoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val csvHeader = "ID Prestamo,ID Usuario,ID Libro,Fecha,Estado\n"
                    val csvRows = prestamosFiltrados.joinToString("\n") { p ->
                        "${p.id},${p.usuarioId},${p.libroId},${p.fechaSolicitud},${p.estado}"
                    }
                    outputStream.write((csvHeader + csvRows).toByteArray())
                }
                Toast.makeText(context, "Reporte guardado con éxito", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al generar el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Control de Préstamos", fontSize = 24.sp, color = Color(0xFF2F4F4F))
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val opciones = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt("Enfoca el código QR del alumno")
                    setBeepEnabled(true)
                    setOrientationLocked(false)
                }
                escanerLauncher.launch(opciones)
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F))
        ) {
            Text("ESCANEAR CÓDIGO QR", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por ID de usuario o libro...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todos", "pendiente", "activo").forEach { estadoFiltro ->
                FilterChip(
                    selected = selectedFilter == estadoFiltro.lowercase(),
                    onClick = { selectedFilter = estadoFiltro.lowercase() },
                    label = { Text(estadoFiltro.uppercase()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { guardarArchivoLauncher.launch("Reporte_Prestamos.csv") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
        ) {
            Text("Guardar Reporte en Excel")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (estado is PrestamosState.Loading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (estado is PrestamosState.Error) {
            val errorMsg = (estado as PrestamosState.Error).message
            Text(text = "Error: $errorMsg", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        LazyColumn {
            items(prestamosFiltrados) { prestamo ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Préstamo #${prestamo.id?.take(8)}", style = MaterialTheme.typography.titleMedium)
                        Text("Usuario ID: ${prestamo.usuarioId}")
                        Text("Libro ID: ${prestamo.libroId}")
                        Text("Estado: ${prestamo.estado.uppercase()}",
                            color = when(prestamo.estado.lowercase()) {
                                "activo" -> Color(0xFF388E3C)
                                "pendiente" -> Color(0xFFE65100)
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }
        }
    }
}