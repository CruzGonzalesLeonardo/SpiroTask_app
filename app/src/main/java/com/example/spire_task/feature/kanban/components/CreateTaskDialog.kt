package com.example.spire_task.feature.kanban.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spire_task.data.local.entities.ColumnEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
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
        "URGENTE_IMPORTANTE" to "🔥 Urgente e Importante",
        "NO_URGENTE_IMPORTANTE" to "📌 Importante, no Urgente",
        "URGENTE_NO_IMPORTANTE" to "⚡ Urgente, no Importante",
        "NO_URGENTE_NO_IMPORTANTE" to "📋 Normal"
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
                text = "➕ Nueva Tarea",
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
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
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

            Spacer(modifier = Modifier.height(8.dp))

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