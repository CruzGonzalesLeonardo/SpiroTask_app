package com.example.spire_task.data.local.daos

import androidx.room.*
import com.example.spire_task.data.local.entidades.PerfilUsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilUsuarioDao {

    /** Obtener el perfil (siempre id=1) */
    @Query("SELECT * FROM perfil_usuario WHERE id = 1")
    fun obtenerPerfil(): Flow<PerfilUsuarioEntity?>

    /** Obtener perfil de forma directa (para verificaciones rápidas) */
    @Query("SELECT * FROM perfil_usuario WHERE id = 1")
    suspend fun obtenerPerfilDirecto(): PerfilUsuarioEntity?

    /** Verificar si existe un perfil creado */
    @Query("SELECT COUNT(*) FROM perfil_usuario WHERE id = 1")
    suspend fun existePerfil(): Int

    /** Insertar o actualizar el perfil */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(perfil: PerfilUsuarioEntity)

    /** Actualizar monedas */
    @Query("UPDATE perfil_usuario SET monedas = :monedas WHERE id = 1")
    suspend fun actualizarMonedas(monedas: Int)

    /** Sumar experiencia al perfil */
    @Query("""
        UPDATE perfil_usuario 
        SET experiencia_perfil = experiencia_perfil + :xp 
        WHERE id = 1
    """)
    suspend fun sumarExperiencia(xp: Int)

    /** Subir nivel de perfil */
    @Query("""
        UPDATE perfil_usuario 
        SET nivel_perfil = nivel_perfil + 1, 
            experiencia_perfil = 0 
        WHERE id = 1
    """)
    suspend fun subirNivel()

    /** Eliminar perfil (para cerrar sesión) */
    @Query("DELETE FROM perfil_usuario WHERE id = 1")
    suspend fun eliminarPerfil()

}