package com.example.biblioteca.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    @SerialName("id_usuario") val id: String = "",
    val correo: String? = null,
    val rol: String = "cliente",
    @SerialName("url_identificacion") val urlIdentificacion: String? = null,
    val verificado: Boolean = false,
    val nombre: String? = null,
    @SerialName("creado_en") val createdAt: String? = null
)