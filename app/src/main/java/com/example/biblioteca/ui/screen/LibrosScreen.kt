package com.example.biblioteca.ui.screen

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

data class Libro(
    val titulo: String,
    val autor: String,
    val descripcion: String
)

@Composable
fun LibrosScreen() {

    var mensaje by remember {
        mutableStateOf("")
    }

    val libros = listOf(
        Libro(
            "Don Quijote",
            "Miguel de Cervantes",
            "Considerada una de las obras más importantes de la literatura española."
        ),
        Libro(
            "El Principito",
            "Antoine de Saint-Exupéry",
            "Historia sobre amistad, amor y la importancia de ver más allá de lo visible."
        ),
        Libro(
            "Harry Potter",
            "J.K. Rowling",
            "Saga de fantasía sobre un joven mago y sus aventuras en Hogwarts."
        ),
        Libro(
            "Cien años de soledad",
            "Gabriel García Márquez",
            "Novela emblemática del realismo mágico latinoamericano."
        ),
        Libro(
            "El Señor de los Anillos",
            "J.R.R. Tolkien",
            "Aventura épica ambientada en la Tierra Media."
        )
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

        if (mensaje.isNotEmpty()) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mensaje,
                color = Color(0xFF2E7D32)
            )
        }

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

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = libro.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF3E2723)
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Autor: ${libro.autor}",
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
                                    text = libro.descripcion,
                                    color = Color(0xFF424242)
                                )

                                Spacer(
                                    modifier = Modifier.height(10.dp)
                                )

                                Button(
                                    onClick = {
                                        mensaje = "${libro.titulo} agregado correctamente"
                                    }
                                ) {
                                    Text("Agregar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}