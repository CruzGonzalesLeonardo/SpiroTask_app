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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.spire_task.core.notification.SpiroNotificationManager
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

    val notificador = remember { SpiroNotificationManager(context) }

    // Estados locales para los Switches agregados
    var recordatorioDiarioActivo by remember { mutableStateOf(true) }
    var modoOscuroActivo by remember { mutableStateOf(false) }

    var mostrarConfirmarActivacion by remember { mutableStateOf<Int?>(null) }
    var mostrarDialogoConfirmarSubida by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmarDescarga by remember { mutableStateOf(false) }

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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ─── PERFIL ──────────────────────────────────────
        SeccionTitulo("Perfil")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
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
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        uiState.perfil?.nombre ?: "Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Nivel ${uiState.perfil?.nivel_perfil ?: 1}") }
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.MonetizationOn, null, tint = Color(0xFFFFA500), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${uiState.perfil?.monedas ?: 0} Monedas",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ─── MIS MASCOTAS ────────────────────────────────
        SeccionTitulo("Mis Mascotas (${uiState.mascotas.size})")
        if (uiState.mascotas.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Text(
                    "No tienes mascotas aún. Visita la tienda para conseguir tu primera mascota.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.mascotas.forEach { mascota ->
                    val esActiva = mascota.estaActiva

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (esActiva)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
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
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Rounded.Star, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text(
                                                    "Activa",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                                Text(
                                    "Nivel ${mascota.nivel} • ${mascota.experiencia}/${mascota.nivel * 100} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        val max = mascota.nivel * 100
                                        if (max > 0) mascota.experiencia.toFloat() / max else 0f
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }

                            if (!esActiva) {
                                FilledTonalButton(
                                    onClick = { mostrarConfirmarActivacion = mascota.id },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Rounded.Pets, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Activar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ─── NUEVO: PREFERENCIAS Y VISUAL ────────────────
        SeccionTitulo("Visual y Alertas")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // Fila Switch de Modo Oscuro
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (modoOscuroActivo) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Tema de la aplicación", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("Cambiar entre claro y oscuro", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = uiState.modoOscuroActivo,
                        onCheckedChange = { viewModel.setModoOscuro(it) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                // Fila Switch de Recordatorio diario de tareas
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.NotificationsActive, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Recordatorio Diario", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("Notificación diaria para tus pendientes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }


                    Switch(
                        checked = uiState.recordatorioDiarioActivo,
                        onCheckedChange = {
                            viewModel.setRecordatorioDiario(it)
                            if (it) {
                                notificador.lanzarRecordatorioDiario()
                            }
                        }
                    )
                }
            }
        }

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

        // ─── SINCRONIZACIÓN ──────────────────────────────
        // ─── SINCRONIZACIÓN ──────────────────────────────
        SeccionTitulo("Sincronización")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                OpcionAjustesFila(
                    icono = Icons.Rounded.CloudUpload,
                    texto = "Subir datos a la nube",
                    subtitulo = "Respaldar tu progreso actual",
                    // Se deshabilita si no está vinculado
                    habilitado = uiState.estaVinculadoGoogle,
                    onClick = { mostrarDialogoConfirmarSubida = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                OpcionAjustesFila(
                    icono = Icons.Rounded.CloudDownload,
                    texto = "Descargar datos",
                    subtitulo = "Recuperar respaldo de la nube",
                    // Se deshabilita si no está vinculado
                    habilitado = uiState.estaVinculadoGoogle,
                    onClick = { mostrarDialogoConfirmarDescarga = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Sync,
                        contentDescription = null,
                        tint = if (uiState.estaVinculadoGoogle) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Sincronización automática",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.estaVinculadoGoogle) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Text(
                            text = "Respalda tus cambios en segundo plano al instante",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (uiState.estaVinculadoGoogle) 1f else 0.38f)
                        )
                    }
                    Switch(
                        // Si no está vinculado, forzamos a que visualmente esté en false
                        checked = uiState.estaVinculadoGoogle && uiState.sincronizacionAutomatica,
                        enabled = uiState.estaVinculadoGoogle, // Deshabilitar interacción
                        onCheckedChange = { viewModel.setSincronizacionAutomatica(it) }
                    )
                }
            }
        }

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
                Text(text = uiState.mensajeSincronizacion!!, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
            }
        }

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

        // ─── PELIGRO ─────────────────────────────────────
        SeccionTitulo("Zona de Riesgo")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
        ) {
            OpcionAjustesFila(
                icono = Icons.Rounded.DeleteForever,
                texto = "Eliminar todos mis datos",
                subtitulo = "Acción destructiva permanente de este móvil",
                colorIcono = MaterialTheme.colorScheme.error,
                onClick = { viewModel.toggleDialogoEliminarDatos(true) }
            )
        }

        // ─── NUEVO: ACERCA DE ────────────────────────────
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Spiro Task", // Nombre de la app
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = "Versión 1.0.0", // Versión de la app
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // ─── DIÁLOGOS ORIGINALES PRESERVADOS ───────────────────
    if (mostrarDialogoConfirmarSubida) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarSubida = false },
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Rounded.CloudUpload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) },
            title = { Text("Subir datos a la nube", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que quieres subir tus datos? Reemplazarán las copias anteriores en la nube.") },
            confirmButton = { Button(onClick = { mostrarDialogoConfirmarSubida = false; viewModel.syncData() }) { Text("Subir") } },
            dismissButton = { TextButton(onClick = { mostrarDialogoConfirmarSubida = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarDialogoConfirmarDescarga) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarDescarga = false },
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Rounded.CloudDownload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) },
            title = { Text("Descargar datos", fontWeight = FontWeight.Bold) },
            text = { Text("¿Quieres descargar los datos? Tus registros del teléfono locales serán reemplazados.") },
            confirmButton = { Button(onClick = { mostrarDialogoConfirmarDescarga = false; viewModel.downloadData() }) { Text("Descargar") } },
            dismissButton = { TextButton(onClick = { mostrarDialogoConfirmarDescarga = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarConfirmarActivacion != null) {
        val mas = uiState.mascotas.find { it.id == mostrarConfirmarActivacion }
        AlertDialog(
            onDismissRequest = { mostrarConfirmarActivacion = null },
            shape = RoundedCornerShape(24.dp),
            icon = { Box(Modifier.size(40.dp)) { MascotaAvatarSimple(rutaAsset = mas?.rutaAsset, emoji = mas?.emoji ?: "🐾") } },
            title = { Text("Cambiar mascota activa", fontWeight = FontWeight.Bold) },
            text = { Text("¿Quieres activar a \"${mas?.nombreEspecie ?: ""}\" como compañera activa?") },
            confirmButton = { Button(onClick = { mostrarConfirmarActivacion?.let { viewModel.activarMascota(it) }; mostrarConfirmarActivacion = null }) { Text("Activar") } },
            dismissButton = { TextButton(onClick = { mostrarConfirmarActivacion = null }) { Text("Cancelar") } }
        )
    }

    if (uiState.mostrarDialogoEliminarDatos) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDialogoEliminarDatos(false) },
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar todos los datos", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás completamente seguro? Se eliminará de forma irreversible tu progreso local.") },
            confirmButton = { Button(onClick = { viewModel.eliminarTodosLosDatos() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Eliminar Todo") } },
            dismissButton = { TextButton(onClick = { viewModel.toggleDialogoEliminarDatos(false) }) { Text("Cancelar") } }
        )
    }

    if (uiState.mostrarDialogoRestaurar) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarRestauracion() },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Cuenta existente en la nube", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas restaurar la copia de la nube o sobrescribirla con tus datos de este móvil?") },
            confirmButton = { Button(onClick = { viewModel.restaurarDatosDesdeNube() }) { Text("Restaurar Nube") } },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.subirDatosLocalesANube() }) { Text("Subir local") }
                    TextButton(onClick = { viewModel.cancelarRestauracion() }) { Text("Cancelar", color = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }
}

@Composable
private fun SeccionTitulo(titulo: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun OpcionAjustesFila(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    subtitulo: String,
    colorIcono: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    habilitado: Boolean = true, // <-- Nuevo parámetro por defecto
    onClick: () -> Unit
) {
    val opacidad = if (habilitado) 1f else 0.38f // Opacidad estándar de Material 3 para deshabilitado

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado, onClick = onClick) // <-- Controla el click
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = if (habilitado) colorIcono else colorIcono.copy(alpha = opacidad),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                texto,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = opacidad)
            )
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = opacidad)
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (habilitado) 0.5f else 0.2f)
        )
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (estaVinculado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = if (estaVinculado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Google Cloud",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (estaVinculado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (estaVinculado && googleEmail != null) "Vinculado con $googleEmail" else "Respalda tu progreso en la nube",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (estaCargandoGoogle) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else if (estaVinculado) {
                OutlinedButton(
                    onClick = onDesvincular,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.LinkOff, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Desconectar", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Button(
                    onClick = onVincular,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Link, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Vincular", style = MaterialTheme.typography.labelMedium)
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