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
                                    rutaAsset = especie?.ruta_asset_base
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
                    cargarSubtareas(tareas)
                }
            } catch (e: Exception) { }
        }
    }

    private suspend fun cargarSubtareas(tareas: List<TareaEntity>) {
        val mapa = mutableMapOf<Int, List<SubtareaEntity>>()
        tareas.forEach { tarea ->
            mapa[tarea.id_tarea] = database.subtareaDao().obtenerPorTareaDirecto(tarea.id_tarea)
        }
        _uiState.update { it.copy(subtareasPorTarea = mapa) }
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
                // Verificar límites antes de cambiar
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

                // Actualizar el estado en la base de datos
                database.tareaDao().cambiarEstado(tarea.id_tarea, nuevoEstado)

                // Actualizar el estado local
                val tareaActualizada = tarea.copy(
                    estado = nuevoEstado,
                    fecha_completado = if (nuevoEstado == "FINALIZADO" && tarea.fecha_completado == null) {
                        System.currentTimeMillis()
                    } else {
                        tarea.fecha_completado
                    }
                )

                // Actualizar la lista correspondiente
                when (nuevoEstado) {
                    "POR_HACER" -> {
                        _uiState.update { state ->
                            state.copy(
                                tareasPorHacer = (state.tareasPorHacer + tareaActualizada).sortedBy { it.orden },
                                tareasEnProgreso = state.tareasEnProgreso.filter { it.id_tarea != tarea.id_tarea },
                                tareasFinalizadas = state.tareasFinalizadas.filter { it.id_tarea != tarea.id_tarea },
                                error = null
                            )
                        }
                    }
                    "EN_PROGRESO" -> {
                        _uiState.update { state ->
                            state.copy(
                                tareasPorHacer = state.tareasPorHacer.filter { it.id_tarea != tarea.id_tarea },
                                tareasEnProgreso = (state.tareasEnProgreso + tareaActualizada).sortedBy { it.orden },
                                tareasFinalizadas = state.tareasFinalizadas.filter { it.id_tarea != tarea.id_tarea },
                                error = null
                            )
                        }
                    }
                    "FINALIZADO" -> {
                        _uiState.update { state ->
                            state.copy(
                                tareasPorHacer = state.tareasPorHacer.filter { it.id_tarea != tarea.id_tarea },
                                tareasEnProgreso = state.tareasEnProgreso.filter { it.id_tarea != tarea.id_tarea },
                                tareasFinalizadas = (state.tareasFinalizadas + tareaActualizada).sortedBy { it.orden },
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al mover tarea: ${e.message}") }
            }
        }
    }

    /**
     * ✅ NUEVO: Calcular recompensa sin reclamar (para previsualización)
     */
    suspend fun calcularRecompensaPrevia(tareaId: Int): RecompensaCalculada? {
        return try {
            val tarea = database.tareaDao().obtenerPorId(tareaId) ?: return null
            val subtareas = database.subtareaDao().obtenerPorTareaDirecto(tareaId)

            val habilidadActiva = _uiState.value.habilidadActiva
            val multiplicadorHabilidad = if (habilidadActiva != null && habilidadActiva.desbloqueada) {
                when (habilidadActiva.tipoAplicacion) {
                    "MENTORA", "ACTIVA" -> habilidadActiva.valorModificador
                    else -> 1.0f
                }
            } else 1.0f

            calcularRecompensaTotal(tarea, subtareas, multiplicadorHabilidad)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun completarTareaYReclamar(tarea: TareaEntity): ResultadoRecompensa {
        return try {
            // Obtener la tarea más actualizada desde la BD
            val tareaActual = database.tareaDao().obtenerPorId(tarea.id_tarea)

            if (tareaActual == null) {
                return ResultadoRecompensa.Error("Tarea no encontrada")
            }

            // Verificar si la tarea ya está finalizada
            if (tareaActual.estado == "FINALIZADO") {
                // Si ya está finalizada, verificar si ya se reclamó la recompensa
                if (tareaActual.recompensa_reclamada) {
                    return ResultadoRecompensa.Error("La recompensa de esta tarea ya fue reclamada anteriormente")
                }
                // Si está finalizada pero no reclamada, proceder a reclamar
            } else if (tareaActual.estado != "EN_PROGRESO") {
                return ResultadoRecompensa.Error("La tarea debe estar en progreso para completarse")
            } else {
                // Cambiar estado a FINALIZADO solo si no lo está
                database.tareaDao().cambiarEstado(tarea.id_tarea, "FINALIZADO")
            }

            // Obtener la tarea actualizada después del cambio de estado
            val tareaFinalizada = database.tareaDao().obtenerPorId(tarea.id_tarea)
                ?: return ResultadoRecompensa.Error("Error al actualizar tarea")

            // Verificar nuevamente si ya se reclamó la recompensa
            if (tareaFinalizada.recompensa_reclamada) {
                return ResultadoRecompensa.Error("La recompensa ya fue reclamada")
            }

            // Obtener subtareas
            val subtareas = database.subtareaDao().obtenerPorTareaDirecto(tarea.id_tarea)
            val subtareasCompletadas = subtareas.count { it.completada }
            val porcentajeSubtareas = if (subtareas.isNotEmpty()) {
                subtareasCompletadas.toFloat() / subtareas.size
            } else 1.0f

            // Obtener habilidad de la mascota
            val tablero = database.tableroDao().obtenerPorId(tableroId)
            var habilidadMascota: MascotaBaseEntity? = null
            var esMascotaActiva = false

            tablero?.let {
                val mascotaUsuario = database.mascotaUsuarioDao().obtenerPorId(it.id_mascota_mentora)
                mascotaUsuario?.let { mascota ->
                    habilidadMascota = database.mascotaBaseDao().obtenerPorId(mascota.id_mascota_base)
                    esMascotaActiva = true
                }
            }

            // Calcular recompensa usando RecompensaService
            val recompensaService = RecompensaService()
            val recompensa = recompensaService.calcularRecompensa(
                tarea = tareaFinalizada,
                habilidadMascota = habilidadMascota,
                esMascotaActiva = esMascotaActiva,
                porcentajeSubtareas = porcentajeSubtareas
            )

            // Obtener perfil
            val perfil = database.perfilUsuarioDao().obtenerPerfilDirecto()
            if (perfil == null) {
                return ResultadoRecompensa.Error("Perfil no encontrado")
            }

            // Aplicar recompensa al perfil
            val nuevasMonedas = perfil.monedas + recompensa.monedasFinal
            database.perfilUsuarioDao().actualizarMonedas(nuevasMonedas)
            database.perfilUsuarioDao().sumarExperiencia(recompensa.xpFinal)

            // Actualizar mascota mentora
            tablero?.let {
                val mascotaUsuario = database.mascotaUsuarioDao().obtenerPorId(it.id_mascota_mentora)
                mascotaUsuario?.let { mascota ->
                    database.mascotaUsuarioDao().sumarExperiencia(mascota.id_mascota_usuario, recompensa.xpFinal)
                    verificarSubidaNivelMascota(mascota)
                    val nuevaFelicidad = (mascota.felicidad_actual + 3).coerceIn(0, 100)
                    database.mascotaUsuarioDao().actualizarFelicidad(mascota.id_mascota_usuario, nuevaFelicidad)
                }
            }

            // Marcar recompensa como reclamada
            database.tareaDao().marcarRecompensaReclamada(tarea.id_tarea)

            // Actualizar estado local
            val tareaConRecompensa = tareaFinalizada.copy(recompensa_reclamada = true)
            _uiState.update { state ->
                state.copy(
                    tareasFinalizadas = state.tareasFinalizadas.map {
                        if (it.id_tarea == tarea.id_tarea) tareaConRecompensa else it
                    },
                    error = null
                )
            }

            // Crear objeto RecompensaCalculada para el diálogo
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

    private suspend fun calcularRecompensaTotal(
        tarea: TareaEntity,
        subtareas: List<SubtareaEntity>,
        multiplicadorHabilidad: Float
    ): RecompensaCalculada {

        val multiplicadorPrioridad = when (tarea.prioridad) {
            3 -> BONO_PRIORIDAD_ALTA
            2 -> BONO_PRIORIDAD_MEDIA
            else -> BONO_PRIORIDAD_BAJA
        }

        val multiplicadorEntrega = calcularMultiplicadorEntrega(tarea)

        val subtareasCompletadas = subtareas.count { it.completada }
        val porcentajeSubtareas = if (subtareas.isNotEmpty()) {
            subtareasCompletadas.toFloat() / subtareas.size
        } else 1.0f
        val multiplicadorSubtareas = 0.7f + (porcentajeSubtareas * 0.3f)

        val multiplicadorBase = multiplicadorPrioridad * multiplicadorEntrega * multiplicadorSubtareas
        val multiplicadorTotal = multiplicadorBase * multiplicadorHabilidad

        val xp = (XP_BASE_POR_TAREA * multiplicadorTotal).toInt().coerceAtLeast(1)
        val monedas = (MONEDAS_BASE_POR_TAREA * multiplicadorTotal).toInt().coerceAtLeast(1)

        val bonos = mutableListOf<String>()
        if (multiplicadorPrioridad > 1.0f) {
            bonos.add("Prioridad: +${((multiplicadorPrioridad - 1) * 100).toInt()}%")
        }
        if (multiplicadorEntrega > 1.0f) {
            bonos.add("Entrega anticipada: +${((multiplicadorEntrega - 1) * 100).toInt()}%")
        } else if (multiplicadorEntrega < 1.0f) {
            bonos.add("Entrega tardía: ${((1 - multiplicadorEntrega) * 100).toInt()}% penalización")
        }
        if (multiplicadorSubtareas > 0.8f && multiplicadorSubtareas < 1.0f) {
            bonos.add("Subtareas: +${((multiplicadorSubtareas - 0.7f) * 100).toInt()}%")
        }
        if (multiplicadorHabilidad > 1.0f) {
            val habilidad = _uiState.value.habilidadActiva
            bonos.add("Habilidad ${habilidad?.nombre ?: ""}: +${((multiplicadorHabilidad - 1) * 100).toInt()}%")
        } else if (multiplicadorHabilidad < 1.0f) {
            bonos.add("Penalización por felicidad baja: ${((1 - multiplicadorHabilidad) * 100).toInt()}%")
        }

        return RecompensaCalculada(
            xp = xp,
            monedas = monedas,
            multiplicador = multiplicadorTotal,
            bonosAplicados = bonos
        )
    }

    private fun calcularMultiplicadorEntrega(tarea: TareaEntity): Float {
        val fechaLimite = tarea.fecha_limite ?: return 1.0f
        val fechaCompletado = tarea.fecha_completado ?: System.currentTimeMillis()

        val diferenciaDias = TimeUnit.MILLISECONDS.toDays(fechaLimite - fechaCompletado)

        return when {
            diferenciaDias >= 1 -> BONO_ENTREGA_ANTICIPADA
            diferenciaDias == 0L -> BONO_ENTREGA_A_TIEMPO
            else -> BONO_ENTREGA_TARDE
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

    fun setError(mensaje: String) {
        _uiState.update { it.copy(error = mensaje) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}