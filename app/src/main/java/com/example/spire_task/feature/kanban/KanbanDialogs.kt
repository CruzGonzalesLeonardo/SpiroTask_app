package com.example.spire_task.feature.kanban

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spire_task.data.local.entidades.TareaEntity
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearTarea(
    onCrear: (String, Int, Long) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var prioridad by remember { mutableIntStateOf(2) }
    var fechaLimite by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val fechaTexto = KanbanUtils.formatearFechaCompleta(fechaLimite)

    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(24.dp),
        title = { Text("✨ Nueva Tarea", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo, onValueChange = { titulo = it; error = null },
                    label = { Text("Título") }, placeholder = { Text("¿Qué necesitas hacer?") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp), isError = error != null,
                    supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )

                Spacer(Modifier.height(14.dp))

                Text("📅 Fecha límite", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { mostrarDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(fechaTexto)
                }

                Spacer(Modifier.height(14.dp))

                Text("🎯 Prioridad", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(Triple(1, "Baja", PriorityLowColor), Triple(2, "Media", PriorityMediumColor), Triple(3, "Alta", PriorityHighColor)).forEach { (p, l, c) ->
                        FilterChip(selected = prioridad == p, onClick = { prioridad = p }, label = { Text(l, style = MaterialTheme.typography.labelSmall) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = c.copy(alpha = 0.2f), selectedLabelColor = c))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { when { titulo.isBlank() -> error = "Ingresa un título"; else -> onCrear(titulo, prioridad, fechaLimite) } }, shape = RoundedCornerShape(14.dp)) { Text("✨ Crear tarea", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fechaLimite)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { fecha ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = fecha
                        cal.set(Calendar.HOUR_OF_DAY, 23)
                        cal.set(Calendar.MINUTE, 59)
                        cal.set(Calendar.SECOND, 59)
                        cal.set(Calendar.MILLISECOND, 999)
                        fechaLimite = cal.timeInMillis
                    }
                    mostrarDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun DialogoConfirmarAvanzar(
    tarea: TareaEntity,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val estadoActual = when (tarea.estado) {
        "POR_HACER" -> "Por Hacer"
        "EN_PROGRESO" -> "En Progreso"
        else -> ""
    }
    val estadoDestino = when (tarea.estado) {
        "POR_HACER" -> "En Progreso"
        "EN_PROGRESO" -> "Finalizado"
        else -> ""
    }
    val icono = if (tarea.estado == "POR_HACER") Icons.Default.PlayArrow else Icons.Default.CheckCircle
    val colorIcono = if (tarea.estado == "POR_HACER") KanbanProgressColor else KanbanDoneColor

    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(24.dp),
        icon = { Icon(icono, null, tint = colorIcono, modifier = Modifier.size(48.dp)) },
        title = { Text(if (tarea.estado == "POR_HACER") "¿Iniciar tarea?" else "¿Completar tarea?", fontWeight = FontWeight.Bold) },
        text = { Text("¿Mover \"${tarea.titulo}\" de \"$estadoActual\" a \"$estadoDestino\"?") },
        confirmButton = {
            Button(onClick = onConfirmar, shape = RoundedCornerShape(14.dp)) {
                Text(if (tarea.estado == "POR_HACER") "Sí, iniciar" else "Sí, completar")
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun DialogoConfirmarRetroceder(
    tarea: TareaEntity,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(24.dp),
        icon = { Icon(Icons.Default.Undo, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) },
        title = { Text(if (tarea.estado == "FINALIZADO") "¿Reabrir tarea?" else "¿Regresar tarea?", fontWeight = FontWeight.Bold) },
        text = { Text("¿Mover \"${tarea.titulo}\" a ${if (tarea.estado == "FINALIZADO") "En Progreso" else "Por Hacer"}?") },
        confirmButton = {
            Button(onClick = onConfirmar, shape = RoundedCornerShape(14.dp)) { Text("Sí, regresar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun DialogoEliminarTarea(
    tarea: TareaEntity,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(24.dp),
        icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)) },
        title = { Text("¿Eliminar tarea?", fontWeight = FontWeight.Bold) },
        text = { Text("¿Estás seguro de eliminar \"${tarea.titulo}\"?\nEsta acción no se puede deshacer.") },
        confirmButton = {
            Button(onClick = onConfirmar, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(14.dp)) { Text("Eliminar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun DialogoRecompensa(
    recompensa: RecompensaMostrada,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                color = GoldColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.AutoAwesome,  // o Icons.Default.Celebration
                        contentDescription = "Celebración",
                        modifier = Modifier.size(48.dp),  // 48.dp ≈ 40.sp
                        tint = GoldColor
                    )
                }
            }
        },
        title = { Text("¡Tarea Completada!", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("¡Has recibido tu recompensa!", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🏆 Recompensa obtenida:", fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("⭐ XP ganada:")
                            Text("+${recompensa.xp}", fontWeight = FontWeight.Bold, color = KanbanDoneColor)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🪙 Monedas ganadas:")
                            Text("+${recompensa.monedas}", fontWeight = FontWeight.Bold, color = GoldColor)
                        }
                        if (recompensa.bonos.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            recompensa.bonos.forEach { bono ->
                                Text("✨ $bono", style = MaterialTheme.typography.labelSmall, color = GoldColor)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onCerrar, shape = RoundedCornerShape(16.dp)) {
                Text("¡Genial!", fontWeight = FontWeight.Bold)
            }
        }
    )
}