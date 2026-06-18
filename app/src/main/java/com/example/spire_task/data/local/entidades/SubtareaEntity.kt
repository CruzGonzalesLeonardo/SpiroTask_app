package com.example.spire_task.data.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una subtarea (checklist atómico).
 *
 * Pertenece a una tarea padre.
 * Solo tiene dos estados: completada o pendiente.
 */
@Entity(tableName = "subtarea")
data class SubtareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id_subtarea: Int = 0,
    val id_tarea: Int,                          // FK → TareaEntity
    val descripcion: String,                    // Texto del paso a completar
    val completada: Boolean = false,            // true = completada
    val orden: Int = 0,                         // Posición en la lista
    val is_deleted: Boolean = false             // Borrado lógico
)