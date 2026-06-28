package com.example.spire_task.feature.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Tarjeta Inmersiva de la Mascota y su Hábitat.
 * Renderiza el bioma al 100% de opacidad de fondo con la mascota al centro.
 * Cambia dinámicamente el asset visual si la felicidad es inferior al 50%.
 */
@Composable
fun MascotaHomeCard(
    emoji: String,
    nombreEspecie: String,
    nivel: Int,
    experiencia: Int,
    felicidad: Int = 100,
    nombrePersonalizado: String? = null,
    rutaAsset: String? = null,
    rutaAssetTriste: String? = null, // NUEVO: Ruta de la variante triste
    rutaHabitad: String? = null
) {
    val experienciaMaxima = nivel * 100
    val progresoXP = if (experienciaMaxima > 0) experiencia.toFloat() / experienciaMaxima else 0f

    // Lógica emocional: Cambia de asset base a asset triste según el porcentaje de felicidad
    val assetEmocionalOpcional = if (felicidad < 50 && rutaAssetTriste != null) {
        rutaAssetTriste
    } else {
        rutaAsset
    }

    val colorFelicidad = when {
        felicidad >= 70 -> Color(0xFF2ED573) // Verde Spiro Task
        felicidad >= 40 -> Color(0xFFFFA502) // Naranja de advertencia
        else -> Color(0xFFFF4757)            // Rojo crítico
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. Hábitat de fondo de la Mascota activa
            if (rutaHabitad != null) {
                HabitadFondoCard(
                    rutaHabitad = rutaHabitad,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Gradiente inferior para proteger la visibilidad del texto sobre cualquier imagen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 250f
                        )
                    )
            )

            // 3. Contenedor de elementos interactivos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Insignia de Nivel Superior
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Nivel $nivel",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Mascota ubicada en el centro del Hábitat (Usa el asset emocional seleccionado)
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarMascota(
                        emoji = emoji,
                        rutaAsset = assetEmocionalOpcional,
                        nombreEspecie = nombreEspecie
                    )
                }

                // Indicadores de Estado Inferiores (EXP y Felicidad)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = nombrePersonalizado ?: nombreEspecie,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = nombreEspecie,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Barra de Progreso de Felicidad
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mood,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Felicidad",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
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
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colorFelicidad,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }

                    // Barra de Progreso de Experiencia
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Experiencia",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Text(
                                text = "$experiencia / $experienciaMaxima",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progresoXP },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

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
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)
                )
            )
        )
    }
}

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
            true
        } catch (e: Exception) {
            false
        }
    }

    Box(
        modifier = Modifier
            .size(105.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.4f))
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
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(emoji, fontSize = 48.sp)
            }
        }
    }
}