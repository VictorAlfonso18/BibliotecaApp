package com.example.biblioteca.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PrestamosScreen() {

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

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5EFE6)
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Don Quijote",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text("Fecha préstamo: 01/06/2026")
                Text("Fecha devolución: 15/06/2026")
                Text(
                    text = "Estado: Activo",
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}