package com.example.biblioteca.data.repository

import android.util.Log
import com.example.biblioteca.core.SupabaseClientHelper
import com.example.biblioteca.data.model.Libro
import io.github.jan.supabase.postgrest.from

class LibroRepository {
    private val db = SupabaseClientHelper.client.from("libros")

    // Obtener catalogo completo
    suspend fun obtenerTodosLosLibros(): List<Libro> {
        return try {
            db.select().decodeList<Libro>()
        } catch (e: Exception) {
            Log.e("LibroRepo", "Error al obtener libros: ${e.message}")
            emptyList()
        }
    }

    // Busqueda general
    suspend fun buscarLibros(consulta: String): List<Libro> {
        return try {
            db.select {
                filter {
                    or {
                        ilike("titulo", "%$consulta%")
                        ilike("autor", "%$consulta%")
                    }
                }
            }.decodeList<Libro>()
        } catch (e: Exception) {
            Log.e("LibroRepo", "Error al buscar libros: ${e.message}")
            emptyList()
        }
    }

    // Busqueda filtrada
    suspend fun filtrarPorCategoria(categoria: String): List<Libro> {
        return try {
            db.select {
                filter {
                    eq("categoria", categoria)
                }
            }.decodeList<Libro>()
        } catch (e: Exception) {
            Log.e("LibroRepo", "Error al filtrar por categoría: ${e.message}")
            emptyList()
        }
    }

    // Actualizar inventario
    suspend fun actualizarDisponibilidad(idLibro: String, nuevaCantidad: Int): Boolean {
        return try {
            db.update({
                set("disponible", nuevaCantidad)
            }) {
                filter {
                    eq("id_libro", idLibro)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("LibroRepo", "Error al actualizar disponibilidad: ${e.message}")
            false
        }
    }

    // Insertar libro
    suspend fun insertarLibro(libro: Libro): Boolean {
        return try {
            db.insert(libro)
            true
        } catch (e: Exception) {
            Log.e("LibroRepo", "Error al guardar Libro: ${e.message}")
            false
        }
    }

    // Actualizar
    suspend fun actualizarLibro(
        idLibro: String, titulo: String, autor: String,
        categoria: String, descripcion: String, urlPortada: String, disponible: Int
    ): Boolean {
        return try {
            db.update({
                set("titulo", titulo)
                set("autor", autor)
                set("categoria", categoria)
                set("descripcion", descripcion)
                set("url_portada", urlPortada)
                set("disponible", disponible)
            }) {
                filter { eq("id_libro", idLibro) }
            }
            true
        } catch (e: Exception) {
            Log.e("LibroRepo", "Error al actualizar libro: ${e.message}")
            false
        }
    }

    // Eliminar un libro
    suspend fun eliminarLibro(idLibro: String): Boolean {
        return try {
            db.delete {
                filter {
                    eq("id_libro", idLibro)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("LibroRepo", "Error al eliminar libro: ${e.message}")
            false
        }
    }
}