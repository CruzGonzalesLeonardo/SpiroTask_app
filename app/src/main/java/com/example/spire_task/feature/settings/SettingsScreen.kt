package com.example.spire_task.feature.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSesionCerrada: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mostrarConfirmarActivacion by remember { mutableStateOf<Int?>(null) }
    var mostrarDialogoConfirmarSubida by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmarDescarga by remember { mutableStateOf(false) }
    var mostrarEstadisticas by remember { mutableStateOf(false) }


    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            scope.launch {
                viewModel.handleGoogleSignInResult(result.data)
            }
        }
    }

    LaunchedEffect(uiState.sesionCerrada) {
        if (uiState.sesionCerrada) onSesionCerrada()
    }

    if (uiState.estaCargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ─── PERFIL ──────────────────────────────────────
        SeccionTitulo("Perfil")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        uiState.perfil?.nombre ?: "Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Nivel ${uiState.perfil?.nivel_perfil ?: 1} • 🪙 ${uiState.perfil?.monedas ?: 0} monedas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ─── MIS MASCOTAS ────────────────────────────────
        SeccionTitulo("Mis Mascotas (${uiState.mascotas.size})")

        if (uiState.mascotas.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    "No tienes mascotas aún. Visita la tienda para conseguir tu primera mascota.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            uiState.mascotas.forEach { mascota ->
                val esActiva = mascota.estaActiva

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esActiva)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MascotaAvatar(
                            rutaAsset = mascota.rutaAsset,
                            emoji = mascota.emoji,
                            esActiva = esActiva
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    mascota.nombreEspecie,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (esActiva) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            "⭐ Activa",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                "Nivel ${mascota.nivel} • ${mascota.experiencia}/${mascota.nivel * 100} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = {
                                    val max = mascota.nivel * 100
                                    if (max > 0) mascota.experiencia.toFloat() / max else 0f
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (esActiva) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        }

                        if (!esActiva) {
                            FilledTonalButton(
                                onClick = { mostrarConfirmarActivacion = mascota.id },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Activar",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ─── CUENTA ──────────────────────────────────────
        SeccionTitulo("Cuenta")

        OpcionGoogle(
            estaVinculado = uiState.estaVinculadoGoogle,
            googleEmail = uiState.googleEmail,
            estaCargandoGoogle = uiState.estaCargandoGoogle,
            onVincular = {
                scope.launch {
                    val signInIntent = viewModel.getGoogleSignInIntent()
                    googleSignInLauncher.launch(signInIntent)
                }
            },
            onDesvincular = { viewModel.desvincularGoogle() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ─── SINCRONIZACIÓN ──────────────────────────────
        SeccionTitulo("Sincronización")

        OpcionAjustes(
            icono = Icons.Default.CloudUpload,
            texto = "Subir datos a la nube",
            subtitulo = "Respaldar tu progreso",
            onClick = { mostrarDialogoConfirmarSubida = true }
        )

        OpcionAjustes(
            icono = Icons.Default.CloudDownload,
            texto = "Descargar datos",
            subtitulo = "Recuperar respaldo desde la nube",
            onClick = { mostrarDialogoConfirmarDescarga = true }
        )

        // Mostrar mensaje de sincronización
        if (uiState.mensajeSincronizacion != null) {
            LaunchedEffect(uiState.mensajeSincronizacion) {
                delay(3000)
                viewModel.limpiarMensajeSincronizacion()
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.mensajeSincronizacion!!.contains("✅"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = uiState.mensajeSincronizacion!!,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Mostrar indicador de carga durante sincronización
        if (uiState.estaSincronizando) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Sincronizando...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ─── ESTADÍSTICAS ────────────────────────────────
        /*SeccionTitulo("Estadísticas")

        OpcionAjustes(
            icono = Icons.Default.BarChart,
            texto = "Estadísticas",
            subtitulo = "Ver tu progreso detallado",
            onClick = { /* TODO - Implementar estadísticas */ }
        )

        Spacer(modifier = Modifier.height(8.dp))
*/
        // ─── PELIGRO ─────────────────────────────────────
        SeccionTitulo("Peligro")

        OpcionAjustes(
            icono = Icons.Default.DeleteForever,
            texto = "Eliminar todos mis datos",
            subtitulo = "Esta acción no se puede deshacer",
            colorTexto = MaterialTheme.colorScheme.error,
            onClick = { viewModel.toggleDialogoEliminarDatos(true) }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ─── DIÁLOGO CONFIRMAR SUBIDA ───────────────────────
    if (mostrarDialogoConfirmarSubida) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarSubida = false },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CloudUpload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
            },
            title = { Text("Subir datos a la nube", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("¿Estás seguro de que quieres subir tus datos?")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Los datos existentes en la nube serán REEMPLAZADOS por tus datos actuales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmarSubida = false
                        viewModel.syncData()
                    },
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Subir", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmarSubida = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ─── DIÁLOGO CONFIRMAR DESCARGA ─────────────────────
    if (mostrarDialogoConfirmarDescarga) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarDescarga = false },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CloudDownload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
            },
            title = { Text("Descargar datos", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("¿Quieres descargar los datos desde la nube?")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tus datos locales serán REEMPLAZADOS por los datos de la nube.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmarDescarga = false
                        viewModel.downloadData()
                    },
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Descargar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmarDescarga = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ─── DIÁLOGO CONFIRMAR ACTIVACIÓN ───────────────────
    if (mostrarConfirmarActivacion != null) {
        val mascota = uiState.mascotas.find { it.id == mostrarConfirmarActivacion }
        AlertDialog(
            onDismissRequest = { mostrarConfirmarActivacion = null },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        MascotaAvatarSimple(
                            rutaAsset = mascota?.rutaAsset,
                            emoji = mascota?.emoji ?: "🐾"
                        )
                    }
                }
            },
            title = { Text("Cambiar mascota activa", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Quieres que \"${mascota?.nombreEspecie ?: ""}\" sea tu mascota principal?\n\nSus habilidades se aplicarán globalmente.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmarActivacion?.let { viewModel.activarMascota(it) }
                        mostrarConfirmarActivacion = null
                    },
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Activar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarActivacion = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ─── DIÁLOGO ELIMINAR DATOS ─────────────────────────
    if (uiState.mostrarDialogoEliminarDatos) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDialogoEliminarDatos(false) },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                    }
                }
            },
            title = { Text("Eliminar datos", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás completamente seguro?\n\nEsta acción eliminará permanentemente tu perfil, mascotas, tableros y tareas. No se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminarTodosLosDatos() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Eliminar todo") }
            },
            dismissButton = { TextButton(onClick = { viewModel.toggleDialogoEliminarDatos(false) }) { Text("Cancelar") } }
        )
    }
    if (uiState.mostrarDialogoRestaurar) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarRestauracion() },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🔄", fontSize = 28.sp)
                    }
                }
            },
            title = { Text("Cuenta existente", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ya existe una cuenta con este correo electrónico en la nube.")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "¿Qué deseas hacer?",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("• Restaurar: Cargarás los datos guardados en la nube")
                    Text("• Subir locales: Reemplazarás los datos en la nube con tus datos actuales")
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restaurarDatosDesdeNube() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text("Restaurar desde nube") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = { viewModel.subirDatosLocalesANube() }
                    ) {
                        Text("Subir mis datos locales")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { viewModel.cancelarRestauracion() }
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }
}

