package com.example.spire_task.data.local.entidades

/**
 * Resultado de calcular la recompensa de una tarea
 */
data class RecompensaTarea(
    val xpBase: Int,
    val xpFinal: Int,
    val monedasBase: Int,
    val monedasFinal: Int,
    val multiplicadorXp: Float,
    val multiplicadorMonedas: Float,
    val bonosAplicados: List<String>,
    val incrementoFelicidad: Int = 0
)