package com.example.spire_task.feature.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spire_task.data.local.entidades.SubtareaEntity
import com.example.spire_task.data.local.entidades.TareaEntity

@Composable
fun TareaKanbanCard(
    tarea: TareaEntity,
    subtareas: List<SubtareaEntity>,
    onClick: () -> Unit,
    onAvanzar: () -> Unit,
    onRetroceder: (() -> Unit)?,
    onEliminar: () -> Unit
) {
    val colorPrioridad = tarea.prioridad.getColorPrioridad()
    val colorEstado = tarea.estado.getColorEstado()
    val completadas = subtareas.count { it.completada }
    val total = subtareas.size
    val diasRestantes = tarea.fecha_limite?.let { KanbanUtils.calcularDiasRestantes(it) }
    val progreso = if (total > 0) completadas.toFloat() / total else 0f

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (tarea.estado == "FINALIZADO") 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colorPrioridad))
                Spacer(Modifier.width(8.dp))
                Text(
                    tarea.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tarea.fecha_limite != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, Modifier.size(13.dp),
                            tint = if (diasRestantes != null && diasRestantes <= 1) PriorityHighColor else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            KanbanUtils.formatearFecha(tarea.fecha_limite),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (diasRestantes != null && diasRestantes <= 1) PriorityHighColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (diasRestantes != null && diasRestantes <= 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                if (total > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(13.dp),
                            tint = if (completadas == total) KanbanDoneColor else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(3.dp))
                        Text("$completadas/$total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (tarea.estado == "EN_PROGRESO" && total > 0) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = KanbanProgressColor,
                    trackColor = KanbanProgressColor.copy(alpha = 0.15f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (tarea.estado != "FINALIZADO") {
                    FilledTonalButton(
                        onClick = onAvanzar, modifier = Modifier.height(32.dp), shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = colorEstado.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            if (tarea.estado == "POR_HACER") "Iniciar" else "Completar",
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = colorEstado
                        )
                    }
                }

                if (onRetroceder != null) {
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onRetroceder, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Undo, "Retroceder", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }

                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Eliminar", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                }
            }
        }
    }
}