package com.example.spire_task.data.repository

import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import com.example.spire_task.data.local.entidades.MascotaUsuarioEntity
import com.example.spire_task.data.local.entidades.RecompensaTarea
import com.example.spire_task.data.local.entidades.TareaEntity
import java.util.concurrent.TimeUnit

class RecompensaService {

    companion object {
        // Valores base de recompensa
        private const val XP_BASE_POR_TAREA = 50
        private const val MONEDAS_BASE_POR_TAREA = 10

        // Bonos por prioridad
        private const val BONO_PRIORIDAD_BAJA = 1.0f
        private const val BONO_PRIORIDAD_MEDIA = 1.25f
        private const val BONO_PRIORIDAD_ALTA = 1.5f

        // Bonos por entrega anticipada
        private const val BONO_ENTREGA_ANTICIPADA_3D = 1.5f  // +50% si entrega 3+ días antes
        private const val BONO_ENTREGA_ANTICIPADA_1D = 1.25f // +25% si entrega 1 día antes
        private const val BONO_ENTREGA_HOY = 1.5f             // +50% si vence hoy
    }

    /**
     * Calcula la recompensa de una tarea basada en:
     * - Prioridad de la tarea
     * - Fecha de entrega (si aplica)
     * - Habilidades de la mascota mentora
     */
    // En RecompensaService.kt - Agrega este método o modifica el existente

    /**
     * Calcula la recompensa de una tarea con porcentaje de subtareas
     */
    suspend fun calcularRecompensa(
        tarea: TareaEntity,
        habilidadMascota: MascotaBaseEntity? = null,
        esMascotaActiva: Boolean = false,
        porcentajeSubtareas: Float = 1.0f
    ): RecompensaTarea {

        // 1. Calcular multiplicador por prioridad
        val multiplicadorPrioridad = when (tarea.prioridad) {
            3 -> BONO_PRIORIDAD_ALTA
            2 -> BONO_PRIORIDAD_MEDIA
            else -> BONO_PRIORIDAD_BAJA
        }

        // 2. Calcular multiplicador por entrega anticipada
        val multiplicadorEntrega = calcularMultiplicadorEntrega(tarea)

        // 3. Calcular multiplicador por subtareas
        // Si todas las subtareas están completadas, multiplicador = 1.0
        // Si no, se reduce proporcionalmente (máximo 30% de penalización)
        val multiplicadorSubtareas = 0.7f + (porcentajeSubtareas * 0.3f)

        // 4. Calcular multiplicadores base
        val multiplicadorBase = multiplicadorPrioridad * multiplicadorEntrega * multiplicadorSubtareas
        val xpBase = (XP_BASE_POR_TAREA * multiplicadorBase).toInt()
        val monedasBase = (MONEDAS_BASE_POR_TAREA * multiplicadorBase).toInt()

        // 5. Aplicar habilidades de mascota
        val bonosAplicados = mutableListOf<String>()
        var multiplicadorXp = 1.0f
        var multiplicadorMonedas = 1.0f

        // Bono por subtareas
        if (porcentajeSubtareas < 1.0f && porcentajeSubtareas > 0.7f) {
            val penalizacion = ((1 - multiplicadorSubtareas) * 100).toInt()
            if (penalizacion > 0) {
                bonosAplicados.add("⚠️ Subtareas incompletas: -$penalizacion%")
            }
        } else if (porcentajeSubtareas == 1.0f && multiplicadorSubtareas == 1.0f) {
            bonosAplicados.add("✅ Todas las subtareas completadas: +0%")
        }

        // Bonos por prioridad
        when (tarea.prioridad) {
            3 -> bonosAplicados.add("🔴 Prioridad Alta: +50%")
            2 -> bonosAplicados.add("🟡 Prioridad Media: +25%")
        }

        // Bonos por entrega
        val diferenciaDias = tarea.fecha_limite?.let {
            calcularDiasDiferencia(it, tarea.fecha_completado ?: System.currentTimeMillis())
        }
        when {
            diferenciaDias != null && diferenciaDias >= 3 -> bonosAplicados.add("📅 Entrega anticipada (3+ días): +50%")
            diferenciaDias != null && diferenciaDias >= 1 -> bonosAplicados.add("📅 Entrega anticipada (1 día): +25%")
            diferenciaDias != null && diferenciaDias == 0L -> bonosAplicados.add("📅 Entrega a tiempo: +0%")
            diferenciaDias != null && diferenciaDias < 0 -> bonosAplicados.add("⚠️ Entrega tardía: sin bono")
        }

        if (habilidadMascota != null) {
            when (habilidadMascota.habilidad_tipo) {
                "MENTORA" -> {
                    if (habilidadMascota.habilidad_nombre.contains("XP", ignoreCase = true) ||
                        habilidadMascota.habilidad_descripcion.contains("XP", ignoreCase = true)) {
                        multiplicadorXp = habilidadMascota.habilidad_valor
                        bonosAplicados.add("✨ ${habilidadMascota.habilidad_nombre}: +${((habilidadMascota.habilidad_valor - 1) * 100).toInt()}% XP")
                    }
                    if (habilidadMascota.habilidad_nombre.contains("monedas", ignoreCase = true) ||
                        habilidadMascota.habilidad_descripcion.contains("monedas", ignoreCase = true)) {
                        multiplicadorMonedas = habilidadMascota.habilidad_valor
                        bonosAplicados.add("💰 ${habilidadMascota.habilidad_nombre}: +${((habilidadMascota.habilidad_valor - 1) * 100).toInt()}% monedas")
                    }
                }
                "ACTIVA" -> {
                    if (esMascotaActiva) {
                        when {
                            habilidadMascota.habilidad_nombre.contains("XP", ignoreCase = true) -> {
                                multiplicadorXp = habilidadMascota.habilidad_valor
                                bonosAplicados.add("✨ ${habilidadMascota.habilidad_nombre}: +${((habilidadMascota.habilidad_valor - 1) * 100).toInt()}% XP")
                            }
                            habilidadMascota.habilidad_nombre.contains("monedas", ignoreCase = true) -> {
                                multiplicadorMonedas = habilidadMascota.habilidad_valor
                                bonosAplicados.add("💰 ${habilidadMascota.habilidad_nombre}: +${((habilidadMascota.habilidad_valor - 1) * 100).toInt()}% monedas")
                            }
                        }
                    }
                }
            }
        }

        // Calcular valores finales
        val xpFinal = (xpBase * multiplicadorXp).toInt().coerceAtLeast(1)
        val monedasFinal = (monedasBase * multiplicadorMonedas).toInt().coerceAtLeast(1)

        return RecompensaTarea(
            xpBase = xpBase,
            xpFinal = xpFinal,
            monedasBase = monedasBase,
            monedasFinal = monedasFinal,
            multiplicadorXp = multiplicadorXp,
            multiplicadorMonedas = multiplicadorMonedas,
            bonosAplicados = bonosAplicados
        )
    }

