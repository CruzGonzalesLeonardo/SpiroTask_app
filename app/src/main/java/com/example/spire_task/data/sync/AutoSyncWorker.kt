package com.example.spire_task.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.spire_task.SpiroTaskApplication
import com.example.spire_task.data.local.database.SpiroDatabase

class AutoSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AUTO_SYNC", "🔄 Iniciando subida automática a la nube...")

        // Obtenemos la instancia de tu base de datos Room
        val database = SpiroTaskApplication.instance.database
        val syncManager = SyncManager(database)

        return try {
            // Reutilizamos tu metodo manual existente que ya mapea todo (incluyendo las rompecabezas)
            val resultado = syncManager.uploadAllData()

            if (resultado is SyncManager.SyncResult.Success) {
                Log.d("AUTO_SYNC", "✅ Sincronización automática exitosa")
                Result.success()
            } else {
                Log.w("AUTO_SYNC", "⚠️ Reintentando sincronización: ${(resultado as SyncManager.SyncResult.Error).message}")
                Result.retry() // Si falló temporalmente, WorkManager reintentará con retraso exponencial
            }
        } catch (e: Exception) {
            Log.e("AUTO_SYNC", "❌ Fallo crítico en AutoSyncWorker: ${e.message}")
            Result.failure()
        }
    }
}