package com.example.spire_task.data.local.dao

import androidx.room.*
import com.example.spire_task.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getAllTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE columnId = :columnId AND userId = :userId ORDER BY updatedAt DESC")
    fun getAllTasksByColumn(columnId: Int, userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isFavorite = 1 AND userId = :userId ORDER BY updatedAt DESC")
    fun getFavoriteTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate = :date AND userId = :userId ORDER BY updatedAt DESC")
    fun getTasksByDate(date: Long, userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId AND userId = :userId LIMIT 1")
    suspend fun getTaskById(taskId: Int, userId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}