package com.example.spire_task.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spire_task.feature.home.SpiroMainContainer
import com.example.spire_task.feature.onboarding.OnboardingScreen
import com.example.spire_task.feature.splash.SplashScreen

/**
 * Rutas de navegación de la aplicación.
 */
object Rutas {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
}

/**
 * Grafo de navegación principal de Spiro Task.
 *
 * Controla el flujo:
 * Splash → Onboarding (si no hay perfil) → Home
 * Splash → Home (si ya hay perfil)
 */
@Composable
fun SpiroNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.SPLASH
    ) {
        // ─── SPLASH ──────────────────────────────────
        composable(Rutas.SPLASH) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Rutas.ONBOARDING) {
                        // Eliminar Splash de la pila para no volver atrás
                        popUpTo(Rutas.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Rutas.HOME) {
                        // Eliminar Splash de la pila para no volver atrás
                        popUpTo(Rutas.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ─── ONBOARDING ──────────────────────────────
        composable(Rutas.ONBOARDING) {
            OnboardingScreen(
                onOnboardingCompletado = {
                    navController.navigate(Rutas.HOME) {
                        // Eliminar Onboarding de la pila
                        popUpTo(Rutas.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ─── HOME ────────────────────────────────────
        composable(Rutas.HOME) {
            // ✅ CORREGIDO: Ahora llama al contenedor que hereda la barra inferior
            SpiroMainContainer(
                onSesionCerrada = {
                    // Si el usuario cierra sesión desde los ajustes, lo mandas al Onboarding o Splash
                    navController.navigate(Rutas.ONBOARDING) {
                        popUpTo(Rutas.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}