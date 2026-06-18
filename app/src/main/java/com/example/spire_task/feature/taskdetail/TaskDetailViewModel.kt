package com.example.spire_task.feature.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import com.example.spire_task.data.local.entidades.RecompensaTarea
import com.example.spire_task.data.local.entidades.SubtareaEntity
import com.example.spire_task.data.local.entidades.TareaEntity
import com.example.spire_task.data.repository.RecompensaService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val tarea: TareaEntity? = null,
    val subtareas: List<SubtareaEntity> = emptyList(),
    val estaCargando: Boolean = true,
    val error: String? = null
)

class TaskDetailViewModel(
    private val database: SpiroDatabase,
    private val tareaId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _recompensaPreview = MutableStateFlow<RecompensaTarea?>(null)
    val recompensaPreview: StateFlow<RecompensaTarea?> = _recompensaPreview.asStateFlow()

    fun cargarTarea(id: Int) {
        viewModelScope.launch {
            // Limpiar estado anterior antes de cargar la nueva tarea
            _uiState.update {
                TaskDetailUiState(estaCargando = true)
            }
            _recompensaPreview.update { null }

            cargarDatosCompletos()
        }
    }

    private fun cargarDatosCompletos() {
        viewModelScope.launch {
            try {
                val tarea = database.tareaDao().obtenerPorId(tareaId)
                val subtareas = if (tarea != null) {
                    database.subtareaDao().obtenerPorTareaDirecto(tarea.id_tarea)
                } else {
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        tarea = tarea,
                        subtareas = subtareas,
                        estaCargando = false
                    )
                }

                calcularRecompensaPreview()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        estaCargando = false,
                        error = "Error al cargar: ${e.message}"
                    )
                }
            }
        }
    }

    fun actualizarTitulo(nuevoTitulo: String) {
        viewModelScope.launch {
            try {
                val tarea = _uiState.value.tarea ?: return@launch
                database.tareaDao().actualizar(tarea.copy(titulo = nuevoTitulo, fecha_modificacion = System.currentTimeMillis()))
                _uiState.update { it.copy(tarea = it.tarea?.copy(titulo = nuevoTitulo)) }
                calcularRecompensaPreview()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al actualizar título") }
            }
        }
    }

    fun actualizarDescripcion(nuevaDescripcion: String) {
        viewModelScope.launch {
            try {
                val tarea = _uiState.value.tarea ?: return@launch
                database.tareaDao().actualizar(tarea.copy(descripcion = nuevaDescripcion.ifBlank { null }, fecha_modificacion = System.currentTimeMillis()))
                _uiState.update { it.copy(tarea = it.tarea?.copy(descripcion = nuevaDescripcion)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al actualizar descripción") }
            }
        }
    }

    fun actualizarPrioridad(nuevaPrioridad: Int) {
        viewModelScope.launch {
            try {
                val tarea = _uiState.value.tarea ?: return@launch
                database.tareaDao().actualizar(tarea.copy(prioridad = nuevaPrioridad, fecha_modificacion = System.currentTimeMillis()))
                _uiState.update { it.copy(tarea = it.tarea?.copy(prioridad = nuevaPrioridad)) }
                calcularRecompensaPreview()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al actualizar prioridad") }
            }
        }
    }

    fun agregarSubtarea(descripcion: String) {
        viewModelScope.launch {
            try {
                val tareaActual = _uiState.value.tarea ?: return@launch
                val nuevaSubtarea = SubtareaEntity(
                    id_tarea = tareaActual.id_tarea,
                    descripcion = descripcion.trim(),
                    orden = _uiState.value.subtareas.size
                )
                database.subtareaDao().insertar(nuevaSubtarea)
                val subtareasActualizadas = database.subtareaDao().obtenerPorTareaDirecto(tareaActual.id_tarea)
                _uiState.update { it.copy(subtareas = subtareasActualizadas) }
                calcularRecompensaPreview()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al crear subtarea: ${e.message}") }
            }
        }
    }

    fun toggleSubtarea(subtarea: SubtareaEntity) {
        viewModelScope.launch {
            try {
                database.subtareaDao().toggleCompletada(subtarea.id_subtarea, !subtarea.completada)
                val subtareasActualizadas = _uiState.value.subtareas.map {
                    if (it.id_subtarea == subtarea.id_subtarea) {
                        it.copy(completada = !it.completada)
                    } else it
                }
                _uiState.update { it.copy(subtareas = subtareasActualizadas) }
                calcularRecompensaPreview()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al actualizar subtarea") }
            }
        }
    }

    fun eliminarSubtarea(subtarea: SubtareaEntity) {
        viewModelScope.launch {
            try {
                database.subtareaDao().borrarLogicamente(subtarea.id_subtarea)
                val tareaActual = _uiState.value.tarea ?: return@launch
                val subtareasActualizadas = database.subtareaDao().obtenerPorTareaDirecto(tareaActual.id_tarea)
                _uiState.update { it.copy(subtareas = subtareasActualizadas) }
                calcularRecompensaPreview()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar subtarea") }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    fun calcularRecompensaPreview() {
        viewModelScope.launch {
            try {
                val tarea = _uiState.value.tarea ?: return@launch
                val subtareas = _uiState.value.subtareas

                // Si la tarea ya está completada, no mostrar preview
                if (tarea.estado == "FINALIZADO") {
                    _recompensaPreview.update { null }
                    return@launch
                }

                // Obtener el tablero y la mascota mentora
                val tablero = database.tableroDao().obtenerPorId(tarea.id_tablero)
                var habilidadMascota: MascotaBaseEntity? = null
                var esMascotaActiva = false

                tablero?.let {
                    val mascotaUsuario = database.mascotaUsuarioDao().obtenerPorId(it.id_mascota_mentora)
                    mascotaUsuario?.let { mascota ->
                        habilidadMascota = database.mascotaBaseDao().obtenerPorId(mascota.id_mascota_base)
                        esMascotaActiva = true
                    }
                }

                // Calcular porcentaje de subtareas completadas
                val subtareasCompletadas = subtareas.count { it.completada }
                val porcentajeSubtareas = if (subtareas.isNotEmpty()) {
                    subtareasCompletadas.toFloat() / subtareas.size
                } else 1.0f

                // Crear tarea preview (como si estuviera completada hoy)
                val tareaPreview = tarea.copy(
                    estado = "FINALIZADO",
                    fecha_completado = System.currentTimeMillis()
                )

                val recompensaService = RecompensaService()
                val recompensa = recompensaService.calcularRecompensa(
                    tarea = tareaPreview,
                    habilidadMascota = habilidadMascota,
                    esMascotaActiva = esMascotaActiva,
                    porcentajeSubtareas = porcentajeSubtareas
                )

                _recompensaPreview.update { recompensa }
            } catch (e: Exception) {
                _recompensaPreview.update { null }
            }
        }
    }
}