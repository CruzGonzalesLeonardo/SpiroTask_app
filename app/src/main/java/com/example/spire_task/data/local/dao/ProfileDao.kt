package com.example.spire_task.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spire_task.data.local.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    // Insertar o reemplazar un perfil
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(profile: ProfileEntity)

    // Obtener un perfil por su ID
    @Query("SELECT * FROM tblPerfiles WHERE idUser = :userId")
    suspend fun obtenerPorId(userId: String): ProfileEntity?

    // Obtener un perfil por nombre de usuario
    @Query("SELECT * FROM tblPerfiles WHERE userName = :userName")
    suspend fun obtenerPorUserName(userName: String): ProfileEntity?

    // Obtener todos los perfiles (para depuración)
    @Query("SELECT * FROM tblPerfiles")
    fun obtenerTodos(): Flow<List<ProfileEntity>>

    // Actualizar experiencia y nivel
    @Query("UPDATE tblPerfiles SET xpTotal = :xp, level = :level WHERE idUser = :userId")
    suspend fun actualizarProgreso(userId: String, xp: Float, level: Int)

    // Sumar monedas
    @Query("UPDATE tblPerfiles SET monedas = monedas + :cantidad WHERE idUser = :userId")
    suspend fun sumarMonedas(userId: String, cantidad: Int)

    // Actualizar racha
    @Query("UPDATE tblPerfiles SET racha = :racha, maxRacha = :maxRacha WHERE idUser = :userId")
    suspend fun actualizarRacha(userId: String, racha: Int, maxRacha: Int)

    // Eliminar un perfil
    @Query("DELETE FROM tblPerfiles WHERE idUser = :userId")
    suspend fun eliminar(userId: String)

    @Query("SELECT * FROM tblPerfiles WHERE authProvider = 'local' LIMIT 1")
    suspend fun obtenerUsuarioInvitado(): ProfileEntity?
}