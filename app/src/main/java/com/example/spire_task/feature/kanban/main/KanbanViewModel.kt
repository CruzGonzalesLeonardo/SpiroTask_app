package com.example.spire_task.feature.kanban.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.domain.repositories.ITaskRepository
import com.example.spire_task.feature.kanban.models.ColumnWithTasks
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KanbanUiState(
    val columns: List<ColumnWithTasks> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class KanbanViewModel(
    private val taskRepository: ITaskRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(KanbanUiState())
    val uiState: StateFlow<KanbanUiState> = _uiState.asStateFlow()

    init {
        Log.d("KANBAN_DEBUG", "🚀 ViewModel iniciado para userId: $userId")

        // ✅ Llamar a debugUserData dentro de viewModelScope.launch
        viewModelScope.launch {
            debugUserData()
            inicializarTablero()
        }
    }

    // ✅ Método de depuración
    private suspend fun debugUserData() {
        val columns = taskRepository.getAllColumns(userId).first()
        val tasks = taskRepository.getAllTasks(userId).first()

        Log.d("KANBAN_DEBUG", "=== DEBUG DATOS USUARIO: $userId ===")
        Log.d("KANBAN_DEBUG", "📊 Columnas encontradas: ${columns.size}")
        columns.forEach { col ->
            Log.d("KANBAN_DEBUG", "  - ${col.name} (id=${col.id}, userId=${col.userId})")
        }
        Log.d("KANBAN_DEBUG", "📋 Tareas encontradas: ${tasks.size}")
        tasks.forEach { task ->
            Log.d("KANBAN_DEBUG", "  - ${task.title} (columnaId=${task.columnId}, userId=${task.userId})")
        }

        // Verificar consistencia
        val columnIds = columns.map { it.id }.toSet()
        val orphanTasks = tasks.filter { it.columnId !in columnIds }
        if (orphanTasks.isNotEmpty()) {
            Log.e("KANBAN_DEBUG", "⚠️ Tareas huérfanas (columna no existe): ${orphanTasks.size}")
            orphanTasks.forEach { task ->
                Log.e("KANBAN_DEBUG", "  - Tarea '${task.title}' busca columna ${task.columnId}")
            }
        }
        Log.d("KANBAN_DEBUG", "=====================================")
    }

    private fun inicializarTablero() {
        viewModelScope.launch {
            try {
                Log.d("KANBAN_DEBUG", "1️⃣ Insertando columnas por defecto...")
                taskRepository.insertDefaultColumns(userId)

                Log.d("KANBAN_DEBUG", "2️⃣ Cargando tablero...")
                cargarTablero()
            } catch (e: Exception) {
                Log.e("KANBAN_DEBUG", "❌ Error en inicialización: ${e.message}", e)
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    private suspend fun cargarTablero() {
        try {
            Log.d("KANBAN_DEBUG", "3️⃣ Suscribiéndose a flujo de datos...")
            taskRepository.getColumnsWithTasks(userId).collect { columnas ->
                Log.d("KANBAN_DEBUG", "4️⃣ Datos actualizados recibidos!")
                Log.d("KANBAN_DEBUG", "   Total columnas con tareas: ${columnas.size}")

                columnas.forEach { colWithTasks ->
                    Log.d("KANBAN_DEBUG", "   📌 ${colWithTasks.column.name}: ${colWithTasks.tasks.size} tareas")
                    colWithTasks.tasks.forEach { task ->
                        Log.d("KANBAN_DEBUG", "      - ${task.title} (id=${task.id})")
                    }
                }

                _uiState.update {
                    it.copy(columns = columnas, isLoading = false, error = null)
                }
            }
        } catch (e: Exception) {
            Log.e("KANBAN_DEBUG", "❌ Error al cargar tablero: ${e.message}", e)
            _uiState.update {
                it.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun createTask(
        title: String,
        description: String,
        columnId: Int,
        priority: String,
        dueDate: Long?
    ) {
        viewModelScope.launch {
            try {
                Log.d("KANBAN_DEBUG", "➕ Creando nueva tarea:")
                Log.d("KANBAN_DEBUG", "   - Título: $title")
                Log.d("KANBAN_DEBUG", "   - Columna ID: $columnId")
                Log.d("KANBAN_DEBUG", "   - Usuario: $userId")

                // Verificar que la columna existe
                val columns = taskRepository.getAllColumns(userId).first()
                val columnExists = columns.any { it.id == columnId }
                Log.d("KANBAN_DEBUG", "   - ¿Columna existe? $columnExists")

                if (!columnExists) {
                    Log.e("KANBAN_DEBUG", "⚠️ La columna $columnId no existe para el usuario $userId")
                    Log.d("KANBAN_DEBUG", "   Columnas disponibles: ${columns.map { "${it.name}(${it.id})" }}")
                    _uiState.update {
                        it.copy(error = "La columna seleccionada no existe")
                    }
                    return@launch
                }

                val tarea = TaskEntity(
                    title = title,
                    description = description,
                    columnId = columnId,
                    priority = priority,
                    dueDate = dueDate,
                    isFavorite = false,
                    xpValue = 10,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    userId = userId
                )

                taskRepository.insertTask(tarea)
                Log.d("KANBAN_DEBUG", "✅ Tarea creada exitosamente")

                // Verificar que se insertó
                delay(500)
                val tasks = taskRepository.getAllTasks(userId).first()
                Log.d("KANBAN_DEBUG", "📊 Total tareas después de insertar: ${tasks.size}")

            } catch (e: Exception) {
                Log.e("KANBAN_DEBUG", "❌ Error al crear tarea: ${e.message}", e)
                _uiState.update {
                    it.copy(error = "Error al crear tarea: ${e.message}")
                }
            }
        }
    }

    fun updateTask(tarea: TaskEntity) {
        viewModelScope.launch {
            Log.d("KANBAN_DEBUG", "✏️ Actualizando tarea: ${tarea.title}")
            taskRepository.updateTask(tarea)
        }
    }

    fun deleteTask(tarea: TaskEntity) {
        viewModelScope.launch {
            Log.d("KANBAN_DEBUG", "🗑️ Eliminando tarea: ${tarea.title}")
            taskRepository.deleteTask(tarea)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}