package com.example.biblioteca.data.repository

import android.util.Log
import com.example.biblioteca.core.SupabaseClientHelper
import com.example.biblioteca.data.model.Prestamo
import io.github.jan.supabase.postgrest.from
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PrestamoRepository {
    private val db = SupabaseClientHelper.client.from("prestamos")

    // Helper para obtener la fecha actual en formato ISO UTC para Supabase
    private fun obtenerFechaActualUTC(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    // Calcula el día de hoy y le añade una semana de vigencia para el plazo de entrega
    private fun obtenerFechaDevolucionLimiteUTC(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, 7) // Suma 7 días naturales de plazo

        return sdf.format(calendario.time)
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

    // Actualizar estado del prestamo y calcular las marcas de tiempo correspondientes
    suspend fun actualizarEstadoPrestamo(idPrestamo: String, nuevoEstado: String): Boolean {
        return try {
            val fechaActual = obtenerFechaActualUTC()

            db.update({
                set("estado", nuevoEstado)

                if (nuevoEstado == "activo") {
                    // El alumno retira el libro hoy
                    set("fecha_prestamo", fechaActual)

                    // Se almacena el límite en una columna específica para no pisar la devolución real
                    val fechaLimite = obtenerFechaDevolucionLimiteUTC()
                    set("fecha_entrega_limite", fechaLimite)

                    // Garantizamos que la fecha de devolución real empiece limpia
                    set<String>("fecha_devolucion", null)

                } else if (nuevoEstado == "devuelto") {
                    // El alumno regresa el libro hoy, registrando la fecha de entrega final
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

    suspend fun obtenerTodosLosPrestamos(): List<Prestamo> {
        return try {
            db.select().decodeList<Prestamo>()
        } catch (e: Exception) {
            Log.e("PrestamoRepo", "Error al obtener todos los préstamos: ${e.message}")
            emptyList()
        }
    }
}