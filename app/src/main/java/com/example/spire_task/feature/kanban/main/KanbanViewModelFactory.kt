package com.example.spire_task.feature.kanban.main

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.spire_task.SpiroApplication

object KanbanViewModelFactory {
    fun createFactory(userId: String): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SpiroApplication
            KanbanViewModel(
                taskRepository = app.taskRepository,
                userId = userId
            )
        }
    }
}