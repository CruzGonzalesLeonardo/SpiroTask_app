package com.example.spire_task.feature.kanban.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spire_task.feature.kanban.HabilidadCompleta
import com.example.spire_task.feature.kanban.MascotaCompletaKanban

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanHeader(
    tableroNombre: String,
    onVolver: () -> Unit,
    mascota: MascotaCompletaKanban?,
    habilidad: HabilidadCompleta?,
    felicidad: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
            }

            Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                Text(
                    text = tableroNombre,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (mascota != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${mascota.nombreEspecie} • Nivel ${mascota.nivel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = "Felicidad",
                                tint = androidx.compose.ui.graphics.Color(0xFFEF4444),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = " $felicidad%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Contenedor RPG para la Mascota Mentora usando tu lógica de Assets
            if (mascota != null) {
                Box(
                    modifier = Modifier.padding(end = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Reutilizamos tu lógica adaptada al tamaño del Header (54.dp)
                    val context = LocalContext.current
                    val rutaAsset = if (felicidad < 50 && !mascota.rutaAssetTriste.isNullOrBlank()) {
                        mascota.rutaAssetTriste
                    } else {
                        mascota.rutaAsset ?: ""
                    } // Mapeado a la propiedad de ruta de tu entidad
                    val rutaCompleta = "file:///android_asset/$rutaAsset"

                    val archivoExiste = remember(rutaAsset) {
                        if (rutaAsset.isBlank()) false else {
                            try {
                                val inputStream = context.assets.open(rutaAsset)
                                inputStream.close()
                                true
                            } catch (e: Exception) {
                                false
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (archivoExiste) {
                            AsyncImage(
                                model = rutaCompleta,
                                contentDescription = mascota.nombreEspecie,
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = mascota.emoji ?: "🐾",
                                fontSize = 28.sp
                            )
                        }
                    }

                    // Medalla flotante de habilidad activa
                    if (habilidad != null) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "Habilidad Activa",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}