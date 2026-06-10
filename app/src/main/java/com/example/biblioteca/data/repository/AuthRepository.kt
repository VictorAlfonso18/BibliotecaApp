package com.example.biblioteca.data.repository

import android.util.Log
import com.example.biblioteca.core.SupabaseClientHelper
import com.example.biblioteca.data.model.Usuario
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from

class AuthRepository {
    private val auth = SupabaseClientHelper.client.auth
    private val db = SupabaseClientHelper.client.from("usuarios")

    // Login
    suspend fun iniciarSesion(correo: String, password: String): Result<Unit> {
        return try {
            auth.signInWith(Email) {
                this.email = correo
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarse(nombre: String, correo: String, pass: String): Result<Unit> {
        return try {
            val resultado = auth.signUpWith(Email) {
                this.email = correo
                this.password = pass
            }
            val userId = resultado?.id ?: throw Exception("Error al obtener ID del usuario registrado")

            val newUserProfile = Usuario(id = userId, correo = correo, nombre = nombre)
            db.insert(newUserProfile)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("REPO", "Error en registro: ${e.message}")
            Result.failure(e)
        }
    }

    // Verificar si hay usuario logueado
    fun isUserLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }

    // Obtener el ID del usuario actual
    fun getCurrentUserId(): String? = auth.currentUserOrNull()?.id

    // Cerrar sesion
    suspend fun signOut() {
        auth.signOut()
    }
}