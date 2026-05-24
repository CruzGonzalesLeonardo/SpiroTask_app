package com.example.spire_task.feature.dashboard.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.data.local.entities.ColumnEntity
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.feature.dashboard.components.ColumnWithTasks
import com.example.spire_task.feature.dashboard.viewmodel.KanbanViewModel
import com.example.spire_task.feature.dashboard.viewmodel.KanbanViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanDashboardScreen(
    viewModel: KanbanViewModel = viewModel(factory = KanbanViewModelFactory.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarCrearTarea by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.clearError() }) {
                        Text("Cerrar error")
                    }
                }
            }

            uiState.columns.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay columnas aún",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.columns, key = { it.column.id }) { columnaConTareas ->
                        KanbanColumna(
                            columnaConTareas = columnaConTareas,
                            onToggleFavorite = { tarea ->
                                viewModel.updateTask(tarea.copy(isFavorite = !tarea.isFavorite))
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarCrearTarea = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF00ACC1)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear tarea",
                tint = Color.White
            )
        }
    }

    if (mostrarCrearTarea) {
        CrearTareaBottomSheet(
            columnas = uiState.columns.map { it.column },
            onDismiss = { mostrarCrearTarea = false },
            onConfirm = { titulo, descripcion, columnId, prioridad, fechaLimite ->
                viewModel.createTask(
                    title = titulo,
                    description = descripcion,
                    columnId = columnId,
                    priority = prioridad,
                    dueDate = fechaLimite
                )
                mostrarCrearTarea = false
            }
        )
    }
}

@Composable
fun KanbanColumna(
    columnaConTareas: ColumnWithTasks,
    onToggleFavorite: (TaskEntity) -> Unit
) {
    val totalTareas = columnaConTareas.tasks.size
    val wipLimit = columnaConTareas.column.wipLimit
    val excedeWip = totalTareas >= wipLimit

    Card(
        modifier = Modifier
            .width(230.dp)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = columnaConTareas.column.name,
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

            if (excedeWip) {
                Text(
                    text = "Límite WIP superado",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFA000),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(columnaConTareas.tasks, key = { it.id }) { tarea ->
                    TareaCard(
                        tarea = tarea,
                        onToggleFavorite = { onToggleFavorite(tarea) }
                    )
                }
            }
        }
    }
}

@Composable
fun TareaCard(
    tarea: TaskEntity,
    onToggleFavorite: () -> Unit
) {
    val colorPrioridad = when (tarea.priority) {
        "URGENTE_IMPORTANTE" -> Color(0xFFE53935)
        "NO_URGENTE_IMPORTANTE" -> Color(0xFFFB8C00)
        "URGENTE_NO_IMPORTANTE" -> Color(0xFFFFB300)
        "NO_URGENTE_NO_IMPORTANTE" -> Color(0xFF43A047)
        else -> Color(0xFF757575)
    }

    val diasRestantes = tarea.dueDate?.let {
        val diff = it - System.currentTimeMillis()
        (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    val colorFecha = when {
        diasRestantes == null -> Color.Transparent
        diasRestantes < 0 -> Color(0xFFE53935)
        diasRestantes <= 1 -> Color(0xFFFF7043)
        diasRestantes <= 3 -> Color(0xFFFFB300)
        else -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = tarea.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (tarea.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (tarea.isFavorite) Color(0xFFFFB300) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (tarea.description.isNotBlank()) {
                Text(
                    text = tarea.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorPrioridad.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (tarea.priority) {
                            "URGENTE_IMPORTANTE" -> "Urgente"
                            "NO_URGENTE_IMPORTANTE" -> "Importante"
                            "URGENTE_NO_IMPORTANTE" -> "Urgente"
                            "NO_URGENTE_NO_IMPORTANTE" -> "Normal"
                            else -> tarea.priority
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colorPrioridad,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                diasRestantes?.let {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorFecha.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when {
                                it < 0 -> "Vencida"
                                it == 0 -> "Hoy"
                                it == 1 -> "Mañana"
                                else -> "En $it días"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = colorFecha,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTareaBottomSheet(
    columnas: List<ColumnEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, Long?) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var columnaSeleccionada by remember { mutableStateOf(columnas.firstOrNull()) }
    var prioridadSeleccionada by remember { mutableStateOf("NO_URGENTE_IMPORTANTE") }
    var mostrarDropdownColumna by remember { mutableStateOf(false) }
    var mostrarDropdownPrioridad by remember { mutableStateOf(false) }
    var tituloVacio by remember { mutableStateOf(false) }

    val prioridades = listOf(
        "URGENTE_IMPORTANTE" to "Urgente e Importante",
        "NO_URGENTE_IMPORTANTE" to "Importante, no Urgente",
        "URGENTE_NO_IMPORTANTE" to "Urgente, no Importante",
        "NO_URGENTE_NO_IMPORTANTE" to "Normal"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nueva Tarea",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    titulo = it
                    tituloVacio = false
                },
                label = { Text("Título *") },
                isError = tituloVacio,
                supportingText = {
                    if (tituloVacio) Text("El título es obligatorio")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            ExposedDropdownMenuBox(
                expanded = mostrarDropdownColumna,
                onExpandedChange = { mostrarDropdownColumna = it }
            ) {
                OutlinedTextField(
                    value = columnaSeleccionada?.name ?: "Seleccionar",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Columna") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = mostrarDropdownColumna)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = mostrarDropdownColumna,
                    onDismissRequest = { mostrarDropdownColumna = false }
                ) {
                    columnas.forEach { columna ->
                        DropdownMenuItem(
                            text = { Text(columna.name) },
                            onClick = {
                                columnaSeleccionada = columna
                                mostrarDropdownColumna = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = mostrarDropdownPrioridad,
                onExpandedChange = { mostrarDropdownPrioridad = it }
            ) {
                OutlinedTextField(
                    value = prioridades.find { it.first == prioridadSeleccionada }?.second ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Prioridad") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = mostrarDropdownPrioridad)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = mostrarDropdownPrioridad,
                    onDismissRequest = { mostrarDropdownPrioridad = false }
                ) {
                    prioridades.forEach { (clave, etiqueta) ->
                        DropdownMenuItem(
                            text = { Text(etiqueta) },
                            onClick = {
                                prioridadSeleccionada = clave
                                mostrarDropdownPrioridad = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (titulo.isBlank()) {
                            tituloVacio = true
                        } else {
                            onConfirm(
                                titulo,
                                descripcion,
                                columnaSeleccionada?.id ?: 1,
                                prioridadSeleccionada,
                                null
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00ACC1)
                    )
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}