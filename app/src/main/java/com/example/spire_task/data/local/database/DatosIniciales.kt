package com.example.spire_task.data.local.database

import com.example.spire_task.data.local.entidades.MascotaBaseEntity

object DatosIniciales {

    const val MONEDAS_INICIALES = 1000

    val mascotasBase = listOf(
        // 🦉 BÚHO DEL ENFOQUE - Habilidad MENTORA (afecta tablero)
        MascotaBaseEntity(
            id_mascota_base = 1,
            nombre_especie = "Búho del Enfoque",
            descripcion = "Sabio guardián de la concentración",
            emoji = "🦉",
            precio_monedas = 50,
            ruta_asset_base = "pets/buho/base.png",
            ruta_asset_habitad = "pets/buho/habitad.png",
            ruta_asset_evolucion = "pets/buho/evolucion.png",
            habilidad_nombre = "Sabiduría Nocturna",
            habilidad_descripcion = "+15% XP en todas las tareas completadas",
            habilidad_tipo = "MENTORA",
            habilidad_valor = 1.15f
        ),

        // 🦊 ZORRO ASTUTO - Habilidad MENTORA (afecta tablero)
        MascotaBaseEntity(
            id_mascota_base = 2,
            nombre_especie = "Zorro Astuto",
            descripcion = "Maestro de las recompensas",
            emoji = "🦊",
            precio_monedas = 80,
            ruta_asset_base = "pets/zorro/base.png",
            ruta_asset_habitad = "pets/zorro/habitad.png",
            ruta_asset_evolucion = "pets/zorro/evolucion.png",
            habilidad_nombre = "Ojo de Zorro",
            habilidad_descripcion = "+20% monedas en todas las tareas completadas",
            habilidad_tipo = "MENTORA",
            habilidad_valor = 1.20f
        ),

        // 🐢 TORTUGA SABIA - Habilidad ACTIVA (multi-tarea)
        MascotaBaseEntity(
            id_mascota_base = 3,
            nombre_especie = "Tortuga Sabia",
            descripcion = "Guardián de la paciencia",
            emoji = "🐢",
            precio_monedas = 60,
            ruta_asset_base = "pets/tortuga/base.png",
            ruta_asset_habitad = "pets/tortuga/habitad.png",
            ruta_asset_evolucion = "pets/tortuga/evolucion.png",
            habilidad_nombre = "Paciencia Dual",
            habilidad_descripcion = "Permite tener 2 tareas en progreso simultáneamente",
            habilidad_tipo = "ACTIVA",
            habilidad_valor = 2f  // Número de tareas simultáneas
        ),

        // 🐉 DRAGÓN MOTIVADOR - Habilidad ACTIVA (protección)
        MascotaBaseEntity(
            id_mascota_base = 4,
            nombre_especie = "Dragón Motivador",
            descripcion = "Protector de tu constancia",
            emoji = "🐉",
            precio_monedas = 200,
            ruta_asset_base = "pets/dragon/base.png",
            ruta_asset_habitad = "pets/dragon/habitad.png",
            ruta_asset_evolucion = "pets/dragon/evolucion.png",
            habilidad_nombre = "Escudo del Dragón",
            habilidad_descripcion = "Protege tu racha por 1 día extra si fallas una tarea",
            habilidad_tipo = "ACTIVA",
            habilidad_valor = 1f  // 1 día extra de protección
        )
    )
}