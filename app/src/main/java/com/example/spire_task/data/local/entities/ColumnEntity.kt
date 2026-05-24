package com.example.spire_task.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "columns")
data class ColumnEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val wipLimit: Int,
    val order: Int
)