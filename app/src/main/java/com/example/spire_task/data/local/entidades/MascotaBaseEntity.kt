package com.example.spire_task.data.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa el catálogo de especies de mascotas.
 * Cada mascota tiene UNA habilidad única que la hace especial.
 */
@Entity(tableName = "mascota_base")
data class MascotaBaseEntity(
    @PrimaryKey
    val id_mascota_base: Int,
    val nombre_especie: String,
    val descripcion: String,
    val emoji: String,
    val precio_monedas: Int,
    val ruta_asset_base: String,
    val ruta_asset_habitad: String? = null,
    val ruta_asset_evolucion: String? = null,

    // HABILIDAD ÚNICA DE LA MASCOTA
    val habilidad_nombre: String,           // Ej: "Sabiduría del Búho"
    val habilidad_descripcion: String,      // Ej: "+15% XP en tareas completadas"
    val habilidad_tipo: String,             // "MENTORA" (afecta tablero) o "ACTIVA" (afecta cuando es principal)
    val habilidad_valor: Float,             // 1.15 = +15%, 1.20 = +20%, etc.

    // SISTEMA DE FELICIDAD
    val felicidad_base: Int = 100,
    val decremento_felicidad_diario: Int = 5
)