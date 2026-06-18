package com.example.spire_task.data.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spire_task.data.local.database.DatosIniciales

/**
 * Entidad que representa el perfil del usuario.
 *
 * SINGLE-USER: Solo existe UN registro con id = 1.
 * Si no tiene id_google, es un perfil local sin respaldo.
 * Si tiene id_google, está vinculado a una cuenta Google.
 */
@Entity(tableName = "perfil_usuario")
data class PerfilUsuarioEntity(
    @PrimaryKey
    val id: Int = 1,                        // Siempre 1 (single-user)
    val nombre: String,                     // Nombre del estudiante
    val id_google: String? = null,          // ID de Google (null = no vinculado)
    val monedas: Int = DatosIniciales.MONEDAS_INICIALES,                // Saldo de monedas del juego
    val nivel_perfil: Int = 1,// Nivel de cuenta
    val google_email: String? = null,
    val experiencia_perfil: Int = 0,        // XP acumulada del perfil
    val fecha_creacion: Long = System.currentTimeMillis(),
    val ultima_sincronizacion: Long? = null // Timestamp de último backup a nube
)