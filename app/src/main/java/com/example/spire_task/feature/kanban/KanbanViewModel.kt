package com.example.spire_task.feature.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.*
import com.example.spire_task.data.repository.RecompensaService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * 📦 ESTADO DE LA INTERFAZ KANBAN
 */
data class KanbanUiState(
    val tablero: TableroEntity? = null,
    val tareasPorHacer: List<TareaEntity> = emptyList(),
    val tareasEnProgreso: List<TareaEntity> = emptyList(),
    val tareasFinalizadas: List<TareaEntity> = emptyList(),
    val filtroActivo: String = "POR_HACER",
    val estaCargando: Boolean = true,
    val mostrarDialogoCrear: Boolean = false,
    val error: String? = null,
    val mascotaMentora: MascotaCompletaKanban? = null,
    val habilidadActiva: HabilidadCompleta? = null,
    val felicidadMascota: Int = 100,
    val colorFondo: String = "#4A90E2",
    val limitePorHacer: Int = KanbanViewModel.LIMITE_POR_HACER,
    val limiteEnProgreso: Int = KanbanViewModel.LIMITE_EN_PROGRESO
)

data class MascotaCompletaKanban(
    val idMascotaUsuario: Int,
    val nombreEspecie: String,
    val emoji: String,
    val nivel: Int,
    val felicidad: Int,
    val rutaAsset: String?,
    val rutaAssetTriste: String?,
    val rutaAssetLike: String?
)

data class HabilidadCompleta(
    val idHabilidad: Int,
    val nombre: String,
    val descripcion: String,
    val nivelRequerido: Int,
    val tipoAplicacion: String,
    val desbloqueada: Boolean,
    val valorModificador: Float
)

data class RecompensaMostrada(val xp: Int, val monedas: Int, val bonos: List<String>)
sealed class ResultadoRecompensa {
    data class Exito(val recompensa: RecompensaCalculada) : ResultadoRecompensa()
    data class Error(val mensaje: String) : ResultadoRecompensa()
}
data class RecompensaCalculada(val xp: Int, val monedas: Int, val multiplicador: Float, val bonosAplicados: List<String> = emptyList())

/**
 * ⚙️ CONTROLADOR DE NEGOCIO (VIEWMODEL)
 */
