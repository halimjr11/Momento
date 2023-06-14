package com.nurhaqhalim.momento.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.nurhaqhalim.momento.core.MoRepository
import com.nurhaqhalim.momento.core.model.AddStoryResponse
import com.nurhaqhalim.momento.core.model.DetailResponse
import com.nurhaqhalim.momento.core.model.LoginRequest
import com.nurhaqhalim.momento.core.model.LoginResponse
import com.nurhaqhalim.momento.core.model.RegisterRequest
import com.nurhaqhalim.momento.core.model.RegisterResponse
import com.nurhaqhalim.momento.core.services.ApiEndpoint
import com.nurhaqhalim.momento.core.services.ApiServices
import com.nurhaqhalim.momento.model.StoryModel
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MoViewModel(application: Application) : AndroidViewModel(application) {
    private val api: ApiEndpoint =
        ApiServices.getInstance(application).create(ApiEndpoint::class.java)
    private val repository = MoRepository(api)
    val errorLogin: LiveData<Boolean> = repository.errorLogin
    val errorRegister: LiveData<Boolean> = repository.errorRegister
    val errorConnection: LiveData<Boolean> = repository.errorConnection
    val errorStory: LiveData<Boolean> = repository.errorStory
    val errorDetail: LiveData<Boolean> = repository.errorDetail
    val errorMessage: LiveData<String> = repository.errorMessage
    val errorAddStory: LiveData<Boolean> = repository.errorAddStory
    fun fetchLogin(loginRequest: LoginRequest): LiveData<LoginResponse> =
        repository.fetchLogin(loginRequest)

    fun fetchRegister(registerRequest: RegisterRequest): LiveData<RegisterResponse> =
        repository.fetchRegister(registerRequest)

    fun fetchStories(token: String, page: Int, size: Int, location: Boolean? = null): LiveData<List<StoryModel>> =
        repository.fetchStories(token, page, size, location)

    fun fetchAddStoryUser(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody
    ): LiveData<AddStoryResponse> = repository.fetchAddStoryUser(token, file, description, latitude, longitude)

    fun fetchGetDetail(token: String, id: String): LiveData<DetailResponse> = repository.fetchGetDetail(token, id)
}