package com.example.spire_task.feature.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import android.util.Log

@Composable
fun MascotaRowCard(
    mascota: MascotaBaseEntity,
    estaSeleccionada: Boolean = false,
    monedasUsuario: Int = 0,
    onClick: () -> Unit
) {
    val puedeComprar = mascota.precio_monedas <= monedasUsuario

    Card(
        modifier = Modifier
            .fillMaxWidth() // Ocupa todo el ancho disponible en la columna de selección
            .padding(vertical = 6.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp), // Esquinas ligeramente más suaves
        colors = CardDefaults.cardColors(
            containerColor = if (estaSeleccionada) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (estaSeleccionada) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        onClick = onClick
    ) {
        // Estructura horizontal: Imagen | Detalles de Texto | Precio y Botón
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. IMAGEN DE MASCOTA (Izquierda)
            ImagenMascota(
                rutaAsset = mascota.ruta_asset_base,
                emojiFallback = mascota.emoji,
                nombreEspecie = mascota.nombre_especie
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. TEXTOS: NOMBRE Y DESCRIPCIÓN (Centro - Toma el espacio restante)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = mascota.nombre_especie,
                    style = MaterialTheme.typography.titleMedium, // Un poco más grande para destacar
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = mascota.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, // Reducido a 2 líneas para mantener la fila compacta
                    lineHeight = 14.sp
                )
            }

            // 3. ACCIONES Y PRECIO (Derecha)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Precio / Etiqueta de Monedas
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (puedeComprar) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = "${mascota.precio_monedas} 🪙",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (puedeComprar) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón de Selección más compacto
                Button(
                    onClick = onClick,
                    enabled = puedeComprar,
                    modifier = Modifier.wrapContentSize(), // Ajustado al tamaño del texto
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), // Más esbelto
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (estaSeleccionada) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                ) {
                    Text(
                        text = if (estaSeleccionada) "✓" else "Elegir",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Componente interno (Se mantiene igual, solo ajusta el tamaño visual a 72.dp para encajar en la fila)
 */
@Composable
private fun ImagenMascota(
    rutaAsset: String,
    emojiFallback: String,
    nombreEspecie: String
) {
    val context = LocalContext.current
    val rutaCompleta = "file:///android_asset/$rutaAsset"

    val archivoExiste = remember(rutaAsset) {
        try {
            val inputStream = context.assets.open(rutaAsset)
            inputStream.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    Box(
        modifier = Modifier.size(72.dp), // Reducido levemente de 80 a 72 para balancear la fila
        contentAlignment = Alignment.Center
    ) {
        if (archivoExiste) {
            AsyncImage(
                model = rutaCompleta,
                contentDescription = nombreEspecie,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = emojiFallback,
                fontSize = 40.sp
            )
        }
    }
}