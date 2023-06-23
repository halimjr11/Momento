package com.nurhaqhalim.momento.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nurhaqhalim.momento.core.MoRepository
import com.nurhaqhalim.momento.core.Result
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
    private var addStoryResponse: MutableLiveData<Result<AddStoryResponse>> = MutableLiveData()
    private var loginResponse: MutableLiveData<Result<LoginResponse>> = MutableLiveData()
    private var registerResponse: MutableLiveData<Result<RegisterResponse>> = MutableLiveData()
    private var detailResponse: MutableLiveData<Result<DetailResponse>> = MutableLiveData()
    private var storyResponse: MutableLiveData<Result<List<StoryModel>>> = MutableLiveData()

    fun getAddStoryResponse() = addStoryResponse
    fun getLoginResponse() = loginResponse
    fun getRegisterResponse() = registerResponse
    fun getDetailResponse() = detailResponse
    fun getStoryResponse() = storyResponse
    fun fetchLogin(loginRequest: LoginRequest) {
        loginResponse.value = repository.fetchLogin(loginRequest)
    }

    fun fetchRegister(registerRequest: RegisterRequest) {
        registerResponse.value = repository.fetchRegister(registerRequest)
    }

    fun fetchStories(token: String, page: Int, size: Int, location: Int? = null) {
        storyResponse.value = repository.fetchStories(token, page, size, location)
    }

    fun fetchAddStoryUser(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody
    ) {
        addStoryResponse.value =
            repository.fetchAddStoryUser(token, file, description, latitude, longitude)
    }

    fun fetchGetDetail(token: String, id: String) {
        detailResponse.value = repository.fetchGetDetail(token, id)
    }
}