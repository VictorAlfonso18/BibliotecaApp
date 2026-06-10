package com.example.biblioteca.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.biblioteca.data.model.Libro
import com.example.biblioteca.data.model.Prestamo
import com.example.biblioteca.ui.viewmodels.PrestamosViewModel
import com.example.biblioteca.ui.viewmodels.PrestamosState
import com.example.biblioteca.ui.viewmodels.LibrosViewModel
import com.example.biblioteca.ui.viewmodels.LibrosState
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PrestamosScreen(
    viewModel: PrestamosViewModel,
    librosViewModel: LibrosViewModel
) {
    val estado by viewModel.prestamosState.collectAsState()
    val estadoLibros by librosViewModel.librosState.collectAsState()

    var mensaje by remember { mutableStateOf("") }
    var qrSeleccionado by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarMisPrestamos()
        if (estadoLibros !is LibrosState.Success) {
            librosViewModel.cargarCatalogo()
        }
    }

    val catalogo = if (estadoLibros is LibrosState.Success) {
        (estadoLibros as LibrosState.Success).libros
    } else {
        emptyList()
    }

    qrSeleccionado?.let { idPrestamo ->
        Dialog(onDismissRequest = { qrSeleccionado = null }) {
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
                        text = "Código de Autorización",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF2F4F4F)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val bitmap = remember(idPrestamo) { generarQR(idPrestamo) }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Código QR del préstamo",
                            modifier = Modifier.size(250.dp)
                        )
                    } else {
                        Text("Error al generar el código QR", color = Color.Red)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Muestra este código al administrador en la biblioteca para recibir o devolver tu libro.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { qrSeleccionado = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F))
                    ) {
                        Text("Cerrar")
                    }
                }
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
                            ItemPrestamo(prestamo = prestamo, catalogo = catalogo) { idPrestamo ->
                                qrSeleccionado = idPrestamo
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemPrestamo(prestamo: Prestamo, catalogo: List<Libro>, onVerQRClick: (String) -> Unit) {
    val libroAsociado = catalogo.find { it.id == prestamo.libroId }
    val tituloLibro = libroAsociado?.titulo ?: "Libro Desconocido"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Folio #${if (!prestamo.id.isNullOrBlank()) prestamo.id.take(8) else "Desconocido"}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tituloLibro,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF3E2723),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            prestamo.fechaSolicitud?.let {
                Text("Solicitado el: ${formatearFecha(it)}")
            }
            prestamo.fechaPrestamo?.let {
                Text("Entregado el: ${formatearFecha(it)}")
            }
            prestamo.fechaDevolucion?.let {
                Text("Devuelto el: ${formatearFecha(it)}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            val colorEstado = when (prestamo.estado.lowercase()) {
                "activo" -> Color(0xFF2E7D32)
                "pendiente" -> Color(0xFFE65100)
                "devuelto" -> Color.Gray
                else -> Color.Black
            }

            Text(
                text = "Estado: ${prestamo.estado.uppercase()}",
                color = colorEstado,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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

fun generarQR(contenido: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun formatearFecha(fechaIso: String): String {
    return try {
        val fechaLimpia = fechaIso.substringBefore(".")
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        val parsedDate = parser.parse(fechaLimpia)
        if (parsedDate != null) formatter.format(parsedDate) else fechaIso
    } catch (e: Exception) {
        fechaIso
    }
}