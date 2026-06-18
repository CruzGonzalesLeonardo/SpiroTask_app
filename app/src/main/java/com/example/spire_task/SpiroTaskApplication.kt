package com.example.spire_task

import android.app.Application
import com.example.spire_task.data.local.database.SpiroDatabase
import com.google.firebase.FirebaseApp

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
    }
}