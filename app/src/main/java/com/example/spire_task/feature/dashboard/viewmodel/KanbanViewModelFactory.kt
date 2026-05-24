package com.example.spire_task.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.spire_task.SpiroApplication

object KanbanViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SpiroApplication
            KanbanViewModel(taskRepository = app.taskRepository)
        }
    }
}