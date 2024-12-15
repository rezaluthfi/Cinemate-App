package com.example.cinemate.api

import com.example.cinemate.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("9YS2s/movies")
    fun getMovies(): Call<List<Map<String, Any>>>

    // GET All Users
    @GET("9YS2s/user")
    fun getUsers(): Call<List<User>>

    @POST("9YS2s/user")
    fun registerUser(@Body user: User): Call<User>

    // Mengubah password pengguna
    @FormUrlEncoded
    @POST("9YS2s/user/{id}")
    fun changePassword(
        @Path("id") userId: String,
        @Field("oldPassword") oldPassword: String,
        @Field("newPassword") newPassword: String
    ): Call<Void>

    // Memperbarui data pengguna
    @POST("9YS2s/user/{id}")
    fun updateUser(@Path("id") userId: String, @Body user: User): Call<User>

    // Menghapus pengguna
    @DELETE("9YS2s/user/{id}")
    fun deleteUser(@Path("id") userId: String): Call<Void>


}
