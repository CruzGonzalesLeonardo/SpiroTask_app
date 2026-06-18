package com.example.spire_task.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spire_task.SpiroTaskApplication
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.remote.auth.GoogleSignInManager

class OnboardingViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            val database = SpiroDatabase.getInstance(
                SpiroTaskApplication.instance
            )
            val googleSignInManager = GoogleSignInManager(context)
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(database, googleSignInManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}