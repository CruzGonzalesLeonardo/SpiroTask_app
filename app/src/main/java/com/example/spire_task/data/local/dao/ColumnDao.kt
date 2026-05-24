package com.example.spire_task.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spire_task.data.local.entities.ColumnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ColumnDao {

    @Query("SELECT * FROM columns ORDER BY `order` ASC")
    fun getAllColumns(): Flow<List<ColumnEntity>>

    @Query("SELECT * FROM columns ORDER BY `order` ASC")
    suspend fun getAllColumnsOnce(): List<ColumnEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColumns(columns: List<ColumnEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(column: ColumnEntity)
}