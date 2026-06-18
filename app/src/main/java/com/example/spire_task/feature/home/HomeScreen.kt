package com.example.spire_task.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.data.local.entidades.TareaEntity
import com.example.spire_task.feature.boards.BoardsListScreen
import com.example.spire_task.feature.components.*
import com.example.spire_task.feature.kanban.KanbanScreen
import com.example.spire_task.feature.kanban.KanbanViewModel
import com.example.spire_task.feature.kanban.KanbanViewModelFactory
import com.example.spire_task.feature.settings.SettingsScreen
import com.example.spire_task.feature.shop.ShopScreen

sealed class BottomNavDestino(
    val ruta: String, val titulo: String,
    val iconoSeleccionado: ImageVector, val iconoNoSeleccionado: ImageVector
) {
    data object Inicio : BottomNavDestino("home", "Inicio", Icons.Filled.Home, Icons.Outlined.Home)
    data object Tableros : BottomNavDestino("boards", "Tableros", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    data object Tienda : BottomNavDestino("shop", "Tienda", Icons.Filled.Store, Icons.Outlined.Store)
    data object Ajustes : BottomNavDestino("settings", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val destinosBottomNav = listOf(
    BottomNavDestino.Inicio, BottomNavDestino.Tableros,
    BottomNavDestino.Tienda, BottomNavDestino.Ajustes
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSesionCerrada: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory())
) {
    var kanbanKey by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()
    var destinoSeleccionado by remember { mutableStateOf<BottomNavDestino>(BottomNavDestino.Inicio) }
    var tableroAbiertoId by remember { mutableStateOf<Int?>(null) }

    // ─── KANBAN A PANTALLA COMPLETA ────────────────
    if (tableroAbiertoId != null) {
        key(tableroAbiertoId, kanbanKey) {
            val viewModel: KanbanViewModel = viewModel(
                key = "kanban_${tableroAbiertoId}_$kanbanKey",
                factory = KanbanViewModelFactory(tableroAbiertoId!!)
            )
            KanbanScreen(
                tableroId = tableroAbiertoId!!,
                onVolver = {
                    tableroAbiertoId = null
                    kanbanKey++
                },
                viewModel = viewModel
            )
        }
        return
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                nombre = uiState.perfil?.nombre,
                monedas = uiState.perfil?.monedas,
                estaCargando = uiState.estaCargando
            )
        },
        bottomBar = {
            SpiroBottomBar(
                destinoSeleccionado = destinoSeleccionado,
                onDestinoSeleccionado = { destinoSeleccionado = it }
            )
        },
        containerColor = Color.Transparent  // ✅ Transparente para ver el hábitat
    ) { paddingValues ->
        if (uiState.estaCargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            ContenidoDestino(
                destino = destinoSeleccionado,
                uiState = uiState,
                onTableroClick = { id ->
                    tableroAbiertoId = id
                    kanbanKey++
                },
                onCambiarPestana = { destinoSeleccionado = it },
                onSesionCerrada = onSesionCerrada,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(nombre: String?, monedas: Int?, estaCargando: Boolean) {
    TopAppBar(
        title = {
            Text(
                text = if (estaCargando) "¡Hola!" else "¡Hola, ${nombre ?: ""}!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        actions = {
            if (!estaCargando && monedas != null) MonedasBadge(monedas)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        )
    )
}

@Composable
private fun MonedasBadge(monedas: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🪙", fontSize = 16.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                "$monedas",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun SpiroBottomBar(
    destinoSeleccionado: BottomNavDestino,
    onDestinoSeleccionado: (BottomNavDestino) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        destinosBottomNav.forEach { destino ->
            val sel = destinoSeleccionado == destino
            NavigationBarItem(
                selected = sel,
                onClick = { onDestinoSeleccionado(destino) },
                icon = {
                    Icon(
                        imageVector = if (sel) destino.iconoSeleccionado else destino.iconoNoSeleccionado,
                        contentDescription = destino.titulo
                    )
                },
                label = {
                    Text(
                        destino.titulo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun ContenidoDestino(
    destino: BottomNavDestino,
    uiState: HomeUiState,
    onTableroClick: (Int) -> Unit,
    onCambiarPestana: (BottomNavDestino) -> Unit,
    onSesionCerrada: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (destino) {
        is BottomNavDestino.Inicio -> ContenidoInicio(
            uiState = uiState,
            onCambiarPestana = onCambiarPestana,
            modifier = modifier
        )
        is BottomNavDestino.Tableros -> BoardsListScreen(
            onTableroClick = onTableroClick,
            modifier = modifier
        )
        is BottomNavDestino.Tienda -> ShopScreen(modifier = modifier)
        is BottomNavDestino.Ajustes -> SettingsScreen(
            onSesionCerrada = onSesionCerrada,
            modifier = modifier
        )
    }
}

@Composable
private fun ContenidoInicio(
    uiState: HomeUiState,
    onCambiarPestana: (BottomNavDestino) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MascotaHomeCard(
            emoji = uiState.emojiMascota,
            nombreEspecie = uiState.nombreMascota,
            nivel = uiState.mascotaActiva?.nivel ?: 1,
            experiencia = uiState.mascotaActiva?.experiencia ?: 0,
            felicidad = uiState.felicidadMascota,
            nombrePersonalizado = uiState.mascotaActiva?.nombre_personalizado,
            rutaAsset = uiState.rutaAssetMascota,
            rutaHabitad = uiState.rutaHabitadMascota  // ✅ PASAR EL HÁBITAT
        )

        Text(
            "Hoy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        EstadisticasCard(
            tareasCompletadas = uiState.tareasCompletadasHoy,
            tareasTotales = uiState.tareasTotalesHoy,
            minutosEnfocado = uiState.minutosEnfocadoHoy,
            rachaDias = uiState.rachaDias,
            iconoTareas = Icons.Default.CheckCircle,
            iconoEnfoque = Icons.Default.Timer,
            iconoRacha = Icons.Default.LocalFireDepartment
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccionRapidaButton(
                icono = Icons.Default.Dashboard,
                texto = "Mis Tableros",
                modifier = Modifier.weight(1f),
                onClick = { onCambiarPestana(BottomNavDestino.Tableros) }
            )
            AccionRapidaButton(
                icono = Icons.Default.Store,
                texto = "Tienda",
                modifier = Modifier.weight(1f),
                esPrimario = true,
                onClick = { onCambiarPestana(BottomNavDestino.Tienda) }
            )
        }

        Text(
            "Tareas pendientes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (uiState.tareasPendientes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📋", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No tienes tareas aún",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Crea tu primer tablero para empezar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            uiState.tareasPendientes.take(5).forEach { tarea ->
                TareaPendienteCard(
                    tarea = tarea,
                    onClick = { onCambiarPestana(BottomNavDestino.Tableros) }
                )
            }
            if (uiState.tareasPendientes.size > 5) {
                Text(
                    "Y ${uiState.tareasPendientes.size - 5} más...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TareaPendienteCard(tarea: TareaEntity, onClick: () -> Unit) {
    val colorPrioridad = when (tarea.prioridad) {
        3 -> Color(0xFFFF4757)
        2 -> Color(0xFFFFA502)
        else -> Color(0xFF2ED573)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(colorPrioridad, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tarea.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                if (tarea.fecha_limite != null) {
                    Text(
                        "Vence: ${formatearFecha(tarea.fecha_limite)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatearFecha(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}