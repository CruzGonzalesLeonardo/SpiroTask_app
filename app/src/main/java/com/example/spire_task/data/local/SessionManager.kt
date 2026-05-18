package com.example.spire_task.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("spire_prefs", MODE_PRIVATE)

    fun saveSession(userId: String, userName: String) {
        prefs.edit().apply {
            putString("user_id", userId)
            putString("user_name", userName)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString("user_id", null)

    fun getUserName(): String? = prefs.getString("user_name", null)

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}