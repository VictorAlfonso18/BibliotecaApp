package com.example.biblioteca.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LibrosScreen() {

    val libros = listOf(
        "Don Quijote",
        "El Principito",
        "Harry Potter",
        "Cien años de soledad",
        "El Señor de los Anillos"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Biblioteca Digital",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF2F4F4F)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {

            items(libros) { libro ->

                var expandido by remember {
                    mutableStateOf(false)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .animateContentSize()
                        .clickable {
                            expandido = !expandido
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5EFE6)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Card(
                            modifier = Modifier
                                .size(
                                    width = 80.dp,
                                    height = 110.dp
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF6D9886)
                            )
                        ) {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = "PORTADA",
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Column {

                            Text(
                                text = libro,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF3E2723)
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Autor: Pendiente",
                                color = Color(0xFF5D4037)
                            )

                            Text(
                                text = "Estado: Disponible",
                                color = Color(0xFF2E7D32)
                            )

                            if (expandido) {

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = "Descripción del libro. Aquí se mostrará información adicional cuando la aplicación esté conectada a la base de datos remota.",
                                    color = Color(0xFF424242)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}