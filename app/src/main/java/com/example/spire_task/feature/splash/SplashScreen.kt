package com.example.spire_task.feature.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = viewModel(
        factory = SplashViewModelFactory()
    )
) {
    var logoVisible by remember { mutableStateOf(false) }
    var textoVisible by remember { mutableStateOf(false) }

    val estadoSplash by viewModel.estado.collectAsState()

    // Navegar cuando el ViewModel lo indique
    LaunchedEffect(estadoSplash) {
        when (estadoSplash) {
            is SplashEstado.IrAOnboarding -> onNavigateToOnboarding()
            is SplashEstado.IrAHome -> onNavigateToHome()
            is SplashEstado.Cargando -> { /* Seguir mostrando splash */ }
        }
    }

    // Animación de entrada
    LaunchedEffect(Unit) {
        logoVisible = true
        delay(200)
        textoVisible = true
    }

    // Animaciones
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.3f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (textoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue = if (textoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "taglineAlpha"
    )

    // Contenedor principal - Adapta automáticamente el fondo de la paleta nueva (Claro/Oscuro)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // ─── LOGO (carga instantánea desde drawable) ───
            Box(
                modifier = Modifier
                    .size(150.dp) // Un toque sutil más grande para apreciar los detalles del logo
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logospiro),
                    contentDescription = "Logo Spiro Task",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── NOMBRE DE LA APP ───
            Text(
                text = "SPIRO TASK",
                // Aprovecha el displayMedium robustecido que agregamos a Type.kt
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 36.sp,
                    letterSpacing = 5.sp
                ),
                color = MaterialTheme.colorScheme.primary, // Violeta en claro / Violeta Neón en oscuro
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ─── LÍNEA DECORATIVA CON GRADIENTE (Púrpura a Cian) ───
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(3.dp)
                    .alpha(titleAlpha)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ─── TAGLINE ───
            Text(
                text = "Productividad con alma",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ─── INDICADOR DE CARGA ───
            Text(
                text = when (estadoSplash) {
                    is SplashEstado.Cargando -> "Preparando todo..."
                    else -> "Listo"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
    }
}