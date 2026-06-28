package com.example.spire_task.data.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una tarea (tarjeta Kanban).
 *
 * Estados posibles:
 * - POR_HACER
 * - EN_PROGRESO
 * - FINALIZADO
 *
 * Prioridades:
 * - 1 = Baja
 * - 2 = Media
 * - 3 = Alta
 */
@Entity(tableName = "tarea")
data class TareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id_tarea: Int = 0,
    val id_tablero: Int,                        // FK → TableroEntity
    val titulo: String,                         // Título de la tarea
    val descripcion: String? = null,            // Descripción opcional
    val estado: String = "POR_HACER",           // POR_HACER, EN_PROGRESO, FINALIZADO
    val prioridad: Int = 1,                     // 1=Baja, 2=Media, 3=Alta
    val fecha_limite: Long? = null,             // Timestamp de vencimiento
    val orden: Int = 0,                         // Posición en la columna (para drag & drop)
    val recompensa_reclamada: Boolean = false,  // ✅ NUEVO: si ya se reclamó la recompensa
    val is_deleted: Boolean = false,            // Borrado lógico
    val fecha_creacion: Long = System.currentTimeMillis(),
    val fecha_modificacion: Long = System.currentTimeMillis(),
    val fecha_completado: Long? = null,
    val penalizacion_aplicada: Boolean = false// ✅ NUEVO: timestamp cuando se completó la tarea
)