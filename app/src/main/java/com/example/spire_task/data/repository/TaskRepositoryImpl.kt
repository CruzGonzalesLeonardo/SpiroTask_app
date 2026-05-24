package com.example.spire_task.data.repository

import com.example.spire_task.data.local.dao.ColumnDao
import com.example.spire_task.data.local.dao.TaskDao
import com.example.spire_task.data.local.entities.ColumnEntity
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.domain.repositories.ITaskRepository
import com.example.spire_task.feature.dashboard.components.ColumnWithTasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val columnDao: ColumnDao
) : ITaskRepository {

    override fun getAllTasks(): Flow<List<TaskEntity>> {
        return taskDao.getAllTasks()
    }

    override fun getTasksByColumn(columnId: Int): Flow<List<TaskEntity>> {
        return taskDao.getAllTasksByColumn(columnId)
    }

    override fun getColumnsWithTasks(): Flow<List<ColumnWithTasks>> {
        return combine(
            columnDao.getAllColumns(),
            taskDao.getAllTasks()
        ) { columnas, tareas ->
            columnas.map { columna ->
                ColumnWithTasks(
                    column = columna,
                    tasks = tareas.filter { it.columnId == columna.id }
                )
            }
        }
    }

    override suspend fun getTaskById(taskId: Int): TaskEntity? {
        return taskDao.getTaskById(taskId)
    }

    override fun getTasksByDate(date: Long): Flow<List<TaskEntity>> {
        return taskDao.getTasksByDate(date)
    }

    override fun getFavoriteTasks(): Flow<List<TaskEntity>> {
        return taskDao.getFavoriteTasks()
    }

    override fun getAllColumns(): Flow<List<ColumnEntity>> {
        return columnDao.getAllColumns()
    }

    override suspend fun insertTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(
            task.copy(updatedAt = System.currentTimeMillis())
        )
    }

    override suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    override suspend fun insertColumn(column: ColumnEntity) {
        columnDao.insert(column)
    }

    override suspend fun insertDefaultColumns() {
        val existingColumns = columnDao.getAllColumns().first()

        if (existingColumns.isEmpty()) {
            columnDao.insertColumns(
                listOf(
                    ColumnEntity(
                        name = "Por hacer",
                        wipLimit = 10,
                        order = 1
                    ),
                    ColumnEntity(
                        name = "En progreso",
                        wipLimit = 5,
                        order = 2
                    ),
                    ColumnEntity(
                        name = "Completado",
                        wipLimit = 999,
                        order = 3
                    )
                )
            )
        }
    }
}