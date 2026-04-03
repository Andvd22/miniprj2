package com.example.miniprj2.data


import android.content.Context
import android.content.SharedPreferences
import com.example.miniprj2.model.User

class SessionManager(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun login(user: User) {
        preferences.edit()
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_FULL_NAME, user.fullName)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun logout() {
        preferences.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = preferences.getBoolean(KEY_LOGGED_IN, false)

    fun getUserId(): Long? {
        val value = preferences.getLong(KEY_USER_ID, -1L)
        return if (value == -1L) null else value
    }

    fun getFullName(): String = preferences.getString(KEY_FULL_NAME, "") ?: ""

    fun getUsername(): String = preferences.getString(KEY_USERNAME, "") ?: ""

    companion object {
        private const val PREF_NAME = "fruit_app_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}