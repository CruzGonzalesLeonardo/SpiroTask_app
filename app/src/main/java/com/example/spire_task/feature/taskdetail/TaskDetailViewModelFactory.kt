package com.example.spire_task.feature.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spire_task.SpiroTaskApplication
import com.example.spire_task.data.local.database.SpiroDatabase

class TaskDetailViewModelFactory(
    private val tareaId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            val database = SpiroDatabase.getInstance(SpiroTaskApplication.instance)
            @Suppress("UNCHECKED_CAST")
            return TaskDetailViewModel(database, tareaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}