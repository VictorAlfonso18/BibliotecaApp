package com.example.biblioteca.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Prestamo(
    @SerialName("id_prestamo") val id: String? = null,
    val estado: String = "pendiente",
    @SerialName("id_usuario") val usuarioId: String = "",
    @SerialName("id_libro") val libroId: String? = "",
    @SerialName("fecha_solicitud") val fechaSolicitud: String? = null,
    @SerialName("fecha_prestamo") val fechaPrestamo: String? = null,
    @SerialName("fecha_devolucion") val fechaDevolucion: String? = null
)