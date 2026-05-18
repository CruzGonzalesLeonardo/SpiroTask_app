package com.example.spire_task.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("spire_prefs", MODE_PRIVATE)

    fun saveSession(userId: String, userName: String, email: String = "", authProvider: String = "local") {
        prefs.edit().apply {
            putString("user_id", userId)
            putString("user_name", userName)
            putString("user_email", email)
            putString("auth_provider", authProvider)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString("user_id", null)
    fun getUserName(): String? = prefs.getString("user_name", null)
    fun getUserEmail(): String? = prefs.getString("user_email", null)
    fun getAuthProvider(): String? = prefs.getString("auth_provider", null)
    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}