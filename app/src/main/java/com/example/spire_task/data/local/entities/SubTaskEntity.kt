package com.example.spire_task.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sub_tasks")
data class SubTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,         // Vincula este subitem con el ID de la tarea principal
    val title: String,
    val isCompleted: Boolean = false
)