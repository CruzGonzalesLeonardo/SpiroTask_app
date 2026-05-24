package com.example.spire_task.feature.dashboard.components

import com.example.spire_task.data.local.entities.ColumnEntity
import com.example.spire_task.data.local.entities.TaskEntity

data class ColumnWithTasks(
    val column: ColumnEntity,
    val tasks: List<TaskEntity>
)