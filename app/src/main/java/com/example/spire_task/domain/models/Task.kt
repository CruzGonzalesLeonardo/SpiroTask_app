package com.example.spire_task.domain.models

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String,
    val status: String,       // "TODO", "IN_PROGRESS", "DONE"
    val priority: String,     // "ALTA", "MEDIA", "BAJA"
    val dueDate: Long?,
    val createdAt: Long = System.currentTimeMillis()
)