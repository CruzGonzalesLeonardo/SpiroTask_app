package com.example.spire_task.feature.shop

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import kotlinx.coroutines.delay

private val GoldColor = Color(0xFFFFCA28)
private val GoldDark = Color(0xFFF9A825)
private val GoldLight = Color(0xFFFFF8E1)
private val KanbanDoneColor = Color(0xFF6BCB77)
private val CyanBright = Color(0xFF06B6D4)
private val PinkAccent = Color(0xFFFF6B9D)

/**
 * 🛠️ FUNCIÓN HELPER: Convierte nombres con tildes o mayúsculas en la ruta exacta
 * de la carpeta asignada en tus assets (buho, dragon, tortuga, etc.)
 */
fun obtenerRutaPieza(mascota: MascotaBaseEntity): String {
    val nombreCarpeta = when (mascota.id_mascota_base) {
        1 -> "buho"
        2 -> "zorro"
        3 -> "tortuga"
        4 -> "dragon"
        else -> "buho" // Ruta de respaldo por si acaso
    }
    return "file:///android_asset/pets/$nombreCarpeta/parte.png"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel = viewModel(factory = ShopViewModelFactory()),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCompra by remember { mutableStateOf<MascotaBaseEntity?>(null) }
    var huevoScale by remember { mutableStateOf(1f) }
    var mostrarBrillo by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.animandoHuevo) {
        if (uiState.animandoHuevo) {
            mostrarBrillo = true
            repeat(5) {
                huevoScale = 1.2f
                delay(100)
                huevoScale = 0.8f
                delay(100)
            }
            huevoScale = 1f
            mostrarBrillo = false
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            delay(3500)
            viewModel.limpiarError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // 🏰 ENCABEZADO
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text("Mercado Místico", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text("Forja tus compañeros de aventura", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = GoldColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("${uiState.monedas}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = GoldDark)
                    }
                }
            }
        }

        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(10.dp))
                    Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ─── RULETA MÍSTICA ───────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎰 RULETA MÍSTICA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Consigue piezas. Excluye criaturas obtenidas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(Modifier.height(16.dp))

                        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                            if (mostrarBrillo) {
                                Box(modifier = Modifier.size(110.dp).clip(CircleShape).background(Brush.radialGradient(listOf(GoldColor.copy(alpha = 0.3f), Color.Transparent))))
                            }
                            Box(modifier = Modifier.size(90.dp).scale(huevoScale).clip(CircleShape).background(GoldLight), contentAlignment = Alignment.Center) {
                                Text("🥚", fontSize = 44.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ChipProbabilidad("🧩", "Comunes", "Alta", KanbanDoneColor)
                            ChipProbabilidad("🐉", "Dragón", "Baja", PinkAccent)
                            ChipProbabilidad("⭐", "XP", "30%", CyanBright)
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.abrirHuevo() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = uiState.monedas >= 30 && !uiState.animandoHuevo,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldColor)
                        ) {
                            if (uiState.animandoHuevo) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldDark, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Rounded.Casino, null, tint = GoldDark)
                                Spacer(Modifier.width(8.dp))
                                Text("GIRAR RULETA (30 🪙)", fontWeight = FontWeight.Bold, color = GoldDark)
                            }
                        }
                    }
                }
            }

            // ─── COMPRA DIRECTA ──────────────────
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Extension, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Piezas de Rompecabezas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Límite: 2 compras directas por criatura al día", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(uiState.mascotasBase) { mascota ->
                val yaPoseida = viewModel.tieneEspecie(mascota.id_mascota_base)
                val compradasHoy = viewModel.obtenerPiezasCompradasHoy(mascota.id_mascota_base)

                MascotaPiezaTiendaCard(
                    mascota = mascota,
                    yaPoseida = yaPoseida,
                    compradasHoy = compradasHoy,
                    onComprarPieza = { mostrarDialogoCompra = mascota },
                    onEnsamblar = { viewModel.ensamblarMascota(mascota) }
                )
            }
        }
    }

    if (uiState.mostrarResultado && uiState.resultadoHuevo != null) {
        DialogoResultadoHuevo(resultado = uiState.resultadoHuevo!!, onCerrar = { viewModel.cerrarResultado() })
    }

    mostrarDialogoCompra?.let { mascota ->
        DialogoConfirmarCompra(
            mascota = mascota,
            monedasActuales = uiState.monedas,
            onConfirmar = {
                viewModel.comprarPiezaDirecta(mascota)
                mostrarDialogoCompra = null
            },
            onCancelar = { mostrarDialogoCompra = null }
        )
    }
}

