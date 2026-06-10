package com.example.biblioteca.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Libro(
    @SerialName("id_libro") val id: String = "",
    val titulo: String = "",
    val autor: String = "",
    val categoria: String = "General",
    val descripcion: String? = null,
    val disponible: Int = 1,
    @SerialName("url_portada") val urlPortada: String? = null,
    @SerialName("creado_en") val createdAt: String? = null
)