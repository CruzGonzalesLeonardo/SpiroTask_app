package com.example.spire_task.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "columns",
    indices = [Index(value = ["userId"])]  // Índice para búsquedas por usuario
)
data class ColumnEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val wipLimit: Int,
    val order: Int,
    val userId: String  // ✅ NUEVO: Asociar columnas al usuario
)