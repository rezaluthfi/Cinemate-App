package com.example.cinemate.model

data class User(
    val _id: String? = null,
    val email: String = "", // Nilai default
    val username: String = "", // Nilai default
    val dob: String = "", // Nilai default
    val password: String = "",
    val newPassword: String? = null
)

