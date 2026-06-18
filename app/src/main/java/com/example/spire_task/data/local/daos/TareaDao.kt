package com.example.spire_task.data.local.daos

import androidx.room.*
import com.example.spire_task.data.local.entidades.TableroEntity
import com.example.spire_task.data.local.entidades.TareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {

    @Query("""
        SELECT * FROM tarea 
        WHERE id_tablero = :idTablero AND is_deleted = 0 
        ORDER BY orden ASC, fecha_creacion ASC
    """)
    fun obtenerPorTablero(idTablero: Int): Flow<List<TareaEntity>>

    @Query("""
        SELECT * FROM tarea 
        WHERE id_tablero = :idTablero AND estado = :estado AND is_deleted = 0 
        ORDER BY orden ASC, fecha_creacion ASC
    """)
    fun obtenerPorTableroYEstado(idTablero: Int, estado: String): Flow<List<TareaEntity>>

    @Query("""
        SELECT * FROM tarea 
        WHERE id_tablero = :idTablero AND estado = :estado AND is_deleted = 0 
        ORDER BY orden ASC
    """)
    suspend fun obtenerPorTableroYEstadoDirecto(idTablero: Int, estado: String): List<TareaEntity>

    @Query("SELECT * FROM tarea WHERE id_tarea = :id AND is_deleted = 0")
    suspend fun obtenerPorId(id: Int): TareaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarea: TareaEntity): Long

    @Update
    suspend fun actualizar(tarea: TareaEntity)

    // En TareaDao.kt - agrega este método si no existe
    @Query("""
    UPDATE tarea 
    SET estado = :nuevoEstado, 
        fecha_modificacion = :timestamp,
        fecha_completado = CASE 
            WHEN :nuevoEstado = 'FINALIZADO' AND fecha_completado IS NULL THEN :timestamp 
            ELSE fecha_completado 
        END
    WHERE id_tarea = :id
""")
    suspend fun cambiarEstado(id: Int, nuevoEstado: String, timestamp: Long = System.currentTimeMillis())
    @Query("""
        UPDATE tarea 
        SET recompensa_reclamada = 1, 
            fecha_modificacion = :timestamp 
        WHERE id_tarea = :id
    """)
    suspend fun marcarRecompensaReclamada(id: Int, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT recompensa_reclamada FROM tarea WHERE id_tarea = :id")
    suspend fun isRecompensaReclamada(id: Int): Boolean

    @Query("UPDATE tarea SET orden = :orden WHERE id_tarea = :id")
    suspend fun actualizarOrden(id: Int, orden: Int)

    @Query("""
        UPDATE tarea 
        SET is_deleted = 1, fecha_modificacion = :timestamp 
        WHERE id_tarea = :id
    """)
    suspend fun borrarLogicamente(id: Int, timestamp: Long = System.currentTimeMillis())

    @Query("""
        SELECT COUNT(*) FROM tarea 
        WHERE id_tablero = :idTablero AND estado = :estado AND is_deleted = 0
    """)
    suspend fun contarPorEstado(idTablero: Int, estado: String): Int

    @Query("SELECT * FROM tarea WHERE is_deleted = 0")
    suspend fun obtenerTodasActivas(): List<TareaEntity>

    @Query("SELECT COUNT(*) FROM tarea WHERE id_tablero = :idTablero AND is_deleted = 0")
    suspend fun contarPorTablero(idTablero: Int): Int

    @Query("SELECT COUNT(*) FROM tarea WHERE id_tablero = :idTablero AND estado = 'FINALIZADO' AND is_deleted = 0")
    suspend fun contarCompletadasPorTablero(idTablero: Int): Int

    @Query("""
        SELECT * FROM tarea 
        WHERE estado = 'FINALIZADO' 
        AND recompensa_reclamada = 0 
        AND is_deleted = 0
    """)
    suspend fun obtenerTareasCompletadasSinReclamar(): List<TareaEntity>

    @Query("DELETE FROM tarea")
    suspend fun eliminarTodas()

    @Query("SELECT * FROM tablero WHERE is_deleted = 0")
    suspend fun obtenerTodos(): List<TableroEntity>

    @Query("SELECT * FROM tarea WHERE is_deleted = 0")
    suspend fun obtenerTodasActivasDirecto(): List<TareaEntity>

}