class KanbanViewModel(
    private val database: SpiroDatabase,
    private val tableroId: Int
) : ViewModel() {

    companion object {
        const val LIMITE_POR_HACER = 10
        const val LIMITE_EN_PROGRESO = 4

        private const val XP_BASE_POR_TAREA = 50
        private const val MONEDAS_BASE_POR_TAREA = 10

        private const val BONO_PRIORIDAD_ALTA = 1.5f
        private const val BONO_PRIORIDAD_MEDIA = 1.25f
        private const val BONO_PRIORIDAD_BAJA = 1.0f

        private const val BONO_ENTREGA_ANTICIPADA = 1.5f
        private const val BONO_ENTREGA_A_TIEMPO = 1.25f
        private const val BONO_ENTREGA_TARDE = 1.0f
    }

    private val _uiState = MutableStateFlow(KanbanUiState())
    val uiState: StateFlow<KanbanUiState> = _uiState.asStateFlow()

    init {
        cargarTablero()
        cargarMascotaMentora()
        cargarTareas()
    }

    private fun cargarTablero() {
        viewModelScope.launch {
            try {
                val tablero = database.tableroDao().obtenerPorId(tableroId)
                _uiState.update { it.copy(tablero = tablero, colorFondo = tablero?.color_hex ?: "#4A90E2") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al cargar tablero") }
            }
        }
    }

    private fun cargarMascotaMentora() {
        viewModelScope.launch {
            try {
                val tablero = database.tableroDao().obtenerPorId(tableroId)
                if (tablero != null) {
                    val mascotaUsuario = database.mascotaUsuarioDao().obtenerPorId(tablero.id_mascota_mentora)
                    if (mascotaUsuario != null) {
                        val especie = database.mascotaBaseDao().obtenerPorId(mascotaUsuario.id_mascota_base)

                        val habilidad = if (especie != null && especie.habilidad_nombre.isNotBlank()) {
                            HabilidadCompleta(
                                idHabilidad = 0,
                                nombre = especie.habilidad_nombre,
                                descripcion = especie.habilidad_descripcion,
                                nivelRequerido = 1,
                                tipoAplicacion = especie.habilidad_tipo,
                                desbloqueada = true,
                                valorModificador = especie.habilidad_valor
                            )
                        } else null

                        _uiState.update {
                            it.copy(
                                mascotaMentora = MascotaCompletaKanban(
                                    idMascotaUsuario = mascotaUsuario.id_mascota_usuario,
                                    nombreEspecie = especie?.nombre_especie ?: "",
                                    emoji = especie?.emoji ?: "🐾",
                                    nivel = mascotaUsuario.nivel,
                                    felicidad = mascotaUsuario.felicidad_actual,
                                    rutaAsset = especie?.ruta_asset_base,
                                    rutaAssetTriste = especie?.ruta_asset_triste,
                                    rutaAssetLike = especie?.ruta_asset_like
                                ),
                                habilidadActiva = habilidad,
                                felicidadMascota = mascotaUsuario.felicidad_actual,
                                colorFondo = tablero.color_hex
                            )
                        }
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun cargarTareas() {
        viewModelScope.launch {
            try {
                database.tareaDao().obtenerPorTablero(tableroId).collect { tareas ->
                    val porHacer = tareas.filter { it.estado == "POR_HACER" }
                    val enProgreso = tareas.filter { it.estado == "EN_PROGRESO" }
                    val finalizadas = tareas.filter { it.estado == "FINALIZADO" }
                    _uiState.update {
                        it.copy(
                            tareasPorHacer = porHacer,
                            tareasEnProgreso = enProgreso,
                            tareasFinalizadas = finalizadas,
                            estaCargando = false
                        )
                    }
                }
            } catch (e: Exception) { }
        }
    }

    fun cambiarFiltro(estado: String) {
        _uiState.update { it.copy(filtroActivo = estado) }
    }

    fun toggleDialogoCrear(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarDialogoCrear = mostrar, error = null) }
    }

    fun crearTarea(titulo: String, prioridad: Int, fechaLimite: Long) {
        viewModelScope.launch {
            try {
                if (_uiState.value.tareasPorHacer.size >= LIMITE_POR_HACER) {
                    _uiState.update { it.copy(error = "Límite alcanzado: máximo $LIMITE_POR_HACER tareas en Por Hacer") }
                    return@launch
                }

                val tarea = TareaEntity(
                    id_tablero = tableroId,
                    titulo = titulo.trim(),
                    prioridad = prioridad,
                    estado = "POR_HACER",
                    fecha_limite = fechaLimite
                )
                database.tareaDao().insertar(tarea)
                _uiState.update { it.copy(mostrarDialogoCrear = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al crear tarea") }
            }
        }
    }

    fun cambiarEstadoTarea(tarea: TareaEntity, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                when (nuevoEstado) {
                    "EN_PROGRESO" -> {
                        if (_uiState.value.tareasEnProgreso.size >= LIMITE_EN_PROGRESO) {
                            _uiState.update { it.copy(error = "Límite alcanzado: máximo $LIMITE_EN_PROGRESO tareas en Progreso") }
                            return@launch
                        }
                    }
                    "POR_HACER" -> {
                        if (_uiState.value.tareasPorHacer.size >= LIMITE_POR_HACER) {
                            _uiState.update { it.copy(error = "Límite alcanzado: máximo $LIMITE_POR_HACER tareas en Por Hacer") }
                            return@launch
                        }
                    }
                }

                database.tareaDao().cambiarEstado(tarea.id_tarea, nuevoEstado)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al mover tarea: ${e.message}") }
            }
        }
    }

    suspend fun completarTareaYReclamar(tarea: TareaEntity): ResultadoRecompensa {
        return try {
            val tareaActual = database.tareaDao().obtenerPorId(tarea.id_tarea) ?: return ResultadoRecompensa.Error("Tarea no encontrada")

            if (tareaActual.estado == "FINALIZADO" && tareaActual.recompensa_reclamada) {
                return ResultadoRecompensa.Error("La recompensa ya fue reclamada anteriormente")
            } else if (tareaActual.estado != "FINALIZADO") {
                database.tareaDao().cambiarEstado(tarea.id_tarea, "FINALIZADO")
            }

            val tareaFinalizada = database.tareaDao().obtenerPorId(tarea.id_tarea) ?: return ResultadoRecompensa.Error("Error al actualizar tarea")

            val tablero = database.tableroDao().obtenerPorId(tableroId)
            var habilidadMascota: MascotaBaseEntity? = null
            var esMascotaActiva = false

            tablero?.let {
                val mascotaUsuario = database.mascotaUsuarioDao().obtenerPorId(it.id_mascota_mentora)
                mascotaUsuario?.let { m ->
                    habilidadMascota = database.mascotaBaseDao().obtenerPorId(m.id_mascota_base)
                    esMascotaActiva = true
                }
            }

            // 1. Invocar al servicio con las actualizaciones de felicidad dinámicas (10, 20, 30)
            val recompensaService = RecompensaService(context = com.example.spire_task.SpiroTaskApplication.instance)
            val recompensa = recompensaService.calcularRecompensa(
                tarea = tareaFinalizada,
                habilidadMascota = habilidadMascota,
                esMascotaActiva = esMascotaActiva
            )

            // Si el servicio detectó límite alcanzado, cancelamos la entrega de recursos
            if (recompensa.xpFinal == 0 && recompensa.monedasFinal == 0) {
                return ResultadoRecompensa.Error(recompensa.bonosAplicados.firstOrNull() ?: "Límite diario alcanzado")
            }

            // 2. Asignar recompensas al perfil de usuario
            val perfil = database.perfilUsuarioDao().obtenerPerfilDirecto() ?: return ResultadoRecompensa.Error("Perfil no encontrado")
            database.perfilUsuarioDao().actualizarMonedas(perfil.monedas + recompensa.monedasFinal)
            database.perfilUsuarioDao().sumarExperiencia(recompensa.xpFinal)

            // 3. Modificar la experiencia y la FELICIDAD de la mascota mentora de manera dinámica
            tablero?.let { t ->
                database.mascotaUsuarioDao().obtenerPorId(t.id_mascota_mentora)?.let { mascota ->
                    // Sumar experiencia y verificar nivel
                    database.mascotaUsuarioDao().sumarExperiencia(mascota.id_mascota_usuario, recompensa.xpFinal)
                    verificarSubidaNivelMascota(mascota)

                    // 🔥 CORRECCIÓN: Usar el incremento calculado por el servicio (10, 20 o 30) en el DAO
                    if (recompensa.incrementoFelicidad > 0) {
                        database.mascotaUsuarioDao().incrementarFelicidad(
                            id = mascota.id_mascota_usuario,
                            incremento = recompensa.incrementoFelicidad
                        )
                    }
                }
            }

            // 4. Guardar estado en la base de datos de la tarea
            database.tareaDao().marcarRecompensaReclamada(tarea.id_tarea)

            // 5. Actualizar el estado de la UI de la mascota en tiempo real para reflejar su nueva felicidad
            cargarMascotaMentora()

            val recompensaCalculada = RecompensaCalculada(
                xp = recompensa.xpFinal,
                monedas = recompensa.monedasFinal,
                multiplicador = (recompensa.multiplicadorXp + recompensa.multiplicadorMonedas) / 2,
                bonosAplicados = recompensa.bonosAplicados
            )

            ResultadoRecompensa.Exito(recompensaCalculada)
        } catch (e: Exception) {
            ResultadoRecompensa.Error("Error al reclamar recompensa: ${e.message}")
        }
    }

    private suspend fun verificarSubidaNivelMascota(mascota: MascotaUsuarioEntity) {
        val xpNecesaria = mascota.nivel * 100
        val mascotaActualizada = database.mascotaUsuarioDao().obtenerPorId(mascota.id_mascota_usuario)
        if (mascotaActualizada != null && mascotaActualizada.experiencia >= xpNecesaria) {
            database.mascotaUsuarioDao().subirNivel(mascota.id_mascota_usuario)
        }
    }

    fun eliminarTarea(tarea: TareaEntity) {
        viewModelScope.launch {
            try {
                database.tareaDao().borrarLogicamente(tarea.id_tarea)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar tarea") }
            }
        }
    }

    fun setError(mensaje: String) { _uiState.update { it.copy(error = mensaje) } }
    fun limpiarError() { _uiState.update { it.copy(error = null) } }
}