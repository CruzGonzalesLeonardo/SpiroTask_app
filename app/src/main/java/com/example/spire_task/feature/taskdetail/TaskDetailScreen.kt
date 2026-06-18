package com.example.spire_task.feature.taskdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.data.local.entidades.SubtareaEntity
import com.example.spire_task.feature.kanban.GoldColor
import com.example.spire_task.feature.kanban.KanbanDoneColor
import java.text.SimpleDateFormat
import java.util.*

private val PriorityHighColor = Color(0xFFFF4757)
private val PriorityMediumColor = Color(0xFFFFA502)
private val PriorityLowColor = Color(0xFF2ED573)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    tareaId: Int,
    onVolver: () -> Unit = {},
    // ✅ Usar key = tareaId para forzar recreación del ViewModel
    viewModel: TaskDetailViewModel = viewModel(
        key = "task_detail_$tareaId",
        factory = TaskDetailViewModelFactory(tareaId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val recompensaPreview by viewModel.recompensaPreview.collectAsState()

    // ✅ Cargar la tarea cuando cambia el ID
    LaunchedEffect(tareaId) {
        viewModel.cargarTarea(tareaId)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Detalle de Tarea", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
            )

            if (uiState.estaCargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.tarea != null) {
                val tarea = uiState.tarea!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    // Título
                    item {
                        var titulo by remember(tarea.id_tarea) { mutableStateOf(tarea.titulo) }
                        var editandoTitulo by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it; editandoTitulo = true },
                            label = { Text("Título") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.actualizarTitulo(titulo)
                                    editandoTitulo = false
                                }
                            ),
                            trailingIcon = {
                                if (editandoTitulo) {
                                    IconButton(onClick = {
                                        viewModel.actualizarTitulo(titulo)
                                        editandoTitulo = false
                                    }) {
                                        Icon(Icons.Default.Save, "Guardar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                    }

                    // Descripción
                    item {
                        var descripcion by remember(tarea.id_tarea) { mutableStateOf(tarea.descripcion ?: "") }
                        var editandoDesc by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it; editandoDesc = true },
                            label = { Text("Descripción") },
                            minLines = 2,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.actualizarDescripcion(descripcion)
                                    editandoDesc = false
                                }
                            ),
                            trailingIcon = {
                                if (editandoDesc) {
                                    IconButton(onClick = {
                                        viewModel.actualizarDescripcion(descripcion)
                                        editandoDesc = false
                                    }) {
                                        Icon(Icons.Default.Save, "Guardar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                    }

                    // Previsualización de recompensa (solo si la tarea no está completada)
                    if (uiState.tarea?.estado != "FINALIZADO") {
                        item {
                            LaunchedEffect(uiState.tarea?.id_tarea, uiState.subtareas) {
                                viewModel.calcularRecompensaPreview()
                            }

                            if (recompensaPreview != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = GoldColor.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🏆", fontSize = 18.sp)
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Recompensa al completar",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldColor
                                            )
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("⭐ XP", style = MaterialTheme.typography.labelSmall)
                                                Text(
                                                    "+${recompensaPreview!!.xpFinal}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = KanbanDoneColor
                                                )
                                                if (recompensaPreview!!.xpBase != recompensaPreview!!.xpFinal) {
                                                    Text(
                                                        "base: +${recompensaPreview!!.xpBase}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Column {
                                                Text("🪙 Monedas", style = MaterialTheme.typography.labelSmall)
                                                Text(
                                                    "+${recompensaPreview!!.monedasFinal}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldColor
                                                )
                                                if (recompensaPreview!!.monedasBase != recompensaPreview!!.monedasFinal) {
                                                    Text(
                                                        "base: +${recompensaPreview!!.monedasBase}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            if (recompensaPreview!!.multiplicadorXp > 1.0f || recompensaPreview!!.multiplicadorMonedas > 1.0f) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = GoldColor.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        "x${String.format("%.1f", (recompensaPreview!!.multiplicadorXp + recompensaPreview!!.multiplicadorMonedas) / 2)}",
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldColor
                                                    )
                                                }
                                            }
                                        }

                                        if (recompensaPreview!!.bonosAplicados.isNotEmpty()) {
                                            Spacer(Modifier.height(8.dp))
                                            Divider(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                color = GoldColor.copy(alpha = 0.3f)
                                            )
                                            recompensaPreview!!.bonosAplicados.forEach { bono ->
                                                Text(
                                                    "✨ $bono",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = GoldColor.copy(alpha = 0.8f),
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Información
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                InfoColumn("📅", tarea.fecha_limite?.let { formatearFecha(it) } ?: "Sin fecha", "Vence")
                                val total = uiState.subtareas.size
                                val completadas = uiState.subtareas.count { it.completada }
                                InfoColumn("📝", "$completadas/$total", "Subtareas")
                                InfoColumn("🎯", when (tarea.prioridad) { 3 -> "Alta"; 2 -> "Media"; else -> "Baja" }, "Prioridad")
                            }
                        }
                    }

                    // Prioridad
                    item {
                        Text("Prioridad", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple(1, "Baja", PriorityLowColor),
                                Triple(2, "Media", PriorityMediumColor),
                                Triple(3, "Alta", PriorityHighColor)
                            ).forEach { (p, label, color) ->
                                FilterChip(
                                    selected = tarea.prioridad == p,
                                    onClick = { viewModel.actualizarPrioridad(p) },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color.copy(alpha = 0.2f),
                                        selectedLabelColor = color
                                    )
                                )
                            }
                        }
                    }

                    // Subtareas
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Subtareas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    "${uiState.subtareas.count { it.completada }}/${uiState.subtareas.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (uiState.subtareas.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    "Sin subtareas. Usa el campo de abajo para añadir.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    items(uiState.subtareas, key = { it.id_subtarea }) { subtarea ->
                        SubtareaItem(
                            subtarea = subtarea,
                            onToggle = { viewModel.toggleSubtarea(subtarea) },
                            onEliminar = { viewModel.eliminarSubtarea(subtarea) }
                        )
                    }

                    // Campo añadir subtarea
                    item {
                        var nuevaSubtarea by remember { mutableStateOf("") }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = nuevaSubtarea,
                                onValueChange = { nuevaSubtarea = it },
                                placeholder = { Text("Añadir subtarea...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (nuevaSubtarea.isNotBlank()) {
                                            viewModel.agregarSubtarea(nuevaSubtarea)
                                            nuevaSubtarea = ""
                                        }
                                    }
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = {
                                    if (nuevaSubtarea.isNotBlank()) {
                                        viewModel.agregarSubtarea(nuevaSubtarea)
                                        nuevaSubtarea = ""
                                    }
                                },
                                enabled = nuevaSubtarea.isNotBlank(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.size(48.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Send, "Añadir", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(emoji: String, valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(valor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SubtareaItem(
    subtarea: SubtareaEntity,
    onToggle: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (subtarea.completada)
                KanbanDoneColor.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = subtarea.completada,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = KanbanDoneColor
                )
            )
            Text(
                text = subtarea.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textDecoration = if (subtarea.completada) TextDecoration.LineThrough else TextDecoration.None,
                color = if (subtarea.completada) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, "Eliminar", Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            }
        }
    }
}

private fun formatearFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}