package com.example.spire_task.feature.kanban

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object KanbanUtils {

    fun formatearFecha(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatearFechaCompleta(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd 'de' MMMM", Locale("es"))
        return sdf.format(Date(timestamp))
    }

    fun calcularDiasRestantes(fechaLimite: Long): Long {
        val ahora = System.currentTimeMillis()
        val diff = fechaLimite - ahora
        return TimeUnit.MILLISECONDS.toDays(diff)
    }
}

// Extension functions para colores
@Composable
fun Int.getColorPrioridad(): Color {
    return when (this) {
        3 -> PriorityHighColor
        2 -> PriorityMediumColor
        else -> PriorityLowColor
    }
}
@Composable
fun String.getColorEstado(): Color {
    return when (this) {
        "POR_HACER" -> KanbanTodoColor
        "EN_PROGRESO" -> KanbanProgressColor
        "FINALIZADO" -> KanbanDoneColor
        else -> MaterialTheme.colorScheme.primary
    }
}