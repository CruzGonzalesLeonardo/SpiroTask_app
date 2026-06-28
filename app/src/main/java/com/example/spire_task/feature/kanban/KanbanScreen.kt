package com.example.spire_task.feature.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler // 🚨 ASEGÚRATE DE AGREGAR ESTE IMPORT ARRIBA
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.SpiroTaskApplication
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.TareaEntity
import com.example.spire_task.feature.kanban.components.DialogoConfirmarAvanzar
import com.example.spire_task.feature.kanban.components.DialogoConfirmarRetroceder
import com.example.spire_task.feature.kanban.components.DialogoCrearTarea
import com.example.spire_task.feature.kanban.components.DialogoRecompensa
import com.example.spire_task.feature.kanban.components.EstadoVacioKanban
import com.example.spire_task.feature.kanban.components.KanbanHeader
import com.example.spire_task.feature.kanban.components.LimitesBar
import com.example.spire_task.feature.kanban.components.TareaKanbanCard
import com.example.spire_task.feature.taskdetail.TaskDetailScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanScreen(
    tableroId: Int,
    onVolver: () -> Unit = {},
    viewModel: KanbanViewModel = viewModel(factory = KanbanViewModelFactory(tableroId))
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var tareaSeleccionadaId by remember { mutableStateOf<Int?>(null) }

    var tareaAAvanzar by remember { mutableStateOf<TareaEntity?>(null) }
    var tareaARetroceder by remember { mutableStateOf<TareaEntity?>(null) }
    var mostrarRecompensa by remember { mutableStateOf<RecompensaMostrada?>(null) }

    if (tareaSeleccionadaId != null) {
        BackHandler {
            tareaSeleccionadaId = null
        }

        TaskDetailScreen(
            tareaId = tareaSeleccionadaId!!,
            onVolver = { tareaSeleccionadaId = null }
        )
        return
    }

    // 🐢 LÓGICA DE CONDICIONAL PARA LA HABILIDAD DE LA TORTUGA
    // Si la especie es una tortuga (validando por nombre o por su emoji característico), expandimos el límite
    val limiteEnProgresoDinamico = if (
        uiState.mascotaMentora?.nombreEspecie?.contains("Tortuga", ignoreCase = true) == true ||
        uiState.mascotaMentora?.emoji == "🐢"
    ) {
        uiState.limiteEnProgreso + 2 // Aumenta +2 la capacidad si es la tortuga
    } else {
        uiState.limiteEnProgreso
    }

    val colorFondo = try {
        Color(android.graphics.Color.parseColor(uiState.colorFondo))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.background
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorFondo.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            KanbanHeader(
                tableroNombre = uiState.tablero?.nombre ?: "Tablero",
                onVolver = onVolver,
                mascota = uiState.mascotaMentora,
                habilidad = uiState.habilidadActiva,
                felicidad = uiState.felicidadMascota
            )

            // ✅ MODIFICADO: Ahora los chips reciben los límites usando la variable calculada dinámicamente
            KanbanFilterChips(
                filtroActivo = uiState.filtroActivo,
                onFiltroChange = viewModel::cambiarFiltro,
                conteos = mapOf(
                    "POR_HACER" to uiState.tareasPorHacer.size,
                    "EN_PROGRESO" to uiState.tareasEnProgreso.size,
                    "FINALIZADO" to uiState.tareasFinalizadas.size
                ),
                limites = mapOf(
                    "POR_HACER" to uiState.limitePorHacer,
                    "EN_PROGRESO" to limiteEnProgresoDinamico // 🟢 Cambiado aquí
                )
            )

            LimitesBar(
                limitePorHacer = uiState.limitePorHacer,
                actualPorHacer = uiState.tareasPorHacer.size,
                limiteEnProgreso = limiteEnProgresoDinamico, // 🟢 Cambiado aquí
                actualEnProgreso = uiState.tareasEnProgreso.size
            )

            uiState.error?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                LaunchedEffect(error) { viewModel.limpiarError() }
            }

            val tareasMostradas = when (uiState.filtroActivo) {
                "POR_HACER" -> uiState.tareasPorHacer
                "EN_PROGRESO" -> uiState.tareasEnProgreso
                "FINALIZADO" -> uiState.tareasFinalizadas
                else -> emptyList()
            }

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.estaCargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (tareasMostradas.isEmpty()) {
                    EstadoVacioKanban()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                    ) {
                        items(tareasMostradas, key = { it.id_tarea }) { tarea ->
                            TareaKanbanCard(
                                tarea = tarea,
                                onClick = { tareaSeleccionadaId = tarea.id_tarea },
                                onAvanzar = { tareaAAvanzar = tarea },
                                onRetroceder = if (tarea.estado != "POR_HACER") {
                                    { tareaARetroceder = tarea }
                                } else null,
                                onEliminar = { viewModel.eliminarTarea(tarea) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.toggleDialogoCrear(true) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(Icons.Rounded.Add, "Nueva tarea", modifier = Modifier.size(28.dp))
        }
    }

    if (uiState.mostrarDialogoCrear) {
        DialogoCrearTarea(
            onCrear = { t, p, f -> viewModel.crearTarea(t, p, f) },
            onCancelar = { viewModel.toggleDialogoCrear(false) }
        )
    }

    tareaAAvanzar?.let { tarea ->
        DialogoConfirmarAvanzar(
            tarea = tarea,
            onConfirmar = {
                if (tarea.estado == "EN_PROGRESO") {
                    scope.launch {
                        val resultado = viewModel.completarTareaYReclamar(tarea)
                        when (resultado) {
                            is ResultadoRecompensa.Exito -> {
                                mostrarRecompensa = RecompensaMostrada(
                                    xp = resultado.recompensa.xp,
                                    monedas = resultado.recompensa.monedas,
                                    bonos = resultado.recompensa.bonosAplicados
                                )
                            }
                            is ResultadoRecompensa.Error -> {
                                viewModel.setError(resultado.mensaje)
                            }
                        }
                        tareaAAvanzar = null
                    }
                } else {
                    // Nota: Si cambias el estado a EN_PROGRESO desde la UI, asegúrate de que tu
                    // KanbanViewModel.cambiarEstadoTarea() use este mismo límite dinámico para validar los bloqueos por límite.
                    viewModel.cambiarEstadoTarea(tarea, "EN_PROGRESO")
                    tareaAAvanzar = null
                }
            },
            onCancelar = { tareaAAvanzar = null }
        )
    }

    tareaARetroceder?.let { tarea ->
        DialogoConfirmarRetroceder(
            tarea = tarea,
            onConfirmar = {
                val nuevoEstado = when (tarea.estado) {
                    "EN_PROGRESO" -> "POR_HACER"
                    "FINALIZADO" -> "EN_PROGRESO"
                    else -> return@DialogoConfirmarRetroceder
                }
                viewModel.cambiarEstadoTarea(tarea, nuevoEstado)
                tareaARetroceder = null
            },
            onCancelar = { tareaARetroceder = null }
        )
    }

    mostrarRecompensa?.let { recompensa ->
        DialogoRecompensa(
            recompensa = recompensa,
            mascota = uiState.mascotaMentora,
            onCerrar = { mostrarRecompensa = null }
        )
    }
}

/**
 * 🛠️ COMPONENTE INTERNO DE FILTROS ACTUALIZADO
 * Muestra dinámicamente el contador junto a su límite: (Actual/Límite)
 */
@Composable
fun KanbanFilterChips(
    filtroActivo: String,
    onFiltroChange: (String) -> Unit,
    conteos: Map<String, Int>,
    limites: Map<String, Int>
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val estados = listOf("POR_HACER", "EN_PROGRESO", "FINALIZADO")

        estados.forEach { estado ->
            val actual = conteos[estado] ?: 0
            val limite = limites[estado]

            // Si el estado tiene un límite definido, muestra "Actual/Límite", si no, solo "Actual"
            val textoConteo = if (limite != null) "$actual/$limite" else "$actual"

            val etiqueta = when (estado) {
                "POR_HACER" -> "Por Hacer ($textoConteo)"
                "EN_PROGRESO" -> "En Progreso ($textoConteo)"
                "FINALIZADO" -> "Finalizado ($textoConteo)"
                else -> ""
            }

            FilterChip(
                selected = filtroActivo == estado,
                onClick = { onFiltroChange(estado) },
                label = { Text(etiqueta, fontSize = 12.sp) }
            )
        }
    }
}

/**
 * 📊 UTILIDADES DE KANBAN (Unificadas aquí para reducir archivos)
 */
object KanbanUtils {
    fun formatearFecha(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatearFechaCompleta(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd 'de' MMMM", Locale("es"))
        return sdf.format(Date(timestamp))
    }

    fun calcularDiasRestantes(fechaLimite: Long): Long {
        val ahora = System.currentTimeMillis()
        val diff = fechaLimite - ahora
        return TimeUnit.MILLISECONDS.toDays(diff)
    }
}

@Composable
fun Int.getColorPrioridad(): Color {
    return when (this) {
        3 -> Color(0xFFDC2626) // Alta (Rojo)
        2 -> Color(0xFFD97706) // Media (Ámbar)
        else -> Color(0xFF2563EB) // Baja (Azul)
    }
}