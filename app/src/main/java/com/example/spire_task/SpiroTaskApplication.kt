package com.example.spire_task

import android.app.Application
import android.content.Context
import androidx.room.RoomDatabase
import androidx.work.*
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.sync.AutoSyncWorker
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpiroTaskApplication : Application() {

    companion object {
        lateinit var instance: SpiroTaskApplication
            private set
    }

    lateinit var database: SpiroDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Inicializar base de datos
        database = SpiroDatabase.getInstance(this)

        // 🚀 Activamos el radar silencioso de cambios en las tablas locales
        configurarEscuchaCambiosBaseDatos()
    }

    private fun configurarEscuchaCambiosBaseDatos() {
        val prefs = getSharedPreferences("spiro_prefs", Context.MODE_PRIVATE)

        // Monitoreamos las tablas clave de Spiro Task
        val tablasAVigilar = arrayOf("perfil_usuario", "tablero", "tarea", "mascota_usuario", "mascota_base")

        database.invalidationTracker.addObserver(
            object : androidx.room.InvalidationTracker.Observer(tablasAVigilar) {
                override fun onInvalidated(tables: Set<String>) {
                    // 1. Verificar si el usuario activó el Switch de guardado automático
                    val autoSyncActivado = prefs.getBoolean("pref_auto_sync", false)

                    if (autoSyncActivado) {
                        // 2. Comprobar en segundo plano si la cuenta de Google está vinculada
                        CoroutineScope(Dispatchers.IO).launch {
                            val perfil = database.perfilUsuarioDao().obtenerPerfilDirecto()
                            val estaVinculadoAGoogle = perfil != null && !perfil.id_google.isNullOrBlank()

                            if (estaVinculadoAGoogle) {
                                // Todo listo -> Le pasamos el trabajo al sistema operativo
                                dispararSincronizacionSilenciosa()
                            }
                        }
                    }
                }
            }
        )
    }

    private fun dispararSincronizacionSilenciosa() {
        // 3. Condición obligatoria: El sistema operativo esperará a que haya internet
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitudSubida = OneTimeWorkRequestBuilder<AutoSyncWorker>()
            .setConstraints(restricciones)
            .build()

        // REPLACE asegura que si el usuario hace múltiples cambios rápidos,
        // no se sature el servidor; se acumulan y se envía la última foto de la BD.
        WorkManager.getInstance(this).enqueueUniqueWork(
            "SpiroAutoSyncUnique",
            ExistingWorkPolicy.REPLACE,
            solicitudSubida
        )
    }
}