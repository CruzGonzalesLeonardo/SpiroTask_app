package com.example.spire_task.feature.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.data.local.entidades.TareaEntity
import com.example.spire_task.feature.taskdetail.TaskDetailScreen
import com.example.spire_task.feature.taskdetail.TaskDetailUiState
import kotlinx.coroutines.launch

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
        TaskDetailScreen(tareaId = tareaSeleccionadaId!!, onVolver = { tareaSeleccionadaId = null })
        return
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

            KanbanFilterChips(
                filtroActivo = uiState.filtroActivo,
                onFiltroChange = viewModel::cambiarFiltro,
                conteos = mapOf(
                    "POR_HACER" to uiState.tareasPorHacer.size,
                    "EN_PROGRESO" to uiState.tareasEnProgreso.size,
                    "FINALIZADO" to uiState.tareasFinalizadas.size
                )
            )

            LimitesBar(
                limitePorHacer = uiState.limitePorHacer,
                actualPorHacer = uiState.tareasPorHacer.size,
                limiteEnProgreso = uiState.limiteEnProgreso,
                actualEnProgreso = uiState.tareasEnProgreso.size
            )

            uiState.error?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
                                subtareas = uiState.subtareasPorTarea[tarea.id_tarea] ?: emptyList(),
                                onClick = { tareaSeleccionadaId = tarea.id_tarea },
                                onAvanzar = { tareaAAvanzar = tarea },
                                onRetroceder = if (tarea.estado != "POR_HACER") {
                                    { tareaARetroceder = tarea }
                                } else null,
                                onEliminar = { viewModel.eliminarTarea(tarea) }
                                //rutaHabitad = uiState.mascotaMentora?.rutaAsset
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
            Icon(Icons.Default.Add, "Nueva tarea", modifier = Modifier.size(28.dp))
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
                        // Mostrar loading si es necesario
                        val resultado = viewModel.completarTareaYReclamar(tarea)
                        when (resultado) {
                            is ResultadoRecompensa.Exito -> {
                                mostrarRecompensa = RecompensaMostrada(
                                    xp = resultado.recompensa.xp,
                                    monedas = resultado.recompensa.monedas,
                                    bonos = resultado.recompensa.bonosAplicados
                                )
                                // No llamar a cambiarEstadoTarea nuevamente porque ya se hizo en completarTareaYReclamar
                            }
                            is ResultadoRecompensa.Error -> {
                                viewModel.setError(resultado.mensaje)
                            }
                        }
                        tareaAAvanzar = null
                    }
                } else {
                    // Para mover de POR_HACER a EN_PROGRESO
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
            onCerrar = { mostrarRecompensa = null }
        )
    }
}