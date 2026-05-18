package com.example.spire_task.feature.dashboard.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatsScreen(
    totalTasks: Int = 0,
    completedTasks: Int = 0,
    totalXp: Float = 0f,
    level: Int = 1
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📈 Estadísticas", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tareas completadas: $completedTasks/$totalTasks")
                Text("XP total: ${totalXp.toInt()}")
                Text("Nivel actual: $level")
            }
        }
    }
}