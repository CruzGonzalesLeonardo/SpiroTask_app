package com.example.spire_task.data.repository

import android.util.Log
import com.example.spire_task.data.local.dao.ColumnDao
import com.example.spire_task.data.local.dao.TaskDao
import com.example.spire_task.data.local.entities.ColumnEntity
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.domain.repositories.ITaskRepository
import com.example.spire_task.feature.kanban.models.ColumnWithTasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val columnDao: ColumnDao
) : ITaskRepository {

    override fun getAllTasks(userId: String): Flow<List<TaskEntity>> {
        return taskDao.getAllTasks(userId)
    }

    override fun getTasksByColumn(columnId: Int, userId: String): Flow<List<TaskEntity>> {
        return taskDao.getAllTasksByColumn(columnId, userId)
    }

    override fun getColumnsWithTasks(userId: String): Flow<List<ColumnWithTasks>> {
        Log.d("KANBAN_DEBUG", "getColumnsWithTasks para userId: $userId")
        return combine(
            columnDao.getAllColumns(userId),  // ← Ahora filtra por userId
            taskDao.getAllTasks(userId)
        ) { columnas, tareas ->
            Log.d("KANBAN_DEBUG", "Columnas para $userId: ${columnas.size}")
            columnas.map { columna ->
                ColumnWithTasks(
                    column = columna,
                    tasks = tareas.filter { it.columnId == columna.id }
                )
            }
        }
    }

    override fun getFavoriteTasks(userId: String): Flow<List<TaskEntity>> {
        return taskDao.getFavoriteTasks(userId)
    }

    override fun getTasksByDate(date: Long, userId: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksByDate(date, userId)
    }

    override suspend fun getTaskById(taskId: Int, userId: String): TaskEntity? {
        return taskDao.getTaskById(taskId, userId)
    }

    override fun getAllColumns(userId: String): Flow<List<ColumnEntity>> {
        return columnDao.getAllColumns(userId)
    }

    override suspend fun insertTask(task: TaskEntity) {
        Log.d("KANBAN_DEBUG", "Insertando tarea para usuario ${task.userId}")
        taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    override suspend fun insertColumn(column: ColumnEntity) {
        columnDao.insert(column)
    }

    override suspend fun insertDefaultColumns(userId: String) {
        val existingColumns = columnDao.getAllColumnsOnce(userId)

        Log.d("KANBAN_DEBUG", "Usuario $userId - Columnas existentes: ${existingColumns.size}")

        if (existingColumns.isEmpty()) {
            Log.d("KANBAN_DEBUG", "Creando columnas para nuevo usuario: $userId")
            columnDao.insertColumns(
                listOf(
                    ColumnEntity(
                        name = "Por hacer",
                        wipLimit = 10,
                        order = 1,
                        userId = userId  // ← Asignar userId
                    ),
                    ColumnEntity(
                        name = "En progreso",
                        wipLimit = 5,
                        order = 2,
                        userId = userId
                    ),
                    ColumnEntity(
                        name = "Completado",
                        wipLimit = 999,
                        order = 3,
                        userId = userId
                    )
                )
            )
        }
    }
}