@Composable
private fun MascotaPiezaTiendaCard(
    mascota: MascotaBaseEntity,
    yaPoseida: Boolean,
    compradasHoy: Int,
    onComprarPieza: () -> Unit,
    onEnsamblar: () -> Unit
) {
    val listoParaEnsamblar = mascota.rompecabezas_actuales >= mascota.rompecabezas_totales
    val esDragon = mascota.nombre_especie.contains("dragon", ignoreCase = true) || mascota.id_mascota_base == 4

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (yaPoseida) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else if (esDragon) PinkAccent.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {

            // 🧩 ASSET DINÁMICO CORREGIDO SEGÚN LA MASCOTA
            Box(modifier = Modifier.size(75.dp), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (esDragon) PinkAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    SubcomposeAsyncImage(
                        model = obtenerRutaPieza(mascota), // <-- Llamada dinámica
                        contentDescription = "Pieza Rompecabezas",
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        contentScale = ContentScale.Fit,
                        error = { Text("🧩", fontSize = 32.sp) }
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(24.dp).align(Alignment.BottomEnd).shadow(2.dp, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(mascota.emoji, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pieza: ${mascota.nombre_especie}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (esDragon) PinkAccent else MaterialTheme.colorScheme.onSurface
                    )
                    if (esDragon) {
                        Surface(shape = RoundedCornerShape(6.dp), color = PinkAccent.copy(alpha = 0.2f), modifier = Modifier.padding(start = 6.dp)) {
                            Text("MÍTICO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = PinkAccent, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { mascota.rompecabezas_actuales.toFloat() / mascota.rompecabezas_totales.toFloat() },
                        modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                        color = if (listoParaEnsamblar) KanbanDoneColor else if (esDragon) PinkAccent else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${mascota.rompecabezas_actuales}/${mascota.rompecabezas_totales}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Adquiridas hoy: $compradasHoy/2 (Directas)",
                    fontSize = 11.sp,
                    color = if (compradasHoy >= 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "${mascota.precio_monedas} 🪙 por pieza",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoldDark,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            when {
                yaPoseida -> {
                    Icon(Icons.Default.CheckCircle, null, tint = KanbanDoneColor)
                }
                listoParaEnsamblar -> {
                    Button(
                        onClick = onEnsamblar,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KanbanDoneColor),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("INVOCAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
                compradasHoy >= 2 -> {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.padding(8.dp).size(16.dp))
                    }
                }
                else -> {
                    IconButton(
                        onClick = onComprarPieza,
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipProbabilidad(emoji: String, titulo: String, tasa: String, color: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.08f)) {
        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Column {
                Text(titulo, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tasa, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
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

    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(modifier = Modifier.size(70.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                Box(contentAlignment = Alignment.Center) {
                    SubcomposeAsyncImage(
                        model = obtenerRutaPieza(mascota), // <-- Diálogo de compra dinámico
                        contentDescription = null,
                        modifier = Modifier.size(45.dp)
                    )
                }
            }
        },
        title = { Text("¿Adquirir pieza de ${mascota.nombre_especie}?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(text = "Añadirás un fragmento al rompecabezas. Requiere ${mascota.rompecabezas_totales} totales para forjar la criatura.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Costo de Pieza:")
                    Text("${mascota.precio_monedas} 🪙", fontWeight = FontWeight.Bold, color = GoldDark)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Fondos tras compra:")
                    Text("$monedasRestantes 🪙", fontWeight = FontWeight.Bold, color = if (monedasRestantes >= 0) KanbanDoneColor else MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { Button(onClick = onConfirmar) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoResultadoHuevo(
    resultado: ResultadoHuevo,
    onCerrar: () -> Unit
) {
    when (resultado) {
        is ResultadoHuevo.NuevaPieza -> {
            AlertDialog(
                onDismissRequest = onCerrar,
                shape = RoundedCornerShape(24.dp),
                icon = {
                    Surface(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(16.dp), color = GoldColor.copy(alpha = 0.2f)) {
                        Box(contentAlignment = Alignment.Center) {
                            SubcomposeAsyncImage(
                                model = obtenerRutaPieza(resultado.especie), // <-- Diálogo de ruleta dinámico
                                contentDescription = null,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                },
                title = { Text("🧩 ¡Fragmento Encontrado!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                text = {
                    Text(
                        text = "¡La suerte está de tu lado! Encontraste una pieza para el rompecabezas de: ${resultado.especie.nombre_especie}.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = { Button(onClick = onCerrar, modifier = Modifier.fillMaxWidth()) { Text("Añadir al Rompecabezas") } }
            )
        }
        is ResultadoHuevo.ExperienciaGanada -> {
            AlertDialog(
                onDismissRequest = onCerrar,
                shape = RoundedCornerShape(24.dp),
                icon = { Text("⭐", fontSize = 40.sp) },
                title = { Text("¡Polvo de Estrellas!", fontWeight = FontWeight.Bold) },
                text = { Text("Tu mascota activa absorbió la energía del huevo obteniendo +${resultado.xp} XP.") },
                confirmButton = { Button(onClick = onCerrar) { Text("Excelente") } }
            )
        }
        is ResultadoHuevo.MonedasRecuperadas -> {
            AlertDialog(
                onDismissRequest = onCerrar,
                shape = RoundedCornerShape(24.dp),
                icon = { Text("🪙", fontSize = 40.sp) },
                title = { Text("Monedas Devueltas", fontWeight = FontWeight.Bold) },
                text = { Text("El huevo colapsó pero recuperaste ${resultado.cantidad} monedas de oro.") },
                confirmButton = { Button(onClick = onCerrar) { Text("Recoger") } }
            )
        }
        is ResultadoHuevo.Vacio -> {
            AlertDialog(
                onDismissRequest = onCerrar,
                shape = RoundedCornerShape(24.dp),
                icon = { Text("💨", fontSize = 40.sp) },
                title = { Text("Huevo Ecléctico", fontWeight = FontWeight.Bold) },
                text = { Text("Se desvaneció en el aire... ¡Inténtalo de nuevo!") },
                confirmButton = { Button(onClick = onCerrar) { Text("Continuar") } }
            )
        }
    }
}