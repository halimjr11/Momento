package com.nurhaqhalim.momento.core.services

import com.nurhaqhalim.momento.core.model.AddStoryResponse
import com.nurhaqhalim.momento.core.model.DetailResponse
import com.nurhaqhalim.momento.core.model.LoginRequest
import com.nurhaqhalim.momento.core.model.LoginResponse
import com.nurhaqhalim.momento.core.model.RegisterRequest
import com.nurhaqhalim.momento.core.model.RegisterResponse
import com.nurhaqhalim.momento.core.model.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiEndpoint {

    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse

    @Multipart
    @POST("stories")
    suspend fun addStoryWithAuth(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") latitude: RequestBody?,
        @Part("lon") longitude: RequestBody?
    ): AddStoryResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = 0,
    ): StoriesResponse

    @GET("stories/{id}")
    suspend fun getDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): DetailResponse
}