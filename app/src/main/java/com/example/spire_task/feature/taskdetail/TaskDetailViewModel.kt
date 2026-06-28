package com.example.spire_task.feature.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import com.example.spire_task.data.local.entidades.RecompensaTarea
import com.example.spire_task.data.local.entidades.TareaEntity
import com.example.spire_task.data.repository.RecompensaService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val tarea: TareaEntity? = null,
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
            _uiState.update { TaskDetailUiState(estaCargando = true) }
            _recompensaPreview.update { null }
            cargarDatosCompletos()
        }
    }

    private fun cargarDatosCompletos() {
        viewModelScope.launch {
            try {
                val tarea = database.tareaDao().obtenerPorId(tareaId)
                _uiState.update {
                    it.copy(tarea = tarea, estaCargando = false)
                }
                calcularRecompensaPreview()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(estaCargando = false, error = "Error al cargar: ${e.message}")
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

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    fun calcularRecompensaPreview() {
        viewModelScope.launch {
            try {
                val tarea = _uiState.value.tarea ?: return@launch

                if (tarea.estado == "FINALIZADO") {
                    _recompensaPreview.update { null }
                    return@launch
                }

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

                val tareaPreview = tarea.copy(
                    estado = "FINALIZADO",
                    fecha_completado = System.currentTimeMillis()
                )

                val recompensaService = RecompensaService(context = com.example.spire_task.SpiroTaskApplication.instance)
                val recompensa = recompensaService.calcularRecompensa(
                    tarea = tareaPreview,
                    habilidadMascota = habilidadMascota,
                    esMascotaActiva = esMascotaActiva,
                )

                _recompensaPreview.update { recompensa }
            } catch (e: Exception) {
                _recompensaPreview.update { null }
            }
        }
    }
}