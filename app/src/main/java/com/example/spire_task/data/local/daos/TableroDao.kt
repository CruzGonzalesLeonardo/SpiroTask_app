package com.example.spire_task.data.local.daos

import androidx.room.*
import com.example.spire_task.data.local.entidades.TableroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TableroDao {

    /** Obtener todos los tableros activos (no eliminados) */
    @Query("""
        SELECT * FROM tablero 
        WHERE is_deleted = 0 
        ORDER BY fecha_modificacion DESC
    """)
    fun obtenerTodos(): Flow<List<TableroEntity>>

    /** Obtener todos de forma directa */
    @Query("SELECT * FROM tablero WHERE is_deleted = 0 ORDER BY fecha_modificacion DESC")
    suspend fun obtenerTodosDirecto(): List<TableroEntity>

    /** Obtener un tablero por ID */
    @Query("SELECT * FROM tablero WHERE id_tablero = :id AND is_deleted = 0")
    suspend fun obtenerPorId(id: Int): TableroEntity?

    /** Insertar un nuevo tablero */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tablero: TableroEntity): Long

    /** Actualizar un tablero */
    @Update
    suspend fun actualizar(tablero: TableroEntity)

    /** Borrado lógico (no elimina físicamente) */
    @Query("""
        UPDATE tablero 
        SET is_deleted = 1, fecha_modificacion = :timestamp 
        WHERE id_tablero = :id
    """)
    suspend fun borrarLogicamente(id: Int, timestamp: Long = System.currentTimeMillis())

    /** Eliminar físicamente (solo para limpieza de datos) */
    @Query("DELETE FROM tablero WHERE id_tablero = :id")
    suspend fun eliminarFisicamente(id: Int)

    /** Actualizar color del tablero */
    @Query("UPDATE tablero SET color_hex = :color WHERE id_tablero = :id")
    suspend fun actualizarColor(id: Int, color: String)

    /** Asignar mascota mentora */
    @Query("UPDATE tablero SET id_mascota_mentora = :idMascota WHERE id_tablero = :id")
    suspend fun asignarMascotaMentora(id: Int, idMascota: Int?)

    /** Contar tableros activos */
    @Query("SELECT COUNT(*) FROM tablero WHERE is_deleted = 0")
    suspend fun contarActivos(): Int

    /** Verificar si el usuario tiene mascotas disponibles para crear un tablero */
    @Query("SELECT COUNT(*) FROM mascota_usuario")
    suspend fun contarMascotasUsuario(): Int

    @Query("DELETE FROM tablero")
    suspend fun eliminarTodos()
}