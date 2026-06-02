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

    // Actualizar monedas (puede ser positivo o negativo)
    @Query("UPDATE tblPerfiles SET monedas = :nuevasMonedas WHERE idUser = :userId")
    suspend fun actualizarMonedas(userId: String, nuevasMonedas: Int)

    // Sumar monedas
    @Query("UPDATE tblPerfiles SET monedas = monedas + :cantidad WHERE idUser = :userId")
    suspend fun sumarMonedas(userId: String, cantidad: Int)

    // Actualizar racha
    @Query("UPDATE tblPerfiles SET racha = :racha, maxRacha = :maxRacha WHERE idUser = :userId")
    suspend fun actualizarRacha(userId: String, racha: Int, maxRacha: Int)

    // Eliminar un perfil
    @Query("DELETE FROM tblPerfiles WHERE idUser = :userId")
    suspend fun eliminar(userId: String)

    @Query("UPDATE tblPerfiles SET userName = :newName, email = :newEmail WHERE idUser = :userId")
    suspend fun actualizarDatos(userId: String, newName: String, newEmail: String)

    @Query("SELECT * FROM tblPerfiles WHERE authProvider = 'local' LIMIT 1")
    suspend fun obtenerUsuarioInvitado(): ProfileEntity?

    @Query("SELECT racha FROM tblPerfiles WHERE idUser = :userId")
    suspend fun obtenerRacha(userId: String): Int?

    @Query("SELECT monedas FROM tblPerfiles WHERE idUser = :userId")
    suspend fun obtenerMonedas(userId: String): Int?

    @Query("SELECT monedas FROM tblPerfiles WHERE idUser = :userId")
    fun observeMonedas(userId: String): Flow<Int?>

    @Query("SELECT racha FROM tblPerfiles WHERE idUser = :userId")
    fun observeRacha(userId: String): Flow<Int?>

    @Query("SELECT * FROM tblPerfiles WHERE idUser = :userId")
    fun observeProfile(userId: String): Flow<ProfileEntity?>
}