package com.example.spire_task

import android.app.Application
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.repository.TaskRepositoryImpl
import com.example.spire_task.domain.repositories.ITaskRepository
import com.google.firebase.FirebaseApp

class SpiroApplication : Application() {

    lateinit var taskRepository: ITaskRepository
        private set

    companion object {
        lateinit var instance: SpiroApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Inicializar base de datos
        val database = SpiroDatabase.getDatabase(this)

        // Inicializar repositorios
        taskRepository = TaskRepositoryImpl(
            taskDao = database.taskDao(),
            columnDao = database.columnDao()
        )
    }
}