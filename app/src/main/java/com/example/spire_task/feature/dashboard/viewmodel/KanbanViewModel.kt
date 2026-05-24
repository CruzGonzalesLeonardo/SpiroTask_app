package com.example.spire_task.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.domain.repositories.ITaskRepository
import com.example.spire_task.feature.dashboard.components.ColumnWithTasks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KanbanUiState(
    val columns: List<ColumnWithTasks> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class KanbanViewModel(
    private val taskRepository: ITaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KanbanUiState())
    val uiState: StateFlow<KanbanUiState> = _uiState.asStateFlow()

    init {
        cargarTablero()
    }

    private fun cargarTablero() {
        viewModelScope.launch {
            try {
                taskRepository.getColumnsWithTasks().collect { columnas ->
                    _uiState.update {
                        it.copy(columns = columnas, isLoading = false, error = null)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
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
            val tarea = TaskEntity(
                title = title,
                description = description,
                columnId = columnId,
                priority = priority,
                dueDate = dueDate,
                isFavorite = false,
                xpValue = 10,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            taskRepository.insertTask(tarea)
        }
    }

    fun updateTask(tarea: TaskEntity) {
        viewModelScope.launch {
            taskRepository.updateTask(tarea)
        }
    }

    fun deleteTask(tarea: TaskEntity) {
        viewModelScope.launch {
            taskRepository.deleteTask(tarea)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}