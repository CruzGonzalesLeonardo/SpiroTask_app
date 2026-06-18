package com.example.spire_task.data.local.daos

import androidx.room.*
import com.example.spire_task.data.local.entidades.SubtareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtareaDao {

    @Query("SELECT * FROM subtarea WHERE id_tarea = :idTarea AND is_deleted = 0 ORDER BY orden ASC")
    fun obtenerPorTarea(idTarea: Int): Flow<List<SubtareaEntity>>

    @Query("SELECT * FROM subtarea WHERE id_tarea = :idTarea AND is_deleted = 0 ORDER BY orden ASC")
    suspend fun obtenerPorTareaDirecto(idTarea: Int): List<SubtareaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(subtarea: SubtareaEntity): Long

    @Update
    suspend fun actualizar(subtarea: SubtareaEntity)

    @Query("UPDATE subtarea SET completada = :completada WHERE id_subtarea = :id")
    suspend fun toggleCompletada(id: Int, completada: Boolean)

    @Query("UPDATE subtarea SET is_deleted = 1 WHERE id_subtarea = :id")
    suspend fun borrarLogicamente(id: Int)

    @Query("DELETE FROM subtarea")
    suspend fun eliminarTodas()

    @Query("SELECT * FROM subtarea WHERE is_deleted = 0")
    suspend fun obtenerTodasActivasDirecto(): List<SubtareaEntity>
}