package com.example.spire_task.feature.kanban.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.feature.kanban.models.ColumnWithTasks

@Composable
fun KanbanColumn(
    columnWithTasks: ColumnWithTasks,
    onToggleFavorite: (TaskEntity) -> Unit,
    onTaskLongPress: (TaskEntity) -> Unit  // ✅ NUEVO: Callback para presión larga
) {
    Log.d("KANBAN_DEBUG", "KanbanColumn: ${columnWithTasks.column.name}, tareas: ${columnWithTasks.tasks.size}")
    val totalTareas = columnWithTasks.tasks.size
    val wipLimit = columnWithTasks.column.wipLimit
    val excedeWip = totalTareas >= wipLimit

    Card(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(0.86f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Cabecera de la columna
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = columnWithTasks.column.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (excedeWip) Color(0xFFFFA000)
                    else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "$totalTareas/$wipLimit",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (excedeWip) Color.White
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Advertencia WIP
            if (excedeWip) {
                Text(
                    text = "⚠️ Límite WIP superado",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFA000),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de tareas
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = columnWithTasks.tasks,
                    key = { it.id }
                ) { tarea ->
                    TaskCard(
                        tarea = tarea,
                        onToggleFavorite = { onToggleFavorite(tarea) },
                        onLongPress = { onTaskLongPress(tarea) }  // ✅ Pasar callback
                    )
                }
            }
        }
    }
}