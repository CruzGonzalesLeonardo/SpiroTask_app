package com.example.spire_task.feature.shop

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import kotlinx.coroutines.delay

// Colores
private val GoldColor = Color(0xFFFFCA28)
private val GoldDark = Color(0xFFF9A825)
private val GoldLight = Color(0xFFFFF8E1)
private val EggShell = Color(0xFFFFF3E0)
private val KanbanDoneColor = Color(0xFF6BCB77)
private val CyanBright = Color(0xFF06B6D4)
private val PinkAccent = Color(0xFFFF6B9D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel = viewModel(factory = ShopViewModelFactory()),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCompra by remember { mutableStateOf<MascotaBaseEntity?>(null) }
    var huevoScale by remember { mutableStateOf(1f) }
    var huevoRotation by remember { mutableFloatStateOf(0f) }
    var mostrarBrillo by remember { mutableStateOf(false) }

    // Animación del huevo
    LaunchedEffect(uiState.animandoHuevo) {
        if (uiState.animandoHuevo) {
            mostrarBrillo = true
            repeat(8) {
                huevoScale = 1.15f
                huevoRotation = 10f
                delay(80)
                huevoScale = 0.85f
                huevoRotation = -10f
                delay(80)
            }
            huevoScale = 1f
            huevoRotation = 0f
            mostrarBrillo = false
        }
    }

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            delay(3000)
            viewModel.limpiarError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Header con monedas
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*Text(
                    "🛍️ Tienda",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GoldColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 20.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${uiState.monedas}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldDark
                        )
                    }
                }*/
            }
        }

        // Mensaje de error
        uiState.error?.let { error ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ─── HUEVO MISTERIOSO ───────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🥚 HUEVO MISTERIOSO",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 2.sp
                        )

                        Text(
                            "¡Abre y descubre qué hay dentro!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(20.dp))

                        // Huevo con brillo
                        Box(
                            modifier = Modifier.size(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (mostrarBrillo) {
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    GoldColor.copy(alpha = 0.4f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .scale(huevoScale)
                                    .shadow(16.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White,
                                                EggShell,
                                                GoldLight,
                                                GoldColor.copy(alpha = 0.5f)
                                            ),
                                            center = Offset(35f, 35f),
                                            radius = 100f
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥚", fontSize = 56.sp)
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Probabilidades
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ChipProbabilidad("🆕", "35%", "Nueva", KanbanDoneColor)
                            ChipProbabilidad("⭐", "30%", "XP", CyanBright)
                            ChipProbabilidad("🪙", "20%", "Monedas", GoldColor)
                            ChipProbabilidad("💨", "15%", "Vacío", Color.Gray)
                        }

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.abrirHuevo() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            enabled = uiState.monedas >= 30 && !uiState.animandoHuevo,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldColor,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            if (uiState.animandoHuevo) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = GoldDark,
                                    strokeWidth = 3.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Abriendo...", fontWeight = FontWeight.Bold, color = GoldDark)
                            } else {
                                Icon(Icons.Default.Star, null, tint = GoldDark, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("ABRIR (30 🪙)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldDark)
                            }
                        }
                    }
                }
            }

            // ─── COMPRA DIRECTA ──────────────────────────────
            item {
                Text(
                    "🏪 Compra Directa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Elige la mascota que quieras, 100% segura",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(uiState.mascotasBase) { mascota ->
                val yaPoseida = viewModel.tieneEspecie(mascota.id_mascota_base)
                MascotaTiendaCard(
                    mascota = mascota,
                    yaPoseida = yaPoseida,
                    puedeComprar = viewModel.puedeComprar(mascota.precio_monedas),
                    onComprar = { mostrarDialogoCompra = mascota }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Diálogo resultado del huevo
    if (uiState.mostrarResultado && uiState.resultadoHuevo != null) {
        DialogoResultadoHuevo(
            resultado = uiState.resultadoHuevo!!,
            onCerrar = { viewModel.cerrarResultado() }
        )
    }

    // Diálogo confirmación de compra
    mostrarDialogoCompra?.let { mascota ->
        DialogoConfirmarCompra(
            mascota = mascota,
            monedasActuales = uiState.monedas,
            onConfirmar = {
                viewModel.comprarMascotaDirecta(mascota)
                mostrarDialogoCompra = null
            },
            onCancelar = { mostrarDialogoCompra = null }
        )
    }
}

@Composable
private fun ChipProbabilidad(emoji: String, porcentaje: String, texto: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(porcentaje, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            Text(texto, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MascotaTiendaCard(
    mascota: MascotaBaseEntity,
    yaPoseida: Boolean,
    puedeComprar: Boolean,
    onComprar: () -> Unit
) {
    val context = LocalContext.current
    val archivoExiste = remember(mascota.ruta_asset_base) {
        try {
            context.assets.open(mascota.ruta_asset_base).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (yaPoseida) 0.dp else 6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (yaPoseida)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con imagen real
            Surface(
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (archivoExiste) {
                        SubcomposeAsyncImage(
                            model = "file:///android_asset/${mascota.ruta_asset_base}",
                            contentDescription = mascota.nombre_especie,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = { CircularProgressIndicator(modifier = Modifier.size(24.dp)) },
                            error = { Text(mascota.emoji, fontSize = 32.sp) }
                        )
                    } else {
                        Text(mascota.emoji, fontSize = 36.sp)
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    mascota.nombre_especie,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (yaPoseida)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                // Habilidad única
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PinkAccent.copy(alpha = 0.1f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 10.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${mascota.habilidad_nombre}: ${mascota.habilidad_descripcion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PinkAccent,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Precio
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (yaPoseida)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        GoldLight.copy(alpha = 0.5f)
                ) {
                    Text(
                        "${mascota.precio_monedas} 🪙",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (yaPoseida)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else
                            GoldDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Botón de acción
            when {
                yaPoseida -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KanbanDoneColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = KanbanDoneColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Adquirida", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = KanbanDoneColor)
                        }
                    }
                }
                !puedeComprar -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sin monedas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onComprar,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Comprar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoConfirmarCompra(
    mascota: MascotaBaseEntity,
    monedasActuales: Int,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val monedasRestantes = monedasActuales - mascota.precio_monedas
    val context = LocalContext.current
    val archivoExiste = remember(mascota.ruta_asset_base) {
        try {
            context.assets.open(mascota.ruta_asset_base).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (archivoExiste) {
                        SubcomposeAsyncImage(
                            model = "file:///android_asset/${mascota.ruta_asset_base}",
                            contentDescription = mascota.nombre_especie,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = { Text(mascota.emoji, fontSize = 36.sp) }
                        )
                    } else {
                        Text(mascota.emoji, fontSize = 36.sp)
                    }
                }
            }
        },
        title = {
            Text(
                "¿Adquirir ${mascota.nombre_especie}?",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    mascota.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldLight.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("✨ Habilidad especial:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "${mascota.habilidad_nombre}: ${mascota.habilidad_descripcion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PinkAccent
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Precio:", style = MaterialTheme.typography.bodyMedium)
                    Text("${mascota.precio_monedas} 🪙", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GoldDark)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Te quedarán:", style = MaterialTheme.typography.bodyMedium)
                    Text("$monedasRestantes 🪙", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (monedasRestantes >= 0) KanbanDoneColor else MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Comprar", fontWeight = FontWeight.Bold)
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
private fun DialogoResultadoHuevo(
    resultado: ResultadoHuevo,
    onCerrar: () -> Unit
) {
    val context = LocalContext.current

    when (resultado) {
        is ResultadoHuevo.NuevaMascota -> {
            val archivoExiste = remember(resultado.especie.ruta_asset_base) {
                try {
                    context.assets.open(resultado.especie.ruta_asset_base).close()
                    true
                } catch (e: Exception) {
                    false
                }
            }

            AlertDialog(
                onDismissRequest = onCerrar,
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = KanbanDoneColor.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (archivoExiste) {
                                SubcomposeAsyncImage(
                                    model = "file:///android_asset/${resultado.especie.ruta_asset_base}",
                                    contentDescription = resultado.especie.nombre_especie,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    error = { Text(resultado.especie.emoji, fontSize = 48.sp) }
                                )
                            } else {
                                Text(resultado.especie.emoji, fontSize = 48.sp)
                            }
                        }
                    }
                },
                title = {
                    Text(
                        "🎉 ¡Nueva Mascota!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "¡Conseguiste un ${resultado.especie.nombre_especie}!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PinkAccent.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("✨ Habilidad:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${resultado.especie.habilidad_nombre}: ${resultado.especie.habilidad_descripcion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PinkAccent
                                )
                            }
                        }
                        if (resultado.especie.ruta_asset_habitad != null) {
                            Spacer(Modifier.height(8.dp))
                            Text("🏡 Hábitat desbloqueado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onCerrar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KanbanDoneColor)
                    ) {
                        Text("¡Genial!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            )
        }
        is ResultadoHuevo.ExperienciaGanada -> {
            AlertDialog(
                onDismissRequest = onCerrar,
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = CyanBright.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⭐", fontSize = 48.sp)
                        }
                    }
                },
                title = { Text("⭐ ¡Experiencia Ganada!", fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${resultado.especie.nombre_especie} ganó +${resultado.xp} XP",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text("¡Sigue así!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    Button(onClick = onCerrar, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = CyanBright)) {
                        Text("Continuar", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        is ResultadoHuevo.MonedasRecuperadas -> {
            AlertDialog(
                onDismissRequest = onCerrar,
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = GoldColor.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🪙", fontSize = 48.sp)
                        }
                    }
                },
                title = { Text("💰 ¡Monedas Recuperadas!", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Recuperaste ${resultado.cantidad} monedas",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(onClick = onCerrar, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = GoldColor)) {
                        Text("Genial", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        is ResultadoHuevo.Vacio -> {
            AlertDialog(
                onDismissRequest = onCerrar,
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = Color.Gray.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("💨", fontSize = 48.sp)
                        }
                    }
                },
                title = { Text("😢 Huevo Vacío", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "El huevo estaba vacío...\n¡Mejor suerte la próxima!",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(onClick = onCerrar, shape = RoundedCornerShape(16.dp)) {
                        Text("Continuar", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}