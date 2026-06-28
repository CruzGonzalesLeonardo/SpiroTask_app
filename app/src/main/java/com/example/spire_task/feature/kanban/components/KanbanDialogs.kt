package com.example.spire_task.feature.kanban.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spire_task.data.local.entidades.TareaEntity
import com.example.spire_task.feature.kanban.KanbanUtils
import com.example.spire_task.feature.kanban.MascotaCompletaKanban
import com.example.spire_task.feature.kanban.RecompensaMostrada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearTarea(
    onCrear: (titulo: String, prioridad: Int, fechaLimite: Long) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf(1) }

    var fechaSeleccionadaTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var textoFecha by remember { mutableStateOf("Seleccionar Día de Entrega") }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fechaSeleccionadaTimestamp)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        fechaSeleccionadaTimestamp = it
                        textoFecha = "Entrega: ${KanbanUtils.formatearFechaCompleta(it)}"
                    }
                    mostrarDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nueva Tarea Kanban", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("¿Qué vas a lograr hoy?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column {
                    Text("Prioridad Estratégica", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3).forEach { nivel ->
                            val texto = when(nivel) {
                                3 -> "Alta"
                                2 -> "Media"
                                else -> "Baja"
                            }
                            FilterChip(
                                selected = prioridad == nivel,
                                onClick = { prioridad = nivel },
                                label = { Text(texto) }
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { mostrarDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(textoFecha)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        onCrear(titulo, prioridad, fechaSeleccionadaTimestamp)
                    }
                },
                enabled = titulo.isNotBlank()
            ) { Text("Guardar Tarea") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun DialogoConfirmarAvanzar(
    tarea: TareaEntity,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val mensaje = if (tarea.estado == "EN_PROGRESO") {
        "¿Deseas finalizar esta tarea y reclamar tus recompensas en Spiro Task?"
    } else {
        "¿Mover esta tarea a 'En Progreso'?"
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Avanzar Tarea") },
        text = { Text(mensaje) },
        confirmButton = { Button(onClick = onConfirmar) { Text("Avanzar") } },
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
        title = { Text("Regresar Estado") },
        text = { Text("¿Estás seguro de que deseas regresar la tarea '${tarea.titulo}' al estado anterior?") },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Regresar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun DialogoRecompensa(
    recompensa: RecompensaMostrada,
    mascota: MascotaCompletaKanban?,
    onCerrar: () -> Unit
) {
    val context = LocalContext.current
    val limiteAlcanzado = recompensa.xp == 0 && recompensa.monedas == 0

    val assetNombre = if (!mascota?.rutaAssetLike.isNullOrBlank()) mascota?.rutaAssetLike!! else "like.png"
    val rutaCompleta = "file:///android_asset/$assetNombre"

    val archivoExiste = remember(assetNombre) {
        if (assetNombre.isBlank()) false else {
            try {
                val inputStream = context.assets.open(assetNombre)
                inputStream.close()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (limiteAlcanzado) Icons.Rounded.Warning else Icons.Rounded.Star,
                    contentDescription = null,
                    tint = if (limiteAlcanzado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (limiteAlcanzado) "Límite Diario Alcanzado" else "¡Misión Cumplida!",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (limiteAlcanzado) {
                        "Has completado la tarea con éxito, pero tu inventario diario está lleno. Vuelve mañana para seguir sumando."
                    } else {
                        "Tu mentor está orgulloso. Recompensas añadidas a tu inventario:"
                    },
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                // 🟢 MASCOTA CENTRADA
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            if (limiteAlcanzado) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (archivoExiste) {
                        AsyncImage(
                            model = rutaCompleta,
                            contentDescription = mascota?.nombreEspecie ?: "Mascota Mentora",
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = mascota?.emoji ?: "🐾",
                            fontSize = 44.sp
                        )
                    }
                }

                // CONTENEDOR DE RECOMPENSAS / ADVERTENCIA
                Surface(
                    color = if (limiteAlcanzado) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (limiteAlcanzado) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text(
                                    text = "0 / 6 Cupos Disponibles Hoy",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.StarRate, contentDescription = "Experiencia", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Text(text = "+${recompensa.xp} Experiencia", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.MonetizationOn, contentDescription = "Monedas", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Text(text = "+${recompensa.monedas} Monedas Oro", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }

                if (recompensa.bonos.isNotEmpty()) {
                    Text("Detalles del Servidor:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    recompensa.bonos.forEach { bono ->
                        Text(text = bono, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Start))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCerrar,
                colors = if (limiteAlcanzado) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
            ) {
                Text(if (limiteAlcanzado) "Entendido" else "Reclamar")
            }
        }
    )
}