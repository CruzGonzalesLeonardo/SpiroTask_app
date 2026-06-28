package com.example.spire_task

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.spire_task.navigation.SpiroNavGraph
import com.example.spire_task.ui.theme.Spire_TaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Accedemos al almacenamiento local de preferencias de Spiro Task
            val prefs = remember {
                getSharedPreferences("spiro_prefs", Context.MODE_PRIVATE)
            }

            // 2. Tomamos el tema actual del sistema por si es la primera vez que abre la app
            val sistemaEstaEnOscuro = isSystemInDarkTheme()

            // 3. Creamos el estado reactivo que Compose observará
            val esModoOscuro = remember {
                mutableStateOf(prefs.getBoolean("pref_dark_mode", sistemaEstaEnOscuro))
            }

            // 4. Escuchamos en tiempo real cuando el Switch de la pantalla de ajustes se mueva
            DisposableEffect(prefs) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                    if (key == "pref_dark_mode") {
                        esModoOscuro.value = sharedPreferences.getBoolean("pref_dark_mode", sistemaEstaEnOscuro)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)

                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            // 5. Pasamos el valor dinámico (esModoOscuro.value) a tu tema personalizado
            Spire_TaskTheme(darkTheme = esModoOscuro.value) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SpiroNavGraph()
                }
            }
        }
    }
}