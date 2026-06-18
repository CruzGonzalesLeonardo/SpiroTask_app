package com.example.spire_task.feature.kanban

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

@Composable
fun KanbanHeader(
    tableroNombre: String,
    onVolver: () -> Unit,
    mascota: MascotaCompletaKanban?,
    habilidad: HabilidadCompleta?,
    felicidad: Int
) {
    val colorFelicidad = when {
        felicidad >= 70 -> Color(0xFF4CAF50)
        felicidad >= 40 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                if (mascota != null) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (mascota.rutaAsset != null) {
                                SubcomposeAsyncImage(
                                    model = "file:///android_asset/${mascota.rutaAsset}",
                                    contentDescription = mascota.nombreEspecie,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    error = { Text(mascota.emoji, fontSize = 28.sp) }
                                )
                            } else Text(mascota.emoji, fontSize = 28.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tableroNombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (mascota != null) {
                        Text(
                            "Mentor: ${mascota.nombreEspecie} • Nivel ${mascota.nivel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                if (mascota != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "Felicidad",
                                modifier = Modifier.size(14.dp),
                                tint = colorFelicidad
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "$felicidad%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorFelicidad
                            )
                        }
                        LinearProgressIndicator(
                            progress = { felicidad / 100f },
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = colorFelicidad,
                            trackColor = colorFelicidad.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            if (habilidad != null && habilidad.desbloqueada) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = KanbanDoneColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Habilidad especial",
                            modifier = Modifier.size(14.dp),
                            tint = KanbanDoneColor
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${habilidad.nombre}: ${habilidad.descripcion}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = KanbanDoneColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanFilterChips(
    filtroActivo: String,
    onFiltroChange: (String) -> Unit,
    conteos: Map<String, Int>
) {
    val estados = listOf(
        Triple("POR_HACER", "Por hacer", KanbanTodoColor),
        Triple("EN_PROGRESO", "En progreso", KanbanProgressColor),
        Triple("FINALIZADO", "Finalizado", KanbanDoneColor)
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        estados.forEach { (estado, label, color) ->
            val sel = filtroActivo == estado
            val count = conteos[estado] ?: 0
            FilterChip(
                selected = sel,
                onClick = { onFiltroChange(estado) },
                label = {
                    Text("$label ($count)", style = MaterialTheme.typography.labelMedium, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(alpha = 0.2f), selectedLabelColor = color),
                border = FilterChipDefaults.filterChipBorder(borderColor = if (sel) color else MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = color, enabled = true, selected = sel)
            )
        }
    }
}

@Composable
fun LimitesBar(
    limitePorHacer: Int, actualPorHacer: Int,
    limiteEnProgreso: Int, actualEnProgreso: Int
) {
    val porHacerLleno = actualPorHacer >= limitePorHacer
    val progresoLleno = actualEnProgreso >= limiteEnProgreso

    if (porHacerLleno || progresoLleno) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "📋 Por hacer: $actualPorHacer/$limitePorHacer",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (porHacerLleno) FontWeight.Bold else FontWeight.Medium,
                    color = if (porHacerLleno) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "⏳ En progreso: $actualEnProgreso/$limiteEnProgreso",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (progresoLleno) FontWeight.Bold else FontWeight.Medium,
                    color = if (progresoLleno) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EstadoVacioKanban() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) { Text("📝", fontSize = 36.sp) }
            }
            Spacer(Modifier.height(12.dp))
            Text("Sin tareas aún", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Toca + para crear la primera", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}