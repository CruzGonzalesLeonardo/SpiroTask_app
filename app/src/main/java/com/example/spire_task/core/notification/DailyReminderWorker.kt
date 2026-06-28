package com.example.spire_task.core.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Usamos tu administrador existente para lanzar la notificación
        val notificationManager = SpiroNotificationManager(applicationContext)
        notificationManager.lanzarRecordatorioDiario()

        return Result.success()
    }
}