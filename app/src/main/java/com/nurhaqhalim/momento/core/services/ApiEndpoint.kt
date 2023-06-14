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
import retrofit2.Call
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
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("register")
    fun register(@Body registerRequest: RegisterRequest): Call<RegisterResponse>

    @Multipart
    @POST("stories")
    fun addStoryWithAuth(
        @Header("Authorization") token : String,
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") latitude: RequestBody?,
        @Part("lon") longitude: RequestBody?
    ): Call<AddStoryResponse>

    @GET("stories")
    fun getStories(
        @Header("Authorization") token : String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Boolean? = false,
    ): Call<StoriesResponse>

    @GET("stories/{id}")
    fun getDetail(@Header("Authorization") token : String, @Path("id") id: String) : Call<DetailResponse>
}