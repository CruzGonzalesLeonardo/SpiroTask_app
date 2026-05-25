package com.example.spire_task.feature.kanban.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.spire_task.data.local.entities.ColumnEntity
import com.example.spire_task.data.local.entities.TaskEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: TaskEntity,
    columns: List<ColumnEntity>,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var titulo by remember { mutableStateOf(task.title) }
    var descripcion by remember { mutableStateOf(task.description) }
    var columnaSeleccionada by remember { mutableStateOf(columns.find { it.id == task.columnId }) }
    var prioridadSeleccionada by remember { mutableStateOf(task.priority) }

    // ✅ Estado para fecha como texto
    var fechaTexto by remember {
        mutableStateOf(
            task.dueDate?.let {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.format(Date(it))
            } ?: ""
        )
    }

    var mostrarDropdownColumna by remember { mutableStateOf(false) }
    var mostrarDropdownPrioridad by remember { mutableStateOf(false) }
    var tituloVacio by remember { mutableStateOf(false) }
    var fechaInvalida by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val prioridades = listOf(
        "URGENTE_IMPORTANTE" to "🔥 Urgente e Importante",
        "NO_URGENTE_IMPORTANTE" to "📌 Importante, no Urgente",
        "URGENTE_NO_IMPORTANTE" to "⚡ Urgente, no Importante",
        "NO_URGENTE_NO_IMPORTANTE" to "📋 Normal"
    )

    // ✅ Validar y parsear fecha
    fun parseFecha(fechaStr: String): Long? {
        if (fechaStr.isBlank()) return null
        return try {
            dateFormatter.parse(fechaStr)?.time
        } catch (e: Exception) {
            null
        }
    }

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
                text = "✏️ Editar Tarea",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Campo: Título
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

            // Campo: Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            // Dropdown: Columna
            ExposedDropdownMenuBox(
                expanded = mostrarDropdownColumna,
                onExpandedChange = { mostrarDropdownColumna = it }
            ) {
                OutlinedTextField(
                    value = columnaSeleccionada?.name ?: "Seleccionar columna",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mover a columna") },
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
                    columns.forEach { columna ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(columna.name)
                                    Text(
                                        text = "Límite WIP: ${columna.wipLimit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                columnaSeleccionada = columna
                                mostrarDropdownColumna = false
                            }
                        )
                    }
                }
            }

            // Dropdown: Prioridad
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

            // ✅ Campo: Fecha de vencimiento (TextField simple)
            OutlinedTextField(
                value = fechaTexto,
                onValueChange = { nuevaFecha ->
                    fechaTexto = nuevaFecha
                    fechaInvalida = false
                    // Validar mientras escribe
                    if (nuevaFecha.isNotBlank() && !nuevaFecha.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                        fechaInvalida = true
                    } else if (nuevaFecha.isNotBlank()) {
                        // Validar que sea una fecha real
                        val fecha = parseFecha(nuevaFecha)
                        if (fecha == null) {
                            fechaInvalida = true
                        } else {
                            fechaInvalida = false
                        }
                    }
                },
                label = { Text("Fecha de vencimiento") },
                placeholder = { Text("Ej: 25/12/2024") },
                isError = fechaInvalida,
                supportingText = {
                    if (fechaInvalida) {
                        Text("Formato inválido. Use dd/MM/yyyy")
                    } else {
                        Text("Formato: día/mes/año (ej: 31/12/2024)")
                    }
                },
                trailingIcon = {
                    IconButton(onClick = {
                        // Opcional: Establecer fecha de hoy
                        val hoy = dateFormatter.format(Date())
                        fechaTexto = hoy
                        fechaInvalida = false
                    }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Usar fecha de hoy"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Información adicional (solo lectura)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📊 Información adicional",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "⭐ Favorito: ${if (task.isFavorite) "Sí" else "No"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🏆 XP: ${task.xpValue}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "📅 Creada: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(task.createdAt))}",
                        style = MaterialTheme.typography.bodySmall
                    )

                }

                // Botones
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
                                // Parsear fecha
                                val fechaLong = if (fechaTexto.isNotBlank() && !fechaInvalida) {
                                    parseFecha(fechaTexto)
                                } else {
                                    null
                                }

                                val updatedTask = task.copy(
                                    title = titulo,
                                    description = descripcion,
                                    columnId = columnaSeleccionada?.id ?: task.columnId,
                                    priority = prioridadSeleccionada,
                                    dueDate = fechaLong,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(updatedTask)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00ACC1)
                        )
                    ) {
                        Text("Guardar cambios")
                    }
                }
            }
        }
    }
}