package com.example.spire_task.feature.kanban.main

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.feature.kanban.components.CreateTaskDialog
import com.example.spire_task.feature.kanban.components.EditTaskDialog
import com.example.spire_task.feature.kanban.components.KanbanColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanScreen(
    viewModel: KanbanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    // ✅ Estado para el diálogo de edición
    var tareaSeleccionada by remember { mutableStateOf<TaskEntity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(uiState.error ?: "Error", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.clearError() }) {
                        Text("Reintentar")
                    }
                }
            }

            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(
                        items = uiState.columns,
                        key = { it.column.id }
                    ) { columnWithTasks ->
                        KanbanColumn(
                            columnWithTasks = columnWithTasks,
                            onToggleFavorite = { tarea ->
                                viewModel.updateTask(tarea.copy(isFavorite = !tarea.isFavorite))
                            },
                            onTaskLongPress = { tarea ->
                                // ✅ Abrir diálogo de edición al presionar largamente
                                Log.d("KANBAN_DEBUG", "Long press en tarea: ${tarea.title}")
                                tareaSeleccionada = tarea
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarDialogoCrear = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF00ACC1)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Crear tarea", tint = Color.White)
        }
    }

    // Diálogo para crear tarea
    if (mostrarDialogoCrear) {
        CreateTaskDialog(
            columnas = uiState.columns.map { it.column },
            onDismiss = { mostrarDialogoCrear = false },
            onConfirm = { titulo, descripcion, columnId, prioridad, _ ->
                viewModel.createTask(
                    title = titulo,
                    description = descripcion,
                    columnId = columnId,
                    priority = prioridad,
                    dueDate = null
                )
                mostrarDialogoCrear = false
            }
        )
    }

    // ✅ Diálogo para editar tarea (presión larga)
    tareaSeleccionada?.let { tarea ->
        EditTaskDialog(
            task = tarea,
            columns = uiState.columns.map { it.column },
            onDismiss = { tareaSeleccionada = null },
            onSave = { tareaActualizada ->
                Log.d("KANBAN_DEBUG", "Guardando tarea actualizada: ${tareaActualizada.title}")
                viewModel.updateTask(tareaActualizada)
                tareaSeleccionada = null
            }
        )
    }
}