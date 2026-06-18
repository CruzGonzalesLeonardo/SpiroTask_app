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
fun MascotaCard(
    mascota: MascotaBaseEntity,
    estaSeleccionada: Boolean = false,
    monedasUsuario: Int = 0,
    onClick: () -> Unit
) {
    val puedeComprar = mascota.precio_monedas <= monedasUsuario

    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ─── IMAGEN DE MASCOTA ────────────────────
            ImagenMascota(
                rutaAsset = mascota.ruta_asset_base,
                emojiFallback = mascota.emoji,
                nombreEspecie = mascota.nombre_especie
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ─── NOMBRE ───────────────────────────────
            Text(
                text = mascota.nombre_especie,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ─── DESCRIPCIÓN ──────────────────────────
            Text(
                text = mascota.descripcion,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ─── PRECIO ───────────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (puedeComprar) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Text(
                    text = "${mascota.precio_monedas} 🪙",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (puedeComprar) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── BOTÓN SELECCIÓN ──────────────────────
            Button(
                onClick = onClick,
                enabled = puedeComprar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (estaSeleccionada) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            ) {
                Text(
                    text = if (estaSeleccionada) "✓ Elegida" else "Elegir",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Componente que carga imagen desde assets o muestra emoji fallback.
 */
@Composable
private fun ImagenMascota(
    rutaAsset: String,
    emojiFallback: String,
    nombreEspecie: String
) {
    val context = LocalContext.current
    val rutaCompleta = "file:///android_asset/$rutaAsset"

    // Verificar si el archivo existe en assets
    val archivoExiste = remember(rutaAsset) {
        try {
            val inputStream = context.assets.open(rutaAsset)
            val tamanio = inputStream.available()
            inputStream.close()
            Log.d("SPIRO_DEBUG", "✅ Archivo encontrado: $rutaAsset ($tamanio bytes)")
            Log.d("SPIRO_DEBUG", "   Ruta completa: $rutaCompleta")
            true
        } catch (e: Exception) {
            Log.e("SPIRO_DEBUG", "❌ Archivo NO encontrado: $rutaAsset")
            Log.e("SPIRO_DEBUG", "   Error: ${e.javaClass.simpleName} - ${e.message}")

            // Listar archivos disponibles en assets
            try {
                val archivosRaiz = context.assets.list("")?.joinToString(", ") ?: "vacío"
                Log.d("SPIRO_DEBUG", "   📁 Archivos en assets/: $archivosRaiz")

                val archivosPets = context.assets.list("pets")?.joinToString(", ") ?: "vacío"
                Log.d("SPIRO_DEBUG", "   📁 Archivos en assets/pets/: $archivosPets")

                // Intentar listar subcarpeta de la mascota
                val carpeta = rutaAsset.substringBeforeLast("/")
                val subArchivos = context.assets.list(carpeta)?.joinToString(", ") ?: "vacío"
                Log.d("SPIRO_DEBUG", "   📁 Archivos en assets/$carpeta/: $subArchivos")
            } catch (e2: Exception) {
                Log.e("SPIRO_DEBUG", "   Error listando archivos: ${e2.message}")
            }
            false
        }
    }

    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        if (archivoExiste) {
            Log.d("SPIRO_DEBUG", "🖼️ Cargando imagen: $nombreEspecie desde $rutaCompleta")

            AsyncImage(
                model = rutaCompleta,
                contentDescription = nombreEspecie,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onLoading = {
                    Log.d("SPIRO_DEBUG", "   ⏳ Cargando imagen de $nombreEspecie...")
                },
                onSuccess = {
                    Log.d("SPIRO_DEBUG", "   ✅ Imagen cargada exitosamente: $nombreEspecie")
                },
                onError = { error ->
                    Log.e("SPIRO_DEBUG", "   ❌ Error al cargar imagen: $nombreEspecie")
                    Log.e("SPIRO_DEBUG", "   Error: ${error.result.throwable?.message}")
                }
            )
        } else {
            Log.w("SPIRO_DEBUG", "⚠️ Mostrando emoji fallback para: $nombreEspecie")
            Text(
                text = emojiFallback,
                fontSize = 48.sp
            )
        }
    }
}