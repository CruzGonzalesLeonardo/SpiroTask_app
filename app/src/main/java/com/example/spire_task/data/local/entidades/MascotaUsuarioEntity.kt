package com.example.spire_task.data.local.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidad que representa las mascotas que posee el usuario.
 * Eliminamos cosméticos y simplificamos.
 */
@Entity(
    tableName = "mascota_usuario",
    foreignKeys = [
        ForeignKey(
            entity = MascotaBaseEntity::class,
            parentColumns = ["id_mascota_base"],
            childColumns = ["id_mascota_base"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MascotaUsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id_mascota_usuario: Int = 0,
    val id_mascota_base: Int,
    val nombre_personalizado: String? = null,
    val nivel: Int = 1,
    val experiencia: Int = 0,
    val esta_activa: Boolean = false,        // Si es la mascota principal
    val fecha_obtencion: Long = System.currentTimeMillis(),

    // SISTEMA DE FELICIDAD
    val felicidad_actual: Int = 100,
    val ultima_interaccion: Long = System.currentTimeMillis()
)