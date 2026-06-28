package com.example.spire_task.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.spire_task.MainActivity

class SpiroNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDERS_ID = "spiro_task_daily_reminders"
        const val NOTIFICATION_ID_DAILY = 1001
    }

    init {
        crearCanalesDeNotificacion()
    }

    private fun crearCanalesDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios Diarios"
            val descriptionText = "Notificaciones para recordarte revisar tus tareas pendientes en Spiro Task"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_REMINDERS_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun lanzarRecordatorioDiario(titulo: String = "¡Hora de avanzar!", mensaje: String = "Revisa tus tareas en Spiro Task y mantén feliz a tu mascota.") {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Reemplazar por R.drawable.ic_notification si tienes uno propio
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_DAILY, builder.build())
    }
}