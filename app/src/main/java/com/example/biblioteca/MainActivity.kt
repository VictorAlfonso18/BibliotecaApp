package com.example.biblioteca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

// Importaciones de Usuario
import com.example.biblioteca.ui.screen.LibrosScreen
import com.example.biblioteca.ui.screen.PerfilScreen
import com.example.biblioteca.ui.screen.PrestamosScreen
import com.example.biblioteca.ui.screen.LoginScreen
import com.example.biblioteca.ui.screen.RegistroScreen

// Importaciones de Admin
import com.example.biblioteca.ui.screen.admin.AdminLibrosScreen
import com.example.biblioteca.ui.screen.admin.AdminPrestamosScreen
import com.example.biblioteca.ui.screen.admin.AdminUsuariosScreen

import com.example.biblioteca.ui.theme.BibliotecaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BibliotecaTheme {
                BibliotecaApp()
            }
        }
    }
}

// 1. Definimos los roles
enum class UserRole {
    NONE, USER, ADMIN
}

// 2. Actualizamos las rutas asignándoles su rol correspondiente
enum class AppDestinations(
    val label: String,
    val icon: Int,
    val role: UserRole
) {
    // Pantallas generales
    LOGIN("Login", R.drawable.ic_account_box, UserRole.NONE),
    REGISTRO("Registro", R.drawable.ic_account_box, UserRole.NONE),

    // Pantallas de Usuario Normal
    HOME("Libros", R.drawable.ic_home, UserRole.USER),
    FAVORITES("Préstamos", R.drawable.ic_favorite, UserRole.USER),
    PROFILE("Perfil", R.drawable.ic_account_box, UserRole.USER),

    // Pantallas de Administrador
    ADMIN_HOME("Admin Libros", R.drawable.ic_home, UserRole.ADMIN),
    ADMIN_PRESTAMOS("Admin Préstamos", R.drawable.ic_favorite, UserRole.ADMIN),
    ADMIN_USUARIOS("Usuarios", R.drawable.ic_account_box, UserRole.ADMIN)
}

@PreviewScreenSizes
@Composable
fun BibliotecaApp() {

    // La app arranca directamente en la pantalla de LOGIN
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.LOGIN)
    }

    // El rol arranca vacío hasta que se elija en el Login
    var currentUserRole by rememberSaveable {
        mutableStateOf(UserRole.NONE)
    }

    val showNavigationBars = currentDestination.role != UserRole.NONE

    if (showNavigationBars) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                // Solo dibuja en la barra de abajo los elementos del ROL ACTUAL
                AppDestinations.entries
                    .filter { it.role == currentUserRole }
                    .forEach { destination ->
                        item(
                            icon = {
                                Icon(
                                    painter = painterResource(destination.icon),
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(destination.label)
                            },
                            selected = destination == currentDestination,
                            onClick = {
                                currentDestination = destination
                            }
                        )
                    }
            }
        ) {
            MainContentWrapper(
                currentDestination = currentDestination,
                onRoleChange = { currentUserRole = it },
                onNavigate = { currentDestination = it }
            )
        }
    } else {
        MainContentWrapper(
            currentDestination = currentDestination,
            onRoleChange = { currentUserRole = it },
            onNavigate = { currentDestination = it }
        )
    }
}

@Composable
fun MainContentWrapper(
    currentDestination: AppDestinations,
    onRoleChange: (UserRole) -> Unit,
    onNavigate: (AppDestinations) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (currentDestination) {
                // Rutas sin barra
                AppDestinations.LOGIN -> {
                    LoginScreen(
                        onLoginUser = {
                            onRoleChange(UserRole.USER)
                            onNavigate(AppDestinations.HOME)
                        },
                        onLoginAdmin = {
                            onRoleChange(UserRole.ADMIN)
                            onNavigate(AppDestinations.ADMIN_HOME)
                        },
                        onNavigateToRegistro = {
                            onNavigate(AppDestinations.REGISTRO)
                        }
                    )
                }
                AppDestinations.REGISTRO -> {
                    RegistroScreen(
                        onRegistroSuccess = {
                            onNavigate(AppDestinations.LOGIN)
                        }
                    )
                }

                // Rutas de Usuario Normal
                AppDestinations.HOME -> { LibrosScreen() }
                AppDestinations.FAVORITES -> { PrestamosScreen() }
                AppDestinations.PROFILE -> { PerfilScreen() }

                // Rutas de Administrador
                AppDestinations.ADMIN_HOME -> { AdminLibrosScreen() }
                AppDestinations.ADMIN_PRESTAMOS -> { AdminPrestamosScreen() }
                AppDestinations.ADMIN_USUARIOS -> { AdminUsuariosScreen() }
            }
        }
    }
}