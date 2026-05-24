package com.example.spire_task.data.local.dao

import androidx.room.*
import com.example.spire_task.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE columnId = :columnId ORDER BY updatedAt DESC")
    fun getAllTasksByColumn(columnId: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    @Query("SELECT * FROM tasks WHERE dueDate = :date ORDER BY updatedAt DESC")
    fun getTasksByDate(date: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}