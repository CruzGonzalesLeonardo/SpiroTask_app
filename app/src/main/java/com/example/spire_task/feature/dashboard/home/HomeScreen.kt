package com.example.spire_task.feature.dashboard.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.feature.kanban.main.KanbanViewModel
import com.example.spire_task.feature.kanban.main.KanbanViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    userName: String,
    userId: String,  // ← NUEVO: Recibir userId
    level: Int = 1,
    xp: Float = 0f,
    xpNeeded: Int = 100,
    monedas: Int = 0,
    racha: Int = 0,
    onNavigateToKanban: () -> Unit = {},  // ← NUEVO: Navegar a Kanban
    onCreateTaskClick: () -> Unit = {}    // ← NUEVO: Crear tarea rápida
) {
    // ✅ Obtener el ViewModel de Kanban con el userId actual
    val kanbanViewModel: KanbanViewModel = viewModel(
        key = userId,
        factory = KanbanViewModelFactory.createFactory(userId)
    )

    val uiState by kanbanViewModel.uiState.collectAsState()

    // ✅ Obtener todas las tareas del usuario (de todas las columnas)
    val todasLasTareas = uiState.columns.flatMap { it.tasks }

    // ✅ Tareas pendientes (excluyendo "Completado")
    val tareasPendientes = todasLasTareas.filter {
        it.columnId != obtenerColumnaCompletada(uiState.columns)?.id
    }

    // ✅ Tareas próximas a vencer (próximos 3 días)
    val hoy = System.currentTimeMillis()
    val tresDias = 3 * 24 * 60 * 60 * 1000L
    val tareasPorVencer = tareasPendientes.filter { task ->
        task.dueDate != null &&
                task.dueDate in hoy..(hoy + tresDias)
    }.sortedBy { it.dueDate }

    // ✅ Tareas vencidas
    val tareasVencidas = tareasPendientes.filter { task ->
        task.dueDate != null && task.dueDate < hoy
    }.sortedByDescending { it.dueDate }

    // ✅ Tareas de alta prioridad
    val tareasUrgentes = tareasPendientes.filter {
        it.priority == "URGENTE_IMPORTANTE"
    }

    // ✅ Estadísticas rápidas
    val totalTareas = todasLasTareas.size
    val tareasCompletadas = todasLasTareas.size - tareasPendientes.size
    val porcentajeCompletado = if (totalTareas > 0) {
        (tareasCompletadas.toFloat() / totalTareas) * 100
    } else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de bienvenida
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "¡Bienvenido, $userName!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = when {
                            tareasVencidas.isNotEmpty() -> "⚠️ Tienes ${tareasVencidas.size} tarea(s) vencida(s)"
                            tareasPorVencer.isNotEmpty() -> "📅 ${tareasPorVencer.size} tarea(s) por vencer"
                            tareasPendientes.isEmpty() -> "🎉 ¡No tienes tareas pendientes!"
                            else -> "Tienes ${tareasPendientes.size} tarea(s) por hacer"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Tarjeta de estadísticas rápidas
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 Tu progreso", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Nivel y monedas
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐ Nivel $level")
                        Spacer(modifier = Modifier.weight(1f))
                        Text("🪙 $monedas monedas")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Barra de XP
                    LinearProgressIndicator(
                        progress = (xp / xpNeeded).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "XP: ${xp.toInt()}/$xpNeeded",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Estadísticas de tareas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📋 Total: $totalTareas", style = MaterialTheme.typography.bodySmall)
                        Text("✅ Completadas: $tareasCompletadas", style = MaterialTheme.typography.bodySmall)
                        Text("⏳ Pendientes: ${tareasPendientes.size}", style = MaterialTheme.typography.bodySmall)
                    }

                    if (totalTareas > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = porcentajeCompletado / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Completado: ${porcentajeCompletado.toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Racha
                    Text("🔥 Racha actual: $racha días")
                }
            }
        }

        // ⚠️ Tareas vencidas (si las hay)
        if (tareasVencidas.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚠️ Tareas Vencidas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        tareasVencidas.take(3).forEach { task ->
                            TareaPendienteItem(
                                task = task,
                                showDueDate = true
                            )
                        }

                        if (tareasVencidas.size > 3) {
                            TextButton(
                                onClick = onNavigateToKanban,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Ver ${tareasVencidas.size - 3} más →")
                            }
                        }
                    }
                }
            }
        }

        // 🔥 Tareas urgentes (si las hay)
        if (tareasUrgentes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔥 Urgentes e Importantes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        tareasUrgentes.take(3).forEach { task ->
                            TareaPendienteItem(
                                task = task,
                                showPriority = false
                            )
                        }

                        if (tareasUrgentes.size > 3) {
                            TextButton(
                                onClick = onNavigateToKanban,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Ver ${tareasUrgentes.size - 3} más →")
                            }
                        }
                    }
                }
            }
        }

        // 📅 Próximas a vencer
        if (tareasPorVencer.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📅 Próximas a vencer",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Vencen en los próximos 3 días",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        tareasPorVencer.take(3).forEach { task ->
                            TareaPendienteItem(
                                task = task,
                                showDueDate = true
                            )
                        }

                        if (tareasPorVencer.size > 3) {
                            TextButton(
                                onClick = onNavigateToKanban,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Ver todas →")
                            }
                        }
                    }
                }
            }
        }

        // 📋 Lista de tareas pendientes
        if (tareasPendientes.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 Tus tareas (${tareasPendientes.size})",
                                style = MaterialTheme.typography.titleMedium
                            )

                            IconButton(onClick = onCreateTaskClick) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Crear tarea rápida"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        tareasPendientes.take(5).forEach { task ->
                            TareaPendienteItem(
                                task = task,
                                showDueDate = true,
                                showPriority = true
                            )
                        }

                        if (tareasPendientes.size > 5) {
                            TextButton(
                                onClick = onNavigateToKanban,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Ver ${tareasPendientes.size - 5} más →")
                            }
                        }
                    }
                }
            }
        } else if (totalTareas == 0) {
            // Estado vacío
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📋 No tienes tareas aún", style = MaterialTheme.typography.titleMedium)
                        Text("¡Crea tu primera tarea para empezar!")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onCreateTaskClick) {
                            Text("+ Crear primera tarea")
                        }
                    }
                }
            }
        }

        // 🐱 Tarjeta de mascota
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🐱 Tu mascota", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = when {
                                tareasPendientes.isEmpty() -> "¡Está muy feliz! 🎉"
                                tareasVencidas.isNotEmpty() -> "Está preocupada por las tareas vencidas 😟"
                                tareasPorVencer.isNotEmpty() -> "Te recuerda las tareas por vencer ⏰"
                                else -> "¡Está contenta con tu progreso! ✨"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(onClick = { /* Navegar a Mascota */ }) {
                        Text("Cuidar →")
                    }
                }
            }
        }
    }
}