// ───────────────────────────────────────────────────────────
// COMPONENTES (igual que antes)
// ───────────────────────────────────────────────────────────

@Composable
private fun SeccionTitulo(titulo: String) {
    Text(
        titulo,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun OpcionAjustes(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    subtitulo: String,
    colorTexto: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(texto, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = colorTexto)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun OpcionGoogle(
    estaVinculado: Boolean,
    googleEmail: String?,
    estaCargandoGoogle: Boolean,
    onVincular: () -> Unit,
    onDesvincular: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (estaVinculado)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (estaVinculado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Google",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (estaVinculado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (estaVinculado && googleEmail != null) {
                    Text(
                        "Vinculado con: $googleEmail",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Respalda tu progreso en la nube",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (estaCargandoGoogle) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else if (estaVinculado) {
                OutlinedButton(
                    onClick = onDesvincular,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.LinkOff, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Desvincular", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Button(
                    onClick = onVincular,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Vincular", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun MascotaAvatar(
    rutaAsset: String?,
    emoji: String,
    esActiva: Boolean
) {
    val context = LocalContext.current
    val archivoExiste = remember(rutaAsset) {
        if (rutaAsset == null) return@remember false
        try {
            context.assets.open(rutaAsset).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (esActiva) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        if (archivoExiste && rutaAsset != null) {
            SubcomposeAsyncImage(
                model = "file:///android_asset/$rutaAsset",
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = { Text(emoji, fontSize = 22.sp) }
            )
        } else {
            Text(emoji, fontSize = 22.sp)
        }
    }
}

@Composable
private fun MascotaAvatarSimple(
    rutaAsset: String?,
    emoji: String
) {
    val context = LocalContext.current
    val archivoExiste = remember(rutaAsset) {
        if (rutaAsset == null) return@remember false
        try {
            context.assets.open(rutaAsset).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (archivoExiste && rutaAsset != null) {
            SubcomposeAsyncImage(
                model = "file:///android_asset/$rutaAsset",
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = { Text(emoji, fontSize = 24.sp) }
            )
        } else {
            Text(emoji, fontSize = 24.sp)
        }
    }
}