package com.example.spire_task.domain.repositories

import com.example.spire_task.data.local.entities.ColumnEntity
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.feature.kanban.models.ColumnWithTasks
import kotlinx.coroutines.flow.Flow

interface ITaskRepository {

    fun getAllTasks(userId: String): Flow<List<TaskEntity>>

    fun getTasksByColumn(columnId: Int, userId: String): Flow<List<TaskEntity>>

    fun getColumnsWithTasks(userId: String): Flow<List<ColumnWithTasks>>

    fun getFavoriteTasks(userId: String): Flow<List<TaskEntity>>

    fun getTasksByDate(date: Long, userId: String): Flow<List<TaskEntity>>

    suspend fun getTaskById(taskId: Int, userId: String): TaskEntity?

    fun getAllColumns(userId: String): Flow<List<ColumnEntity>>

    suspend fun insertTask(task: TaskEntity)

    suspend fun updateTask(task: TaskEntity)

    suspend fun deleteTask(task: TaskEntity)

    suspend fun insertColumn(column: ColumnEntity)

    suspend fun insertDefaultColumns(userId: String)
}