package com.example.spire_task.feature.kanban.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanFilterChips(
    filtroActivo: String,
    onFiltroChange: (String) -> Unit,
    conteos: Map<String, Int>,
    limites: Map<String, Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val estados = listOf("POR_HACER", "EN_PROGRESO", "FINALIZADO")

        estados.forEach { estado ->
            val actual = conteos[estado] ?: 0
            val limite = limites[estado]
            val textoConteo = if (limite != null) "$actual/$limite" else "$actual"

            val etiqueta = when (estado) {
                "POR_HACER" -> "Por Hacer"
                "EN_PROGRESO" -> "En Progreso"
                "FINALIZADO" -> "Finalizado"
                else -> ""
            }

            FilterChip(
                selected = filtroActivo == estado,
                onClick = { onFiltroChange(estado) },
                label = {
                    Text(
                        text = "$etiqueta ($textoConteo)",
                        fontWeight = if (filtroActivo == estado) FontWeight.Bold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun LimitesBar(
    limitePorHacer: Int,
    actualPorHacer: Int,
    limiteEnProgreso: Int,
    actualEnProgreso: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val sobrepasaHacer = actualPorHacer >= limitePorHacer
        val sobrepasaProgreso = actualEnProgreso >= limiteEnProgreso

        SuggestionChip(
            onClick = {},
            label = { Text("Límite Por Hacer: $actualPorHacer/$limitePorHacer", fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            colors = SuggestionChipDefaults.suggestionChipColors(
                labelColor = if (sobrepasaHacer) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        SuggestionChip(
            onClick = {},
            label = { Text("En Progreso: $actualEnProgreso/$limiteEnProgreso", fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            colors = SuggestionChipDefaults.suggestionChipColors(
                labelColor = if (sobrepasaProgreso) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}