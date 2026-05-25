package com.example.spire_task.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spire_task.data.local.entities.ColumnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ColumnDao {

    // ✅ Filtrar por userId
    @Query("SELECT * FROM columns WHERE userId = :userId ORDER BY `order` ASC")
    fun getAllColumns(userId: String): Flow<List<ColumnEntity>>

    // ✅ Filtrar por userId
    @Query("SELECT * FROM columns WHERE userId = :userId ORDER BY `order` ASC")
    suspend fun getAllColumnsOnce(userId: String): List<ColumnEntity>

    @Query("SELECT COUNT(*) FROM columns WHERE userId = :userId")
    suspend fun getColumnCountForUser(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColumns(columns: List<ColumnEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(column: ColumnEntity)

    // Opcional: Limpiar columnas de un usuario específico
    @Query("DELETE FROM columns WHERE userId = :userId")
    suspend fun deleteColumnsByUser(userId: String)
}