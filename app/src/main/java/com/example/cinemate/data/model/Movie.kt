package com.example.cinemate.data.model

data class Movie(
    val _id: String? = null,
    val title: String,
    val description: String,
    val genre: String,
    val rating: Double,
    val duration: Int,
    val posterUrl: String,
    val isTrending: Boolean,
    val isLatest: Boolean,
    val cinemas: List<String>
)

