package com.example.cinemate.api

import com.example.cinemate.model.Movie
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MovieApi {
    @GET("9YS2s/movies")
    fun getMovies(): Call<List<Map<String, Any>>>

    @POST("9YS2s/movies")
    fun addMovies(@Body movies: List<Movie>): Call<List<Movie>> // Mengirim daftar film ke API

    @DELETE("9YS2s/movies/{id}")
    fun deleteMovie(@Path("id") id: String): Call<Void>
}
