package com.example.cinemate.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class PrefManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val PREF_NAME = "cinemate_pref"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_EMAIL = "email"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DOB = "dob" // Key untuk menyimpan tanggal lahir
        private const val KEY_PASSWORD = "password" // Key untuk menyimpan password
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveEmail(email: String) {
        editor.putString(KEY_EMAIL, email)
        editor.apply()
        Log.d("PrefManager", "Email saved: $email")
    }

    fun getEmail(): String? {
        val email = sharedPreferences.getString(KEY_EMAIL, null)
        Log.d("PrefManager", "Email retrieved: $email")
        return email
    }

    fun saveUsername(username: String) {
        editor.putString(KEY_USERNAME, username)
        editor.apply()
        Log.d("PrefManager", "Username saved: $username")
    }

    fun getUsername(): String? {
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        Log.d("PrefManager", "Username retrieved: $username")
        return username
    }

    fun saveUserId(userId: String) {
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
        Log.d("PrefManager", "User ID saved: $userId")
    }

    fun getUserId(): String? {
        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        Log.d("PrefManager", "User ID retrieved: $userId")
        return userId
    }

    fun saveDob(dob: String) { // Menyimpan tanggal lahir
        editor.putString(KEY_DOB, dob)
        editor.apply()
        Log.d("PrefManager", "DOB saved: $dob")
    }

    fun getDob(): String? { // Mengambil tanggal lahir
        val dob = sharedPreferences.getString(KEY_DOB, null)
        Log.d("PrefManager", "DOB retrieved: $dob")
        return dob
    }

    fun savePassword(password: String) { // Menyimpan password
        editor.putString(KEY_PASSWORD, password)
        editor.apply()
        Log.d("PrefManager", "Password saved: $password")
    }

    fun getPassword(): String? { // Mengambil password
        val password = sharedPreferences.getString(KEY_PASSWORD, null)
        Log.d("PrefManager", "Password retrieved: $password")
        return password
    }

    fun logout() {
        editor.clear()
        editor.apply()
        Log.d("PrefManager", "User logged out and preferences cleared")
    }
}
