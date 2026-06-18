package com.example.spire_task.data.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un tablero (asignatura/área de trabajo).
 *
 * Soporta borrado lógico (is_deleted) para no perder datos
 * antes de la sincronización con la nube.
 */
@Entity(tableName = "tablero")
data class TableroEntity(
    @PrimaryKey(autoGenerate = true)
    val id_tablero: Int = 0,
    val nombre: String,                         // Ej: "Cálculo II"
    val color_hex: String = "#7C3AED",          // Color identificativo
    val id_mascota_mentora: Int,        // FK → MascotaUsuarioEntity
    val is_deleted: Boolean = false,            // Borrado lógico
    val fecha_creacion: Long = System.currentTimeMillis(),
    val fecha_modificacion: Long = System.currentTimeMillis()
)