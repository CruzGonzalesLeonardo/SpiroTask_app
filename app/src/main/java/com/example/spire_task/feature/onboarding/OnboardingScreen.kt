package com.example.spire_task.feature.onboarding

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.R
import com.example.spire_task.data.remote.auth.GoogleSignInManager
import com.example.spire_task.feature.components.MascotaCard
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onOnboardingCompletado: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleSignInManager = remember { GoogleSignInManager(context) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            scope.launch {
                Log.d("ONBOARDING", "✅ Google Sign-In exitoso, procesando...")
                val googleResult = googleSignInManager.handleSignInResult(result.data)
                googleResult.onSuccess { googleAccount ->
                    Log.d("ONBOARDING", "✅ Google Account: ${googleAccount.email}, ID: ${googleAccount.id}")
                    val authResult = googleSignInManager.firebaseAuthWithGoogle(googleAccount)
                    authResult.onSuccess {
                        Log.d("ONBOARDING", "✅ Firebase Auth exitoso, llamando a crearPerfilConGoogle")
                        viewModel.crearPerfilConGoogle(googleAccount)
                    }.onFailure { exception ->
                        Log.e("ONBOARDING", "❌ Error en Firebase Auth: ${exception.message}")
                    }
                }.onFailure { exception ->
                    Log.e("ONBOARDING", "❌ Error en Google Sign-In: ${exception.message}")
                }
            }
        } else {
            Log.d("ONBOARDING", "❌ Google Sign-In cancelado o error, resultCode: ${result.resultCode}")
        }
    }

    LaunchedEffect(uiState.onboardingCompletado) {
        if (uiState.onboardingCompletado) {
            Log.d("ONBOARDING", "🎉 Onboarding completado, llamando a callback")
            onOnboardingCompletado()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = uiState.paso,
            transitionSpec = {
                if (targetState == PasoOnboarding.ELEGIR_MASCOTA) {
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                } else {
                    slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                }
            },
            label = "transicion_onboarding"
        ) { paso ->
            when (paso) {
                PasoOnboarding.NOMBRE -> PasoNombre(
                    nombre = uiState.nombre,
                    error = uiState.error,
                    estaCargando = uiState.estaCargando,
                    onNombreChange = viewModel::actualizarNombre,
                    onContinuar = viewModel::avanzarAElegirMascota,
                    onGoogleSignIn = {
                        Log.d("ONBOARDING", "🖱️ Botón Google Sign-In presionado")
                        val signInIntent = googleSignInManager.getSignInIntent()
                        googleSignInLauncher.launch(signInIntent)
                    }
                )
                PasoOnboarding.ELEGIR_MASCOTA -> PasoElegirMascota(
                    mascotas = uiState.mascotasDisponibles,
                    mascotaSeleccionada = uiState.mascotaSeleccionada,
                    monedasIniciales = uiState.monedasIniciales,
                    error = uiState.error,
                    estaCargando = uiState.estaCargando,
                    onSeleccionarMascota = viewModel::seleccionarMascota,
                    onConfirmar = viewModel::confirmarMascota,
                    onVolver = viewModel::volverANombre
                )
            }
        }
    }
}

@Composable
private fun PasoNombre(
    nombre: String,
    error: String?,
    estaCargando: Boolean,
    onNombreChange: (String) -> Unit,
    onContinuar: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logospiro),
            contentDescription = "Logo Spiro Task",
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡Bienvenid@!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu viaje comienza aquí",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = onNombreChange,
            label = { Text("¿Cómo te llamas?") },
            placeholder = { Text("Tu nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            isError = error != null,
            enabled = !estaCargando,
            supportingText = {
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !estaCargando,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continuar con Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Text(
                text = " o ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onContinuar,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = nombre.isNotBlank() && !estaCargando
        ) {
            if (estaCargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        IndicadorPasos(pasoActual = 1, totalPasos = 2)
    }
}

@Composable
private fun PasoElegirMascota(
    mascotas: List<com.example.spire_task.data.local.entidades.MascotaBaseEntity>,
    mascotaSeleccionada: com.example.spire_task.data.local.entidades.MascotaBaseEntity?,
    monedasIniciales: Int,
    error: String?,
    estaCargando: Boolean,
    onSeleccionarMascota: (com.example.spire_task.data.local.entidades.MascotaBaseEntity) -> Unit,
    onConfirmar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Elige a tu guardián",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💰", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tu saldo: $monedasIniciales monedas",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(mascotas) { mascota ->
                MascotaCard(
                    mascota = mascota,
                    estaSeleccionada = mascotaSeleccionada?.id_mascota_base == mascota.id_mascota_base,
                    monedasUsuario = monedasIniciales,
                    onClick = { onSeleccionarMascota(mascota) }
                )
            }
        }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (mascotaSeleccionada != null) {
            val monedasRestantes = monedasIniciales - mascotaSeleccionada.precio_monedas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Te quedarán:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$monedasRestantes 🪙",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("← Volver")
            }

            Button(
                onClick = onConfirmar,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = mascotaSeleccionada != null && !estaCargando
            ) {
                if (estaCargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Comenzar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        IndicadorPasos(pasoActual = 2, totalPasos = 2)
    }
}

@Composable
private fun IndicadorPasos(pasoActual: Int, totalPasos: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPasos) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (index + 1 == pasoActual) 10.dp else 8.dp)
                    .background(
                        color = if (index + 1 == pasoActual) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}