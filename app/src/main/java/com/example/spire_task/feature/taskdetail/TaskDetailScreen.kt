package com.example.spire_task.feature.taskdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    viewModel: TaskDetailViewModel = viewModel(
        key = "task_detail_$tareaId",
        factory = TaskDetailViewModelFactory(tareaId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val recompensaPreview by viewModel.recompensaPreview.collectAsState()

    LaunchedEffect(tareaId) {
        viewModel.cargarTarea(tareaId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de Tarea",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Rounded.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                )
            )

            if (uiState.estaCargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.tarea != null) {
                val tarea = uiState.tarea!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Título de la Tarea
                    item {
                        var titulo by remember(tarea.id_tarea) { mutableStateOf(tarea.titulo) }
                        var editandoTitulo by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it; editandoTitulo = true },
                            label = { Text("Título de la tarea") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (titulo.isNotBlank()) {
                                        viewModel.actualizarTitulo(titulo)
                                        editandoTitulo = false
                                    }
                                }
                            ),
                            trailingIcon = {
                                if (editandoTitulo) {
                                    IconButton(onClick = {
                                        if (titulo.isNotBlank()) {
                                            viewModel.actualizarTitulo(titulo)
                                            editandoTitulo = false
                                        }
                                    }) {
                                        Icon(Icons.Rounded.Check, "Guardar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                    }

                    // Descripción de la Tarea
                    item {
                        var descripcion by remember(tarea.id_tarea) { mutableStateOf(tarea.descripcion ?: "") }
                        var editandoDesc by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it; editandoDesc = true },
                            label = { Text("Notas / Descripción") },
                            minLines = 3,
                            maxLines = 6,
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
                                        Icon(Icons.Rounded.Check, "Guardar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                    }

                    // Fila informativa del Tablero (Vencimiento y Prioridad Actual)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InfoColumn(
                                    icon = Icons.Rounded.CalendarMonth,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    valor = tarea.fecha_limite?.let { formatearFecha(it) } ?: "Sin fecha",
                                    etiqueta = "Vence"
                                )
                                VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                InfoColumn(
                                    icon = Icons.Rounded.Flag,
                                    iconColor = when (tarea.prioridad) {
                                        3 -> PriorityHighColor
                                        2 -> PriorityMediumColor
                                        else -> PriorityLowColor
                                    },
                                    valor = when (tarea.prioridad) { 3 -> "Alta"; 2 -> "Media"; else -> "Baja" },
                                    etiqueta = "Prioridad"
                                )
                            }
                        }
                    }

                    // Selector de Cambios de Prioridad
                    item {
                        Text(
                            text = "Cambiar Prioridad",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                Triple(1, "Baja", PriorityLowColor),
                                Triple(2, "Media", PriorityMediumColor),
                                Triple(3, "Alta", PriorityHighColor)
                            ).forEach { (p, label, color) ->
                                val estaSeleccionado = tarea.prioridad == p
                                FilterChip(
                                    selected = estaSeleccionado,
                                    onClick = { viewModel.actualizarPrioridad(p) },
                                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color.copy(alpha = 0.15f),
                                        selectedLabelColor = color,
                                        selectedLeadingIconColor = color
                                    ),
                                    leadingIcon = {
                                        if (estaSeleccionado) {
                                            Icon(Icons.Rounded.RadioButtonChecked, contentDescription = null, modifier = Modifier.size(16.dp))
                                        } else {
                                            Icon(Icons.Rounded.RadioButtonUnchecked, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Previsualización de recompensa (Solo si está pendiente)
                    if (uiState.tarea?.estado != "FINALIZADO" && recompensaPreview != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.MilitaryTech,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Recompensas Estimadas",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Bloque de XP
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Star, "XP", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Column {
                                                Text(
                                                    text = "+${recompensaPreview!!.xpFinal} XP",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (recompensaPreview!!.xpBase != recompensaPreview!!.xpFinal) {
                                                    Text(
                                                        text = "base: +${recompensaPreview!!.xpBase}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        // Bloque de Monedas
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.MonetizationOn, "Monedas", tint = Color(0xFFFFA500), modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Column {
                                                Text(
                                                    text = "+${recompensaPreview!!.monedasFinal} Monedas",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (recompensaPreview!!.monedasBase != recompensaPreview!!.monedasFinal) {
                                                    Text(
                                                        text = "base: +${recompensaPreview!!.monedasBase}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        // Multiplicador Global
                                        if (recompensaPreview!!.multiplicadorXp > 1.0f || recompensaPreview!!.multiplicadorMonedas > 1.0f) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        text = "x${String.format("%.1f", (recompensaPreview!!.multiplicadorXp + recompensaPreview!!.multiplicadorMonedas) / 2)}",
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    // Desglose de Bonos activos
                                    if (recompensaPreview!!.bonosAplicados.isNotEmpty()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)
                                        )
                                        recompensaPreview!!.bonosAplicados.forEach { bono ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Rounded.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = bono,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(icon: ImageVector, iconColor: Color, valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatearFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}