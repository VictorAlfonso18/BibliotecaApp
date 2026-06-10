package com.example.biblioteca.data.repository

import android.util.Log
import com.example.biblioteca.core.SupabaseClientHelper
import com.example.biblioteca.data.model.Usuario
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class UsuarioRepository {
    private val db = SupabaseClientHelper.client.from("usuarios")
    private val storage = SupabaseClientHelper.client.storage.from("identificaciones")

    // Obtener datos del perfil
    suspend fun obtenerPerfil(idUsuario: String): Usuario? {
        return try {
            db.select {
                filter {
                    eq("id_usuario", idUsuario)
                }
            }.decodeSingleOrNull<Usuario>()
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al obtener perfil: ${e.message}")
            null
        }
    }

    // Subir identificacion
    suspend fun subirIdentificacion(idUsuario: String, fotoBytes: ByteArray): Boolean {
        return try {
            val nombreBucket = "identificaciones"
            val nombreArchivo = "${idUsuario}_credencial.jpg"

            val bucket = SupabaseClientHelper.client.storage.from(nombreBucket)
            bucket.upload(nombreArchivo, fotoBytes) {
                upsert = true
            }

            val urlPublica = bucket.publicUrl(nombreArchivo)

            db.update({
                set("url_identificacion", urlPublica)
            }) {
                filter {
                    eq("id_usuario", idUsuario)
                }
            }

            true
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al subir identificación: ${e.message}")
            false
        }
    }

    // Obtener todos los usuarios
    suspend fun obtenerTodosLosUsuarios(): List<Usuario> {
        return try {
            db.select().decodeList<Usuario>()
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al obtener usuarios: ${e.message}")
            emptyList()
        }
    }

    // Validar usuario
    suspend fun validarUsuario(idUsuario: String): Boolean {
        return try {
            db.update({
                set("verificado", true)
            }) {
                filter {
                    eq("id_usuario", idUsuario)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("UsuarioRepo", "Error al verificar usuario: ${e.message}")
            false
        }
    }
}