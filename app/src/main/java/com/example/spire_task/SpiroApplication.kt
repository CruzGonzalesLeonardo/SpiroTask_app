package com.example.spire_task

import android.app.Application
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.repository.TaskRepositoryImpl
import com.example.spire_task.domain.repositories.ITaskRepository

class SpiroApplication : Application() {

    val database by lazy {
        SpiroDatabase.getDatabase(this)
    }

    val taskRepository: ITaskRepository by lazy {
        TaskRepositoryImpl(
            taskDao = database.taskDao(),
            columnDao = database.columnDao()
        )
    }
}