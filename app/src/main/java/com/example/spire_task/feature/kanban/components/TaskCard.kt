package com.example.spire_task.feature.kanban.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spire_task.data.local.entities.TaskEntity

@Composable
fun TaskCard(
    tarea: TaskEntity,
    onToggleFavorite: () -> Unit,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
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
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = { onLongPress?.invoke() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Fila superior: título + favorito
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
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (tarea.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (tarea.isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (tarea.isFavorite) Color(0xFFFFB300) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Descripción (si existe)
            if (tarea.description.isNotBlank()) {
                Text(
                    text = tarea.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2
                )
            }

            // Fila inferior: prioridad + fecha
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
                            "URGENTE_IMPORTANTE" -> "🔥 Urgente"
                            "NO_URGENTE_IMPORTANTE" -> "📌 Importante"
                            "URGENTE_NO_IMPORTANTE" -> "⚡ Urgente"
                            "NO_URGENTE_NO_IMPORTANTE" -> "📋 Normal"
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
                                it < 0 -> "⏰ Vencida"
                                it == 0 -> "📅 Hoy"
                                it == 1 -> "📅 Mañana"
                                else -> "📅 En $it días"
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