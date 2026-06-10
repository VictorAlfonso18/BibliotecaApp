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
import androidx.lifecycle.viewmodel.compose.viewModel

// Inicializador de Supabase
import com.example.biblioteca.core.SupabaseClientHelper

// Importaciones de Vistas y ViewModels
import com.example.biblioteca.ui.screen.LibrosScreen
import com.example.biblioteca.ui.screen.PerfilScreen
import com.example.biblioteca.ui.screen.PrestamosScreen
import com.example.biblioteca.ui.screen.LoginScreen
import com.example.biblioteca.ui.screen.RegistroScreen
import com.example.biblioteca.ui.screen.admin.AdminLibrosScreen
import com.example.biblioteca.ui.screen.admin.AdminPrestamosScreen
import com.example.biblioteca.ui.screen.admin.AdminUsuariosScreen
import com.example.biblioteca.ui.theme.BibliotecaTheme
import com.example.biblioteca.ui.viewmodels.AuthViewModel
import com.example.biblioteca.ui.viewmodels.LibrosViewModel
import com.example.biblioteca.ui.viewmodels.PerfilViewModel
import com.example.biblioteca.ui.viewmodels.PrestamosViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Iniciar Supabase
        SupabaseClientHelper.initialize(applicationContext)

        setContent {
            BibliotecaTheme {
                BibliotecaApp()
            }
        }
    }
}

// Definimos los roles
enum class UserRole {
    NONE, USER, ADMIN
}

// Actualizamos las rutas
enum class AppDestinations(
    val label: String,
    val icon: Int,
    val role: UserRole
) {
    LOGIN("Login", R.drawable.ic_account_box, UserRole.NONE),
    REGISTRO("Registro", R.drawable.ic_account_box, UserRole.NONE),
    HOME("Libros", R.drawable.ic_home, UserRole.USER),
    FAVORITES("Préstamos", R.drawable.ic_favorite, UserRole.USER),
    PROFILE("Perfil", R.drawable.ic_account_box, UserRole.USER),
    ADMIN_HOME("Admin Libros", R.drawable.ic_home, UserRole.ADMIN),
    ADMIN_PRESTAMOS("Admin Préstamos", R.drawable.ic_favorite, UserRole.ADMIN),
    ADMIN_USUARIOS("Usuarios", R.drawable.ic_account_box, UserRole.ADMIN)
}

@PreviewScreenSizes
@Composable
fun BibliotecaApp() {

    val authViewModel: AuthViewModel = viewModel()
    val librosViewModel: LibrosViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()
    val prestamosViewModel: PrestamosViewModel = viewModel()

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.LOGIN) }
    var currentUserRole by rememberSaveable { mutableStateOf(UserRole.NONE) }

    val showNavigationBars = currentDestination.role != UserRole.NONE

    if (showNavigationBars) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries
                    .filter { it.role == currentUserRole }
                    .forEach { destination ->
                        item(
                            icon = { Icon(painterResource(destination.icon), contentDescription = destination.label) },
                            label = { Text(destination.label) },
                            selected = destination == currentDestination,
                            onClick = { currentDestination = destination }
                        )
                    }
            }
        ) {
            MainContentWrapper(
                currentDestination = currentDestination,
                onRoleChange = { currentUserRole = it },
                onNavigate = { currentDestination = it },
                authViewModel = authViewModel,
                librosViewModel = librosViewModel,
                perfilViewModel = perfilViewModel,
                prestamosViewModel = prestamosViewModel
            )
        }
    } else {
        MainContentWrapper(
            currentDestination = currentDestination,
            onRoleChange = { currentUserRole = it },
            onNavigate = { currentDestination = it },
            authViewModel = authViewModel,
            librosViewModel = librosViewModel,
            perfilViewModel = perfilViewModel,
            prestamosViewModel = prestamosViewModel
        )
    }
}

@Composable
fun MainContentWrapper(
    currentDestination: AppDestinations,
    onRoleChange: (UserRole) -> Unit,
    onNavigate: (AppDestinations) -> Unit,
    authViewModel: AuthViewModel,
    librosViewModel: LibrosViewModel,
    perfilViewModel: PerfilViewModel,
    prestamosViewModel: PrestamosViewModel
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentDestination) {
                AppDestinations.LOGIN -> {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            // TODO: Más adelante, aquí consultaremos la BD para saber si es ADMIN o USER.
                            onRoleChange(UserRole.USER)
                            onNavigate(AppDestinations.HOME)
                        },
                        onNavigateToRegistro = {
                            onNavigate(AppDestinations.REGISTRO)
                        }
                    )
                }
                AppDestinations.REGISTRO -> {
                    RegistroScreen(
                        viewModel = authViewModel,
                        onRegistroSuccess = {
                            onRoleChange(UserRole.USER)
                            onNavigate(AppDestinations.HOME)
                        }
                    )
                }

                // RUTAS DE CLIENTE
                AppDestinations.HOME -> {
                    LibrosScreen(viewModel = librosViewModel)
                }
                AppDestinations.FAVORITES -> {
                    PrestamosScreen(viewModel = prestamosViewModel)
                }
                AppDestinations.PROFILE -> {
                    PerfilScreen(
                        viewModel = perfilViewModel,
                        onSignOutSuccess = {
                            onRoleChange(UserRole.NONE)
                            onNavigate(AppDestinations.LOGIN)
                        }
                    )
                }

                // RUTAS DE ADMINISTRADOR
                AppDestinations.ADMIN_HOME -> { AdminLibrosScreen() }
                AppDestinations.ADMIN_PRESTAMOS -> { AdminPrestamosScreen() }
                AppDestinations.ADMIN_USUARIOS -> { AdminUsuariosScreen() }
            }
        }
    }
}