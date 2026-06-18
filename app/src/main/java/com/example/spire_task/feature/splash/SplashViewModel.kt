package com.example.spire_task.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.database.SpiroDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados posibles del Splash.
 */
sealed class SplashEstado {
    /** Mostrando la pantalla de carga */
    data object Cargando : SplashEstado()

    /** No hay perfil → Ir al Onboarding */
    data object IrAOnboarding : SplashEstado()

    /** Ya existe perfil → Ir al Home */
    data object IrAHome : SplashEstado()
}

/**
 * ViewModel para la pantalla Splash.
 *
 * Responsabilidades:
 * - Esperar tiempo mínimo de marca (1.5 segundos)
 * - Verificar si existe un perfil en Room
 * - Emitir el estado correspondiente para navegar
 */
class SplashViewModel(
    private val database: SpiroDatabase
) : ViewModel() {

    private val _estado = MutableStateFlow<SplashEstado>(SplashEstado.Cargando)
    val estado: StateFlow<SplashEstado> = _estado.asStateFlow()

    init {
        verificarPerfil()
    }

    /**
     * Verifica si existe un perfil en la base de datos.
     *
     * Flujo:
     * 1. Espera 1.5 segundos (tiempo mínimo de splash)
     * 2. Consulta Room para ver si hay perfil
     * 3. Emite el estado correspondiente
     */
    private fun verificarPerfil() {
        viewModelScope.launch {
            try {
                // Tiempo mínimo para mostrar la marca
                delay(1500L)

                // Consultar si existe perfil
                val count = database.perfilUsuarioDao().existePerfil()

                // Emitir el estado según resultado
                _estado.value = if (count > 0) {
                    SplashEstado.IrAHome
                } else {
                    SplashEstado.IrAOnboarding
                }
            } catch (e: Exception) {
                // Si hay error en la BD, asumimos que no hay perfil
                // y dejamos que el onboarding cree uno nuevo
                _estado.value = SplashEstado.IrAOnboarding
            }
        }
    }
}