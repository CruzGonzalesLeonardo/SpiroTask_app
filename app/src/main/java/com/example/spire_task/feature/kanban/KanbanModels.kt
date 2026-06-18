package com.example.spire_task.feature.kanban

import com.example.spire_task.data.local.entidades.SubtareaEntity
import com.example.spire_task.data.local.entidades.TableroEntity
import com.example.spire_task.data.local.entidades.TareaEntity

// ───────────────────────────────────────────────────────────
// MODELOS DE DATOS PARA LA UI
// ───────────────────────────────────────────────────────────

data class MascotaCompletaKanban(
    val idMascotaUsuario: Int,
    val nombreEspecie: String,
    val emoji: String,
    val nivel: Int,
    val felicidad: Int,
    val rutaAsset: String?
)

data class HabilidadCompleta(
    val idHabilidad: Int,
    val nombre: String,
    val descripcion: String,
    val nivelRequerido: Int,
    val tipoAplicacion: String,
    val desbloqueada: Boolean,
    val valorModificador: Float = 1.0f
)

data class KanbanUiState(
    val tablero: TableroEntity? = null,
    val tareasPorHacer: List<TareaEntity> = emptyList(),
    val tareasEnProgreso: List<TareaEntity> = emptyList(),
    val tareasFinalizadas: List<TareaEntity> = emptyList(),
    val subtareasPorTarea: Map<Int, List<SubtareaEntity>> = emptyMap(),
    val filtroActivo: String = "POR_HACER",
    val estaCargando: Boolean = true,
    val mostrarDialogoCrear: Boolean = false,
    val error: String? = null,
    val mascotaMentora: MascotaCompletaKanban? = null,
    val habilidadActiva: HabilidadCompleta? = null,
    val felicidadMascota: Int = 100,
    val colorFondo: String = "#4A90E2",
    val limitePorHacer: Int = KanbanViewModel.LIMITE_POR_HACER,
    val limiteEnProgreso: Int = KanbanViewModel.LIMITE_EN_PROGRESO
)

// ───────────────────────────────────────────────────────────
// RESULTADOS Y RECOMPENSAS
// ───────────────────────────────────────────────────────────

data class RecompensaMostrada(
    val xp: Int,
    val monedas: Int,
    val bonos: List<String>
)

sealed class ResultadoRecompensa {
    data class Exito(val recompensa: RecompensaCalculada) : ResultadoRecompensa()
    data class Error(val mensaje: String) : ResultadoRecompensa()
}

data class RecompensaCalculada(
    val xp: Int,
    val monedas: Int,
    val multiplicador: Float,
    val bonosAplicados: List<String> = emptyList()
)