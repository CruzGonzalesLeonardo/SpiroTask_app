package com.example.spire_task.feature.boards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import com.example.spire_task.data.local.entidades.MascotaUsuarioEntity
import com.example.spire_task.data.local.entidades.TableroEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MascotaCompleta(
    val mascotaUsuario: MascotaUsuarioEntity,
    val nombreEspecie: String,
    val emoji: String,
    val rutaAsset: String?
)

data class TableroConTareas(
    val tablero: TableroEntity,
    val totalTareas: Int = 0,
    val tareasCompletadas: Int = 0
)

data class BoardsListUiState(
    val tablerosConTareas: List<TableroConTareas> = emptyList(), // ← CAMBIADO
    val mascotasCompletas: List<MascotaCompleta> = emptyList(),
    val estaCargando: Boolean = true,
    val mostrarDialogoCrear: Boolean = false,
    val mostrarDialogoEliminar: Boolean = false,
    val tableroAEliminar: TableroEntity? = null,
    val error: String? = null
)

class BoardsListViewModel(
    private val database: SpiroDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardsListUiState())
    val uiState: StateFlow<BoardsListUiState> = _uiState.asStateFlow()

    init {
        cargarTablerosConTareas()
        cargarMascotasCompletas()
    }

    private fun cargarTablerosConTareas() {
        viewModelScope.launch {
            try {
                database.tableroDao().obtenerTodos().collect { tableros ->
                    val tablerosConTareas = tableros.map { tablero ->
                        val total = database.tareaDao().contarPorTablero(tablero.id_tablero)
                        val completadas = database.tareaDao().contarCompletadasPorTablero(tablero.id_tablero)
                        TableroConTareas(
                            tablero = tablero,
                            totalTareas = total,
                            tareasCompletadas = completadas
                        )
                    }
                    _uiState.update { it.copy(tablerosConTareas = tablerosConTareas, estaCargando = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(estaCargando = false, error = "Error al cargar tableros") }
            }
        }
    }

    private fun cargarMascotasCompletas() {
        viewModelScope.launch {
            try {
                database.mascotaUsuarioDao().obtenerTodas().collect { mascotas ->
                    val completas = mascotas.map { mascota ->
                        val especie = database.mascotaBaseDao().obtenerPorId(mascota.id_mascota_base)
                        MascotaCompleta(
                            mascotaUsuario = mascota,
                            nombreEspecie = especie?.nombre_especie ?: "Desconocida",
                            emoji = especie?.emoji ?: "🐾",
                            rutaAsset = especie?.ruta_asset_base
                        )
                    }
                    _uiState.update { it.copy(mascotasCompletas = completas) }
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }

    fun toggleDialogoCrear(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarDialogoCrear = mostrar) }
    }

    fun mostrarConfirmacionEliminar(tablero: TableroEntity) {
        _uiState.update { it.copy(mostrarDialogoEliminar = true, tableroAEliminar = tablero) }
    }

    fun ocultarDialogoEliminar() {
        _uiState.update { it.copy(mostrarDialogoEliminar = false, tableroAEliminar = null) }
    }

    fun crearTablero(nombre: String, colorHex: String, idMascotaMentora: Int) {
        viewModelScope.launch {
            try {
                val tablero = TableroEntity(
                    nombre = nombre.trim(),
                    color_hex = colorHex,
                    id_mascota_mentora = idMascotaMentora
                )
                database.tableroDao().insertar(tablero)
                _uiState.update { it.copy(mostrarDialogoCrear = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al crear tablero") }
            }
        }
    }

    fun eliminarTablero() {
        val tablero = _uiState.value.tableroAEliminar ?: return
        viewModelScope.launch {
            try {
                database.tableroDao().borrarLogicamente(tablero.id_tablero)
                _uiState.update { it.copy(mostrarDialogoEliminar = false, tableroAEliminar = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar tablero") }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    fun recargar() {
        viewModelScope.launch {
            try {
                val tableros = database.tableroDao().obtenerTodosDirecto()
                val tablerosConTareas = tableros.map { tablero ->
                    val total = database.tareaDao().contarPorTablero(tablero.id_tablero)
                    val completadas = database.tareaDao().contarCompletadasPorTablero(tablero.id_tablero)
                    TableroConTareas(tablero = tablero, totalTareas = total, tareasCompletadas = completadas)
                }
                _uiState.update { it.copy(tablerosConTareas = tablerosConTareas, estaCargando = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al recargar") }
            }
        }
    }
}