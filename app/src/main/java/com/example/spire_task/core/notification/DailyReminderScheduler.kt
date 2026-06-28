package com.example.spire_task.core.notification

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyReminderScheduler(private val context: Context) {

    fun programarRecordatorio() {
        // Configurar la hora objetivo (ejemplo: 9:00 AM)
        val horaObjetivo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val ahora = Calendar.getInstance()
        if (horaObjetivo.before(ahora)) {
            // Si ya pasaron las 9 AM de hoy, programamos para mañana
            horaObjetivo.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delayInicial = horaObjetivo.timeInMillis - ahora.timeInMillis

        // Creamos la petición de ejecución
        val solicitudTrabajo = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(delayInicial, TimeUnit.MILLISECONDS)
            .addTag("SPIRO_DAILY_REMINDER_TAG")
            .build()

        // Encolar de manera única para evitar alertas duplicadas
        WorkManager.getInstance(context).enqueueUniqueWork(
            "SpiroDailyReminderUnique",
            ExistingWorkPolicy.REPLACE,
            solicitudTrabajo
        )
    }

    fun cancelarRecordatorio() {
        WorkManager.getInstance(context).cancelAllWorkByTag("SPIRO_DAILY_REMINDER_TAG")
    }
}