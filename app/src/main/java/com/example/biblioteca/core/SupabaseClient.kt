////package com.example.biblioteca.core
//
////import android.content.Context
//import android.content.SharedPreferences
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.auth.Auth
//import io.github.jan.supabase.auth.SessionManager
//import io.github.jan.supabase.auth.user.UserSession
//import io.github.jan.supabase.createSupabaseClient
//import io.github.jan.supabase.postgrest.Postgrest
//import io.github.jan.supabase.storage.Storage
//import io.github.jan.supabase.serializer.KotlinXSerializer
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//
//object SupabaseClientHelper {
//
//    private const val SUPABASE_URL = "TU_URL_AQUI"
//    private const val SUPABASE_KEY = "TU_ANON_KEY_AQUI"
//
//    lateinit var client: SupabaseClient
//
//    fun initialize(context: Context) {
//        client = createSupabaseClient(
//            supabaseUrl = SUPABASE_URL,
//            supabaseKey = SUPABASE_KEY
//        ) {
//            install(Auth) {
//                sessionManager = AndroidSessionManager(context)
//            }
//            install(Postgrest)
//            install(Storage)
//
//            defaultSerializer = KotlinXSerializer(Json {
//                ignoreUnknownKeys = true
//            })
//        }
//    }
//}
//
//class AndroidSessionManager(context: Context) : SessionManager {
//
//    private val prefs: SharedPreferences =
//        context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
//
//    override suspend fun saveSession(session: UserSession) {
//        val jsonSession = Json.encodeToString(session)
//        prefs.edit().putString("session_key", jsonSession).apply()
//    }
//
//    override suspend fun loadSession(): UserSession? {
//        val jsonSession = prefs.getString("session_key", null) ?: return null
//        return try {
//            Json.decodeFromString<UserSession>(jsonSession)
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    override suspend fun deleteSession() {
//        prefs.edit().remove("session_key").apply()
//    }
//}