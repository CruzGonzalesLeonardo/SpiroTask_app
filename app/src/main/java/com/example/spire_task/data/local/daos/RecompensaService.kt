package com.example.spire_task.data.repository

import android.content.Context
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import com.example.spire_task.data.local.entidades.RecompensaTarea
import com.example.spire_task.data.local.entidades.TareaEntity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecompensaService(private val context: Context) {

    companion object {
        private const val XP_BASE_POR_TAREA = 50
        private const val MONEDAS_BASE_POR_TAREA = 10

        private const val BONO_PRIORIDAD_BAJA = 1.0f
        private const val BONO_PRIORIDAD_MEDIA = 1.25f
        private const val BONO_PRIORIDAD_ALTA = 1.5f

        private const val BONO_ENTREGA_ANTICIPADA_3D = 1.5f
        private const val BONO_ENTREGA_ANTICIPADA_1D = 1.25f
        private const val BONO_ENTREGA_HOY = 1.5f

        // 🔒 Límite diario de reclamos
        private const val LIMITE_DIARIO_RECOMPENSAS = 6
        private const val PREFS_NAME = "spiro_task_rewards_prefs"
        private const val KEY_RECOMPENSAS_HOY = "recompensas_reclamadas_hoy"
        private const val KEY_ULTIMA_FECHA = "ultima_fecha_reclamo"
    }

    private fun verificarYRegistrarLimiteDiario(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val hoyString = sdf.format(Date())

        val ultimaFecha = prefs.getString(KEY_ULTIMA_FECHA, "")
        var reclamadasHoy = prefs.getInt(KEY_RECOMPENSAS_HOY, 0)

        if (ultimaFecha != hoyString) {
            reclamadasHoy = 0
            prefs.edit().putString(KEY_ULTIMA_FECHA, hoyString).apply()
        }

        if (reclamadasHoy >= LIMITE_DIARIO_RECOMPENSAS) {
            return false
        }

        prefs.edit().putInt(KEY_RECOMPENSAS_HOY, reclamadasHoy + 1).apply()
        return true
    }

    fun obtenerDisponiblesHoy(): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val hoyString = sdf.format(Date())

        if (prefs.getString(KEY_ULTIMA_FECHA, "") != hoyString) return LIMITE_DIARIO_RECOMPENSAS
        val reclamadas = prefs.getInt(KEY_RECOMPENSAS_HOY, 0)
        return (LIMITE_DIARIO_RECOMPENSAS - reclamadas).coerceAtLeast(0)
    }

    /**
     * Calcula la recompensa de una tarea e incluye el incremento de felicidad.
     */
    suspend fun calcularRecompensa(
        tarea: TareaEntity,
        habilidadMascota: MascotaBaseEntity? = null,
        esMascotaActiva: Boolean = false
    ): RecompensaTarea {

        val puedeReclamar = verificarYRegistrarLimiteDiario()

        if (!puedeReclamar) {
            return RecompensaTarea(
                xpBase = 0, xpFinal = 0,
                monedasBase = 0, monedasFinal = 0,
                multiplicadorXp = 0f, multiplicadorMonedas = 0f,
                bonosAplicados = listOf("🚫 LÍMITE ALCANZADO: Has reclamado tus 6 recompensas de hoy. ¡Vuelve mañana!"),
                incrementoFelicidad = 0
            )
        }

        val multiplicadorPrioridad = when (tarea.prioridad) {
            3 -> BONO_PRIORIDAD_ALTA
            2 -> BONO_PRIORIDAD_MEDIA
            else -> BONO_PRIORIDAD_BAJA
        }

        val multiplicadorEntrega = calcularMultiplicadorEntrega(tarea)

        val multiplicadorBase = multiplicadorPrioridad * multiplicadorEntrega
        val xpBase = (XP_BASE_POR_TAREA * multiplicadorBase).toInt()
        val monedasBase = (MONEDAS_BASE_POR_TAREA * multiplicadorBase).toInt()

        val bonosAplicados = mutableListOf<String>()
        var multiplicadorXp = 1.0f
        var multiplicadorMonedas = 1.0f

        // 📈 ✅ NUEVO: Determinar el incremento de felicidad por nivel de tarea
        val incrementoFelicidad = when (tarea.prioridad) {
            3 -> 30 // Alta
            2 -> 20 // Media
            else -> 10 // Baja
        }

        // Agregar desglose visual en los bonos
        when (tarea.prioridad) {
            3 -> {
                bonosAplicados.add("🔴 Prioridad Alta: +50%")
                bonosAplicados.add("❤️ Vínculo fortalecido: +30 Felicidad")
            }
            2 -> {
                bonosAplicados.add("🟡 Prioridad Media: +25%")
                bonosAplicados.add("❤️ Vínculo fortalecido: +20 Felicidad")
            }
            else -> {
                bonosAplicados.add("❤️ Vínculo fortalecido: +10 Felicidad")
            }
        }

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
                        bonosAplicados.add("⭐ ${habilidadMascota.habilidad_nombre}: +${((habilidadMascota.habilidad_valor - 1) * 100).toInt()}% XP")
                    }
                    if (habilidadMascota.habilidad_nombre.contains("monedas", ignoreCase = true) ||
                        habilidadMascota.habilidad_descripcion.contains("monedas", ignoreCase = true)) {
                        multiplicadorMonedas = habilidadMascota.habilidad_valor
                        bonosAplicados.add("🪙 ${habilidadMascota.habilidad_nombre}: +${((habilidadMascota.habilidad_valor - 1) * 100).toInt()}% monedas")
                    }
                }
                "ACTIVA" -> {
                    if (esMascotaActiva) {
                        when {
                            habilidadMascota.habilidad_nombre.contains("XP", ignoreCase = true) -> {
                                multiplicadorXp = habilidadMascota.habilidad_valor
                                bonosAplicados.add("⭐ ${habilidadMascota.habilidad_nombre}: +${((habilidadMascota.habilidad_valor - 1) * 100).toInt()}% XP")
                            }
                            habilidadMascota.habilidad_nombre.contains("monedas", ignoreCase = true) -> {
                                multiplicadorMonedas = habilidadMascota.habilidad_valor
                                bonosAplicados.add("🪙 ${habilidadMascota.habilidad_nombre}: +${((habilidadMascota.habilidad_valor - 1) * 100).toInt()}% monedas")
                            }
                        }
                    }
                }
            }
        }

        val xpFinal = (xpBase * multiplicadorXp).toInt().coerceAtLeast(1)
        val monedasFinal = (monedasBase * multiplicadorMonedas).toInt().coerceAtLeast(1)

        val restantes = obtenerDisponiblesHoy()
        bonosAplicados.add("🔄 Recompensas restantes hoy: $restantes")

        return RecompensaTarea(
            xpBase = xpBase, xpFinal = xpFinal,
            monedasBase = monedasBase, monedasFinal = monedasFinal,
            multiplicadorXp = multiplicadorXp, multiplicadorMonedas = multiplicadorMonedas,
            bonosAplicados = bonosAplicados,
            incrementoFelicidad = incrementoFelicidad // <-- ASIGNACIÓN
        )
    }

    private fun calcularDiasDiferencia(fechaLimite: Long, fechaCompletado: Long): Long {
        return TimeUnit.MILLISECONDS.toDays(fechaLimite - fechaCompletado)
    }

    private fun calcularMultiplicadorEntrega(tarea: TareaEntity): Float {
        val fechaLimite = tarea.fecha_limite ?: return 1.0f
        val fechaCompletado = tarea.fecha_completado ?: return 1.0f
        val diferenciaDias = TimeUnit.MILLISECONDS.toDays(fechaLimite - fechaCompletado)

        return when {
            diferenciaDias >= 3 -> BONO_ENTREGA_ANTICIPADA_3D
            diferenciaDias >= 1 -> BONO_ENTREGA_ANTICIPADA_1D
            diferenciaDias == 0L -> BONO_ENTREGA_HOY
            else -> 1.0f
        }
    }

    fun calcularBonoRacha(diasRacha: Int): Float {
        return when {
            diasRacha >= 30 -> 2.0f
            diasRacha >= 14 -> 1.5f
            diasRacha >= 7 -> 1.25f
            diasRacha >= 3 -> 1.1f
            else -> 1.0f
        }
    }
}