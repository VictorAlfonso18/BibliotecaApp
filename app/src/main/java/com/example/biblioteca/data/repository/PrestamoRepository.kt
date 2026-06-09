package com.example.biblioteca.data.repository

import android.util.Log
import com.example.biblioteca.core.SupabaseClientHelper
import com.example.biblioteca.data.model.Prestamo
import io.github.jan.supabase.postgrest.from
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PrestamoRepository {
    private val db = SupabaseClientHelper.client.from("prestamos")

    // Helper para obtener la fecha actual
    private fun obtenerFechaActualUTC(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    // Solicitar prestamo
    suspend fun solicitarPrestamo(nuevoPrestamo: Prestamo): Boolean {
        return try {
            db.insert(nuevoPrestamo)
            true
        } catch (e: Exception) {
            Log.e("PrestamoRepo", "Error al solicitar préstamo: ${e.message}")
            false
        }
    }

    // Obtener prestamos de usuario
    suspend fun obtenerMisPrestamos(idUsuario: String): List<Prestamo> {
        return try {
            db.select {
                filter {
                    eq("id_usuario", idUsuario)
                }
            }.decodeList<Prestamo>()
        } catch (e: Exception) {
            Log.e("PrestamoRepo", "Error al obtener mis préstamos: ${e.message}")
            emptyList()
        }
    }

    // Obtener prestamos pendientes
    suspend fun obtenerPrestamosPendientes(): List<Prestamo> {
        return try {
            db.select {
                filter {
                    eq("estado", "pendiente")
                }
            }.decodeList<Prestamo>()
        } catch (e: Exception) {
            Log.e("PrestamoRepo", "Error al obtener pendientes: ${e.message}")
            emptyList()
        }
    }

    // Actualizar estado del prestamo
    suspend fun actualizarEstadoPrestamo(idPrestamo: String, nuevoEstado: String): Boolean {
        return try {
            val fechaActual = obtenerFechaActualUTC()

            db.update({
                set("estado", nuevoEstado)

                if (nuevoEstado == "activo") {
                    set("fecha_prestamo", fechaActual)
                } else if (nuevoEstado == "devuelto") {
                    set("fecha_devolucion", fechaActual)
                }
            }) {
                filter {
                    eq("id_prestamo", idPrestamo)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("PrestamoRepo", "Error al actualizar estado: ${e.message}")
            false
        }
    }
}