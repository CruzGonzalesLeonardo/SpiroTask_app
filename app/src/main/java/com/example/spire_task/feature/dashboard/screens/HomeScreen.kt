package com.example.spire_task.feature.dashboard.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class TareaPendiente(
    val id: String,
    val titulo: String
)

@Composable
fun HomeScreen(
    userName: String,
    level: Int = 1,
    xp: Float = 0f,
    xpNeeded: Int = 100,
    monedas: Int = 0,
    racha: Int = 0,
    tareasPendientes: List<TareaPendiente> = emptyList()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de bienvenida
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "¡Bienvenido, $userName!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Sigue así, ¡estás progresando!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Tarjeta de estadísticas rápidas
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 Tu progreso", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Nivel y XP
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐ Nivel $level")
                        Spacer(modifier = Modifier.weight(1f))
                        Text("🪙 $monedas monedas")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Barra de XP
                    LinearProgressIndicator(
                        progress = (xp / xpNeeded).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "XP: ${xp.toInt()}/$xpNeeded",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Racha
                    Text("🔥 Racha actual: $racha días")
                }
            }
        }

        // Tarjeta de tareas pendientes
        if (tareasPendientes.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📋 Tareas pendientes", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        tareasPendientes.forEach { tarea ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("☐ ", style = MaterialTheme.typography.bodyLarge)
                                Text(tarea.titulo, style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { /* Navegar a Kanban */ },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Ver todas →")
                        }
                    }
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📋 Tareas", style = MaterialTheme.typography.titleMedium)
                        Text("¡No hay tareas pendientes! 🎉")
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { /* Navegar a Kanban */ },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Crear tarea +")
                        }
                    }
                }
            }
        }

        // Tarjeta de mascota (resumen)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🐱 Tu mascota", style = MaterialTheme.typography.titleMedium)
                        Text("¡Está feliz!", style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(onClick = { /* Navegar a Mascota */ }) {
                        Text("Cuidar →")
                    }
                }
            }
        }
    }
}