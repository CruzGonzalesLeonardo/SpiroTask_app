package com.example.spire_task.feature.boards

import android.util.Log
import androidx.activity.compose.BackHandler // 🚨 NO OLVIDES EL IMPORT
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Dashboard
//import androidx.compose.material.icons.rounded.DashboardCustomization
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage

private val coloresTablero = listOf(
    "#7C3AED", "#FF6B6B", "#4ECDC4", "#FFD93D",
    "#6BCB77", "#FF8C42", "#A78BFA", "#F472B6"
)

@Composable
fun BoardsListScreen(
    onTableroClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: BoardsListViewModel = viewModel(factory = BoardsListViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.estaCargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // ─── ENCABEZADO DE LA VISTA PRINCIPAL ───────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Mis Tableros",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Gestiona tus proyectos y asignaturas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (uiState.tablerosConTareas.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(90.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Dashboard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "Sin tableros aún",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Toca el botón inferior para crear tu primer tablero guiado por una mascota.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.toggleDialogoCrear(true) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crear tablero", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
                    ) {
                        items(uiState.tablerosConTareas, key = { it.tablero.id_tablero }) { tct ->
                            TableroCard(
                                tablero = tct.tablero,
                                totalTareas = tct.totalTareas,
                                tareasCompletadas = tct.tareasCompletadas,
                                onClick = { onTableroClick(tct.tablero.id_tablero) },
                                onEliminar = { viewModel.mostrarConfirmacionEliminar(tct.tablero) }
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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Rounded.Add, "Crear tablero", modifier = Modifier.size(28.dp))
        }
    }

    if (uiState.mostrarDialogoCrear) {
        DialogoCrearTablero(
            mascotasCompletas = uiState.mascotasCompletas,
            onCrear = { n, c, m -> viewModel.crearTablero(n, c, m) },
            onCancelar = { viewModel.toggleDialogoCrear(false) }
        )
    }
    if (uiState.mostrarDialogoEliminar) {
        DialogoEliminarTablero(
            nombreTablero = uiState.tableroAEliminar?.nombre ?: "",
            onConfirmar = { viewModel.eliminarTablero() },
            onCancelar = { viewModel.ocultarDialogoEliminar() }
        )
    }
}

@Composable
private fun TableroCard(
    tablero: com.example.spire_task.data.local.entidades.TableroEntity,
    totalTareas: Int,
    tareasCompletadas: Int,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorTablero = try {
        Color(android.graphics.Color.parseColor(tablero.color_hex))
    } catch (e: Exception) { MaterialTheme.colorScheme.primary }

    val completado = totalTareas > 0 && tareasCompletadas == totalTareas

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(55.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.verticalGradient(listOf(colorTablero, colorTablero.copy(alpha = 0.6f))))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tablero.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (completado) Color(0xFF6BCB77).copy(alpha = 0.15f) else colorTablero.copy(alpha = 0.12f)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (completado) Color(0xFF6BCB77) else colorTablero
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$tareasCompletadas/$totalTareas tareas", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = if (completado) Color(0xFF6BCB77) else colorTablero)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(formatearFecha(tablero.fecha_creacion), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
            MascotaMentoraAvatar(mascotaId = tablero.id_mascota_mentora)
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onEliminar, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.DeleteOutline, "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun MascotaMentoraAvatar(mascotaId: Int) {
    var rutaAsset by remember { mutableStateOf<String?>(null) }
    var estaCargando by remember { mutableStateOf(true) }

    LaunchedEffect(mascotaId) {
        try {
            val db = com.example.spire_task.SpiroTaskApplication.instance.database
            val mascota = db.mascotaUsuarioDao().obtenerPorId(mascotaId)
            if (mascota != null) {
                val base = db.mascotaBaseDao().obtenerPorId(mascota.id_mascota_base)
                rutaAsset = base?.ruta_asset_base
                Log.d("SPIRO_DEBUG", "✅ Avatar mascota: ruta=$rutaAsset")
            }
            estaCargando = false
        } catch (e: Exception) {
            Log.e("SPIRO_DEBUG", "❌ Error cargando mascota: ${e.message}")
            estaCargando = false
        }
    }

    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                estaCargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                rutaAsset != null -> {
                    SubcomposeAsyncImage(
                        model = "file:///android_asset/$rutaAsset",
                        contentDescription = "Mascota mentora",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            }
                        },
                        error = {
                            Icon(Icons.Rounded.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    )
                }
                else -> {
                    Icon(Icons.Rounded.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DialogoCrearTablero(
    mascotasCompletas: List<MascotaCompleta>,
    onCrear: (String, String, Int) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var colorSel by remember { mutableStateOf("#7C3AED") }
    var mascotaSel by remember { mutableIntStateOf(mascotasCompletas.firstOrNull()?.mascotaUsuario?.id_mascota_usuario ?: 0) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.Dashboard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                "Nuevo Tablero",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; error = null },
                    label = { Text("Nombre del tablero") },
                    placeholder = { Text("Ej: Proyecto Alfa o Historia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    isError = error != null,
                    supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.ColorLens, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Text("Tema de color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        coloresTablero.forEach { hex ->
                            val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
                            val sel = colorSel == hex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { colorSel = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (sel) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Pets, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Text("Asignar Mascota Mentora", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (mascotasCompletas.isEmpty()) {
                        Text("No tienes mascotas disponibles", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            mascotasCompletas.forEach { mc ->
                                val sel = mascotaSel == mc.mascotaUsuario.id_mascota_usuario
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { mascotaSel = mc.mascotaUsuario.id_mascota_usuario },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (sel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = if (sel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(38.dp),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (mc.rutaAsset != null) {
                                                    SubcomposeAsyncImage(
                                                        model = "file:///android_asset/${mc.rutaAsset}",
                                                        contentDescription = mc.nombreEspecie,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape),
                                                        contentScale = ContentScale.Crop,
                                                        error = { Icon(Icons.Rounded.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                                                    )
                                                } else {
                                                    Icon(Icons.Rounded.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(mc.nombreEspecie, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Nivel ${mc.mascotaUsuario.nivel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        RadioButton(
                                            selected = sel,
                                            onClick = { mascotaSel = mc.mascotaUsuario.id_mascota_usuario },
                                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        nombre.isBlank() -> error = "Ingresa un nombre"
                        mascotaSel == 0 -> error = "Selecciona una mascota"
                        else -> onCrear(nombre, colorSel, mascotaSel)
                    }
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Crear Tablero", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DialogoEliminarTablero(
    nombreTablero: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        title = { Text("¿Eliminar tablero?", fontWeight = FontWeight.Bold) },
        text = { Text("Se borrará permanentemente \"$nombreTablero\" junto con todas las tareas asociadas a él.") },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatearFecha(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}