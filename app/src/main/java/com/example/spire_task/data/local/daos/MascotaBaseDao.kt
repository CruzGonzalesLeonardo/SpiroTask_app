package com.example.spire_task.data.local.daos

import androidx.room.*
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MascotaBaseDao {

    /** Obtener todas las especies del catálogo */
    @Query("SELECT * FROM mascota_base ORDER BY id_mascota_base ASC")
    fun obtenerTodas(): Flow<List<MascotaBaseEntity>>

    /** Obtener todas de forma directa */
    @Query("SELECT * FROM mascota_base ORDER BY id_mascota_base ASC")
    suspend fun obtenerTodasDirecto(): List<MascotaBaseEntity>

    /** Obtener una especie por su ID */
    @Query("SELECT * FROM mascota_base WHERE id_mascota_base = :id")
    suspend fun obtenerPorId(id: Int): MascotaBaseEntity?

    /** Insertar mascotas precargadas (solo se usa al crear la BD) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(mascotas: List<MascotaBaseEntity>)

    /** Contar cuántas especies hay (para verificar precarga) */
    @Query("SELECT COUNT(*) FROM mascota_base")
    suspend fun contar(): Int

    // ✅ NUEVO: Obtener solo la habilidad de una mascota
    @Query("""
        SELECT habilidad_nombre, habilidad_descripcion, habilidad_tipo, habilidad_valor 
        FROM mascota_base 
        WHERE id_mascota_base = :id
    """)
    suspend fun obtenerHabilidadPorId(id: Int): HabilidadSimplificada?
}

// Data class para la habilidad simplificada
data class HabilidadSimplificada(
    val habilidad_nombre: String,
    val habilidad_descripcion: String,
    val habilidad_tipo: String,  // "MENTORA" o "ACTIVA"
    val habilidad_valor: Float
)