package com.example.spire_task.domain.repositories

import com.example.spire_task.data.local.entities.ColumnEntity
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.feature.dashboard.components.ColumnWithTasks
import kotlinx.coroutines.flow.Flow

interface ITaskRepository {

    fun getAllTasks(): Flow<List<TaskEntity>>

    fun getTasksByColumn(columnId: Int): Flow<List<TaskEntity>>

    fun getColumnsWithTasks(): Flow<List<ColumnWithTasks>>

    suspend fun getTaskById(taskId: Int): TaskEntity?

    fun getTasksByDate(date: Long): Flow<List<TaskEntity>>

    fun getFavoriteTasks(): Flow<List<TaskEntity>>

    fun getAllColumns(): Flow<List<ColumnEntity>>

    suspend fun insertTask(task: TaskEntity)

    suspend fun updateTask(task: TaskEntity)

    suspend fun deleteTask(task: TaskEntity)

    suspend fun insertColumn(column: ColumnEntity)

    suspend fun insertDefaultColumns()
}