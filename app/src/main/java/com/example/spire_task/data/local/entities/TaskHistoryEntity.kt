package com.example.spire_task.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_history")
data class TaskHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskId: Int,
    val fromColumn: Int,
    val toColumn: Int,
    val timestamp: Long
)