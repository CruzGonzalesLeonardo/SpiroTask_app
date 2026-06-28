package com.example.spire_task.feature.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Tarjeta que muestra las estadísticas del día.
 *
 * @param tareasCompletadas Tareas completadas hoy
 * @param tareasTotales Total de tareas del día
 * @param minutosEnfocado Minutos en sesiones de enfoque
 * @param rachaDias Días consecutivos de racha
 */
@Composable
fun EstadisticasCard(
    tareasCompletadas: Int = 0,
    tareasTotales: Int = 0,
    minutosEnfocado: Int = 0,
    rachaDias: Int = 0,
    iconoTareas: ImageVector,
    iconoRacha: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EstadisticaItem(
                icono = iconoTareas,
                valor = "$tareasCompletadas/$tareasTotales",
                etiqueta = "Tareas",
                color = MaterialTheme.colorScheme.primary
            )
            EstadisticaItem(
                icono = iconoRacha,
                valor = "$rachaDias",
                etiqueta = "Racha",
                color = Color(0xFFFF6B35)
            )
        }
    }
}

/**
 * Item individual de estadística.
 */
@Composable
private fun EstadisticaItem(
    icono: ImageVector,
    valor: String,
    etiqueta: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icono,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}