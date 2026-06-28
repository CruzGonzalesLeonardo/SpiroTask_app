package com.example.spire_task.data.local.daos

import androidx.room.*
import com.example.spire_task.data.local.entidades.MascotaUsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MascotaUsuarioDao {

    /** Obtener todas las mascotas del usuario */
    @Query("SELECT * FROM mascota_usuario ORDER BY fecha_obtencion ASC")
    fun obtenerTodas(): Flow<List<MascotaUsuarioEntity>>

    /** Obtener la mascota activa actual */
    @Query("SELECT * FROM mascota_usuario WHERE esta_activa = 1 LIMIT 1")
    fun obtenerMascotaActiva(): Flow<MascotaUsuarioEntity?>

    /** Obtener mascota activa de forma directa */
    @Query("SELECT * FROM mascota_usuario WHERE esta_activa = 1 LIMIT 1")
    suspend fun obtenerMascotaActivaDirecta(): MascotaUsuarioEntity?

    /** Obtener una mascota por su ID */
    @Query("SELECT * FROM mascota_usuario WHERE id_mascota_usuario = :id")
    suspend fun obtenerPorId(id: Int): MascotaUsuarioEntity?

    /** Verificar si el usuario ya tiene una especie específica */
    @Query("SELECT COUNT(*) FROM mascota_usuario WHERE id_mascota_base = :idBase")
    suspend fun tieneEspecie(idBase: Int): Int

    /** Insertar nueva mascota */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(mascota: MascotaUsuarioEntity): Long

    /** Desactivar todas las mascotas (antes de activar una nueva) */
    @Query("UPDATE mascota_usuario SET esta_activa = 0")
    suspend fun desactivarTodas()

    /** Activar una mascota específica */
    @Query("UPDATE mascota_usuario SET esta_activa = 1 WHERE id_mascota_usuario = :id")
    suspend fun activarMascota(id: Int)

    /** Cambiar mascota activa (desactiva todas + activa la elegida) */
    @Transaction
    suspend fun cambiarMascotaActiva(id: Int) {
        desactivarTodas()
        activarMascota(id)
    }

    /** Sumar experiencia a una mascota */
    @Query("""
        UPDATE mascota_usuario 
        SET experiencia = experiencia + :xp 
        WHERE id_mascota_usuario = :id
    """)
    suspend fun sumarExperiencia(id: Int, xp: Int)

    /** Subir nivel de una mascota */
    @Query("""
        UPDATE mascota_usuario 
        SET nivel = nivel + 1, 
            experiencia = 0 
        WHERE id_mascota_usuario = :id
    """)
    suspend fun subirNivel(id: Int)

    /** Actualizar nombre personalizado */
    @Query("UPDATE mascota_usuario SET nombre_personalizado = :nombre WHERE id_mascota_usuario = :id")
    suspend fun actualizarNombre(id: Int, nombre: String)

    /** Eliminar una mascota */
    @Query("DELETE FROM mascota_usuario WHERE id_mascota_usuario = :id")
    suspend fun eliminar(id: Int)

    @Query("DELETE FROM mascota_usuario")
    suspend fun eliminarTodas()

    // ✅ NUEVOS MÉTODOS PARA FELICIDAD

    /** Actualizar felicidad de una mascota */
    @Query("""
    UPDATE mascota_usuario 
    SET felicidad_actual = :felicidad, 
        ultima_interaccion = :timestamp 
    WHERE id_mascota_usuario = :id
""")
    suspend fun actualizarFelicidad(id: Int, felicidad: Int, timestamp: Long = System.currentTimeMillis())

    /** Obtener mascotas con felicidad baja (menor a 30) */
    @Query("SELECT * FROM mascota_usuario WHERE felicidad_actual < 30")
    suspend fun obtenerMascotasConFelicidadBaja(): List<MascotaUsuarioEntity>

    /** Incrementar felicidad (por interacción) */
    @Query("""
        UPDATE mascota_usuario 
        SET felicidad_actual = min(100, felicidad_actual + :incremento),
            ultima_interaccion = :timestamp
        WHERE id_mascota_usuario = :id
    """)
    suspend fun incrementarFelicidad(id: Int, incremento: Int, timestamp: Long = System.currentTimeMillis())

    /** Disminuir felicidad (por abandono) */
    @Query("""
        UPDATE mascota_usuario 
        SET felicidad_actual = max(0, felicidad_actual - :decremento)
        WHERE id_mascota_usuario = :id
    """)
    suspend fun disminuirFelicidad(id: Int, decremento: Int)

    @Query("SELECT * FROM mascota_usuario")
    suspend fun obtenerTodosDirecto(): List<MascotaUsuarioEntity>

    @Query("""
    SELECT * FROM mascota_usuario 
    WHERE id_mascota_usuario IN (SELECT id_mascota_mentora FROM tablero WHERE is_deleted = 0)
""")
    suspend fun obtenerMascotasMentorasActivas(): List<MascotaUsuarioEntity>

    @Update
    suspend fun actualizarMascotas(mascotas: List<MascotaUsuarioEntity>)


    @Query("""
    UPDATE mascota_usuario 
    SET felicidad_actual = MAX(0, felicidad_actual - :puntos)
    WHERE id_mascota_usuario = (
        SELECT id_mascota_mentora FROM tablero WHERE id_tablero = :idTablero LIMIT 1
    )
""")
    suspend fun reducirFelicidadPorTablero(idTablero: Int, puntos: Int)
}