    private fun calcularDiasDiferencia(fechaLimite: Long, fechaCompletado: Long): Long {
        return TimeUnit.MILLISECONDS.toDays(fechaLimite - fechaCompletado)
    }

    /**
     * Calcula el multiplicador por entrega anticipada o a tiempo
     */
    private fun calcularMultiplicadorEntrega(tarea: TareaEntity): Float {
        val fechaLimite = tarea.fecha_limite ?: return 1.0f
        val fechaCompletado = tarea.fecha_completado ?: return 1.0f

        val diferenciaDias = TimeUnit.MILLISECONDS.toDays(fechaLimite - fechaCompletado)

        return when {
            diferenciaDias >= 3 -> BONO_ENTREGA_ANTICIPADA_3D  // +50% si entrega 3+ días antes
            diferenciaDias >= 1 -> BONO_ENTREGA_ANTICIPADA_1D  // +25% si entrega 1 día antes
            diferenciaDias == 0L -> BONO_ENTREGA_HOY          // +50% si entrega justo hoy
            diferenciaDias < 0 -> {
                // Tarea entregada tarde, sin bono
                1.0f
            }
            else -> 1.0f
        }
    }

    /**
     * Calcula el bono de racha (días consecutivos completando tareas)
     */
    fun calcularBonoRacha(diasRacha: Int): Float {
        return when {
            diasRacha >= 30 -> 2.0f  // +100% después de 30 días
            diasRacha >= 14 -> 1.5f  // +50% después de 14 días
            diasRacha >= 7 -> 1.25f  // +25% después de 7 días
            diasRacha >= 3 -> 1.1f   // +10% después de 3 días
            else -> 1.0f
        }
    }
}