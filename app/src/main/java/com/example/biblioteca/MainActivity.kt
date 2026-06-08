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
import com.example.biblioteca.ui.screen.LibrosScreen
import com.example.biblioteca.ui.screen.PerfilScreen
import com.example.biblioteca.ui.screen.PrestamosScreen
import com.example.biblioteca.ui.screen.LoginScreen
import com.example.biblioteca.ui.screen.RegistroScreen
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

@PreviewScreenSizes
@Composable
fun BibliotecaApp() {

    // La app arranca directo en la pantalla limpia de LOGIN
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.LOGIN)
    }

    // Oculta la barra de navegación de abajo si estamos en LOGIN o REGISTRO
    val showNavigationBars = currentDestination != AppDestinations.LOGIN && currentDestination != AppDestinations.REGISTRO

    if (showNavigationBars) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                // Solo dibuja en la barra de abajo los elementos marcados con showInBottomBar = true
                AppDestinations.entries
                    .filter { it.showInBottomBar }
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
            MainContentWrapper(currentDestination) { currentDestination = it }
        }
    } else {
        MainContentWrapper(currentDestination) { currentDestination = it }
    }
}

@Composable
fun MainContentWrapper(currentDestination: AppDestinations, onNavigate: (AppDestinations) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestinations.LOGIN -> {
                    LoginScreen(
                        onLoginSuccess = { correoDigitado, passwordDigitado ->
                            // Aquí es donde tu compañero meterá la corrutina de Supabase.
                            // Por ahora, al dar clic ingresa directo para que pruebes la navegación.
                            onNavigate(AppDestinations.HOME)
                        },
                        onNavigateToRegistro = {
                            onNavigate(AppDestinations.REGISTRO)
                        }
                    )
                }
                AppDestinations.REGISTRO -> {
                    RegistroScreen(
                        onRegistroSuccess = {
                            // Al registrarse, lo regresa al Login
                            onNavigate(AppDestinations.LOGIN)
                        }
                    )
                }
                AppDestinations.HOME -> { LibrosScreen() }
                AppDestinations.FAVORITES -> { PrestamosScreen() }
                AppDestinations.PROFILE -> { PerfilScreen() }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
    val showInBottomBar: Boolean
) {
    LOGIN("Login", R.drawable.ic_account_box, showInBottomBar = false),
    REGISTRO("Registro", R.drawable.ic_account_box, showInBottomBar = false),
    HOME("Libros", R.drawable.ic_home, showInBottomBar = true),
    FAVORITES("Préstamos", R.drawable.ic_favorite, showInBottomBar = true),
    PROFILE("Perfil", R.drawable.ic_account_box, showInBottomBar = true),
}