@Composable
fun TareaPendienteItem(
    task: TaskEntity,
    showDueDate: Boolean = true,
    showPriority: Boolean = false
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val hoy = System.currentTimeMillis()
    val estaVencida = task.dueDate != null && task.dueDate < hoy

    val priorityEmoji = when (task.priority) {
        "URGENTE_IMPORTANTE" -> "🔥"
        "NO_URGENTE_IMPORTANTE" -> "📌"
        "URGENTE_NO_IMPORTANTE" -> "⚡"
        "NO_URGENTE_NO_IMPORTANTE" -> "📋"
        else -> "📝"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "☐ ", style = MaterialTheme.typography.bodyLarge)

            if (showPriority) {
                Text(text = priorityEmoji, style = MaterialTheme.typography.bodyLarge)
            }

            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (estaVencida) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description.take(50),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        if (showDueDate && task.dueDate != null) {
            Text(
                text = if (estaVencida) "⚠️ Vencida" else "📅 ${dateFormatter.format(Date(task.dueDate))}",
                style = MaterialTheme.typography.bodySmall,
                color = if (estaVencida) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Función auxiliar para obtener la columna "Completado"
fun obtenerColumnaCompletada(columns: List<com.example.spire_task.feature.kanban.models.ColumnWithTasks>): com.example.spire_task.data.local.entities.ColumnEntity? {
    return columns.find {
        it.column.name.equals("Completado", ignoreCase = true) ||
                it.column.name.equals("Completadas", ignoreCase = true) ||
                it.column.name.equals("Done", ignoreCase = true)
    }?.column
}