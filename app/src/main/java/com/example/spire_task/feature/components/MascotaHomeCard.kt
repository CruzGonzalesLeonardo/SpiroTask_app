package com.example.spire_task.feature.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Tarjeta que muestra la mascota activa del usuario en la pantalla Home.
 * Incluye el hábitat como fondo dentro de la tarjeta y barra de felicidad.
 */
@Composable
fun MascotaHomeCard(
    emoji: String,
    nombreEspecie: String,
    nivel: Int,
    experiencia: Int,
    felicidad: Int = 100,  // ✅ NUEVO: nivel de felicidad (0-100)
    nombrePersonalizado: String? = null,
    rutaAsset: String? = null,
    rutaHabitad: String? = null
) {
    val experienciaMaxima = nivel * 100
    val progresoXP = if (experienciaMaxima > 0) {
        experiencia.toFloat() / experienciaMaxima
    } else 0f

    val colorFelicidad = when {
        felicidad >= 70 -> Color(0xFF4CAF50)  // Verde
        felicidad >= 40 -> Color(0xFFFFC107)  // Amarillo
        else -> Color(0xFFF44336)              // Rojo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        )
    ) {
        // ✅ Fondo del hábitat dentro de la tarjeta
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Imagen del hábitat como fondo de la tarjeta
            if (rutaHabitad != null) {
                HabitadFondoCard(
                    rutaHabitad = rutaHabitad,
                    modifier = Modifier.matchParentSize()
                )
            }

            // Contenido de la tarjeta (encima del fondo)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Fila principal: avatar + información
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar circular
                    AvatarMascota(
                        emoji = emoji,
                        rutaAsset = rutaAsset,
                        nombreEspecie = nombreEspecie
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Información de la mascota
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nombrePersonalizado ?: nombreEspecie,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$nombreEspecie • Nivel $nivel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ✅ Barra de felicidad
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "😊 Felicidad",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$felicidad%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorFelicidad
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { felicidad / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = colorFelicidad,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de XP
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "⭐ XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$experiencia / $experienciaMaxima",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progresoXP },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Componente interno para el fondo del hábitat dentro de la tarjeta
 */
@Composable
private fun HabitadFondoCard(
    rutaHabitad: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val archivoExiste = remember(rutaHabitad) {
        try {
            context.assets.open(rutaHabitad).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    if (archivoExiste) {
        AsyncImage(
            model = "file:///android_asset/$rutaHabitad",
            contentDescription = "Hábitat",
            modifier = modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.3f)),
            contentScale = ContentScale.Crop,
            alpha = 0.4f  // Transparente para que se vea el contenido
        )
    }
}

/**
 * Avatar circular de la mascota.
 */
@Composable
private fun AvatarMascota(
    emoji: String,
    rutaAsset: String?,
    nombreEspecie: String
) {
    val context = LocalContext.current

    val archivoExiste = remember(rutaAsset) {
        if (rutaAsset == null) return@remember false
        try {
            context.assets.open(rutaAsset).close()
            Log.d("SPIRO_DEBUG", "✅ Avatar encontrado: $rutaAsset")
            true
        } catch (e: Exception) {
            Log.d("SPIRO_DEBUG", "⚠️ Avatar no encontrado: $rutaAsset")
            false
        }
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (archivoExiste && rutaAsset != null) {
            AsyncImage(
                model = "file:///android_asset/$rutaAsset",
                contentDescription = nombreEspecie,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onError = {
                    Log.e("SPIRO_DEBUG", "❌ Error al cargar avatar: $rutaAsset")
                }
            )
        } else {
            Text(emoji, fontSize = 40.sp)
        }
    }
}