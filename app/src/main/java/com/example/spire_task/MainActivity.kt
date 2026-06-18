package com.example.spire_task

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.spire_task.navigation.SpiroNavGraph
import com.example.spire_task.ui.theme.Spire_TaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Spire_TaskTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SpiroNavGraph()
                }
            }
        }
    }
}