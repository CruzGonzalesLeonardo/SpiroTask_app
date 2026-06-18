package com.example.spire_task.feature.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Botón de acción rápida usado en la pantalla Home.
 *
 * @param icono Icono Material Design a mostrar
 * @param texto Texto del botón
 * @param modifier Modificador opcional
 * @param esPrimario Si es true, usa color primario; si no, secundario
 * @param onClick Acción al presionar el botón
 */
@Composable
fun AccionRapidaButton(
    icono: ImageVector,
    texto: String,
    modifier: Modifier = Modifier,
    esPrimario: Boolean = false,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = if (esPrimario) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }
    ) {
        Icon(icono, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto, fontWeight = FontWeight.SemiBold)
    }
}