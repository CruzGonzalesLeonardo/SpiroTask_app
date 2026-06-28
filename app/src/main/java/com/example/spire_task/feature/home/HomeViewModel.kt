package com.example.spire_task.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.MascotaUsuarioEntity
import com.example.spire_task.data.local.entidades.PerfilUsuarioEntity
import com.example.spire_task.data.local.entidades.TareaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val perfil: PerfilUsuarioEntity? = null,
    val mascotaActiva: MascotaUsuarioEntity? = null,
    val nombreMascota: String = "",
    val emojiMascota: String = "🦉",
    val rutaAssetMascota: String? = null,
    val rutaHabitadMascota: String? = null,  // ✅ NUEVO CAMPO
    val rutaAssetTristeMascota: String? = null,
    val felicidadMascota: Int = 100,
    val estaCargando: Boolean = true,
    val tareasCompletadasHoy: Int = 0,
    val tareasTotalesHoy: Int = 0,
    val minutosEnfocadoHoy: Int = 0,
    val rachaDias: Int = 0,
    val tareasPendientes: List<TareaEntity> = emptyList()
)

class HomeViewModel(
    private val database: SpiroDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observarPerfil()
        observarMascotaActiva()
        cargarEstadisticas()
        cargarTareasPendientes()
    }

    private fun observarPerfil() {
        viewModelScope.launch {
            try {
                database.perfilUsuarioDao().obtenerPerfil().collect { perfil ->
                    _uiState.update { it.copy(perfil = perfil, estaCargando = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(estaCargando = false) }
            }
        }
    }

    private fun observarMascotaActiva() {
        viewModelScope.launch {
            try {
                database.mascotaUsuarioDao().obtenerMascotaActiva().collect { mascota ->
                    if (mascota != null) {
                        val especie = database.mascotaBaseDao().obtenerPorId(mascota.id_mascota_base)
                        _uiState.update {
                            it.copy(
                                mascotaActiva = mascota,
                                nombreMascota = especie?.nombre_especie ?: "",
                                emojiMascota = especie?.emoji ?: "🦉",
                                rutaAssetMascota = especie?.ruta_asset_base,
                                rutaAssetTristeMascota = especie?.ruta_asset_triste,
                                rutaHabitadMascota = especie?.ruta_asset_habitad ,
                                felicidadMascota = mascota.felicidad_actual// ✅ AGREGADO
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                mascotaActiva = null,
                                nombreMascota = "",
                                emojiMascota = "🦉",
                                rutaAssetMascota = null,
                                rutaAssetTristeMascota = null,
                                rutaHabitadMascota = null,
                                felicidadMascota = 100// ✅ AGREGADO
                            )
                        }
                    }
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }

    private fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                val todasTareas = database.tareaDao().obtenerTodasActivas()
                val hoy = System.currentTimeMillis()
                val inicioDia = hoy - (hoy % 86400000)

                val tareasHoy = todasTareas.filter { it.fecha_modificacion >= inicioDia }
                val completadas = tareasHoy.count { it.estado == "FINALIZADO" }
                val total = tareasHoy.size

                _uiState.update {
                    it.copy(
                        tareasCompletadasHoy = completadas,
                        tareasTotalesHoy = total,
                        rachaDias = 0
                    )
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }

    private fun cargarTareasPendientes() {
        viewModelScope.launch {
            try {
                database.tareaDao().obtenerTodasActivas().let { tareas ->
                    val pendientes = tareas.filter {
                        it.estado == "POR_HACER" || it.estado == "EN_PROGRESO"
                    }.sortedBy { it.fecha_limite }

                    _uiState.update { it.copy(tareasPendientes = pendientes) }
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }
    fun interactuarConMascota() {
        viewModelScope.launch {
            val mascotaActual = _uiState.value.mascotaActiva ?: return@launch
            val nuevaFelicidad = (mascotaActual.felicidad_actual + 10).coerceIn(0, 100)

            database.mascotaUsuarioDao().actualizarFelicidad(
                id = mascotaActual.id_mascota_usuario,
                felicidad = nuevaFelicidad
            )

            _uiState.update {
                it.copy(felicidadMascota = nuevaFelicidad)
            }
        }
    }
}