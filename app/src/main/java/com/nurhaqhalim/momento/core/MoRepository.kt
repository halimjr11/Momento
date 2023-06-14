package com.nurhaqhalim.momento.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nurhaqhalim.momento.core.model.AddStoryResponse
import com.nurhaqhalim.momento.core.model.DetailResponse
import com.nurhaqhalim.momento.core.model.LoginRequest
import com.nurhaqhalim.momento.core.model.LoginResponse
import com.nurhaqhalim.momento.core.model.RegisterRequest
import com.nurhaqhalim.momento.core.model.RegisterResponse
import com.nurhaqhalim.momento.core.model.StoriesResponse
import com.nurhaqhalim.momento.core.services.ApiEndpoint
import com.nurhaqhalim.momento.model.StoryModel
import com.nurhaqhalim.momento.utils.DataMapper
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

class MoRepository(val api: ApiEndpoint) {
    private val listStory: MutableLiveData<List<StoryModel>> = MutableLiveData()
    val errorStory: MutableLiveData<Boolean> = MutableLiveData()
    val errorLogin: MutableLiveData<Boolean> = MutableLiveData()
    val errorDetail: MutableLiveData<Boolean> = MutableLiveData()
    val errorAddStory: MutableLiveData<Boolean> = MutableLiveData()
    val errorRegister: MutableLiveData<Boolean> = MutableLiveData()
    val errorConnection: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()
    private val addStoriesResponse: MutableLiveData<AddStoryResponse> = MutableLiveData()
    private val detailResponse: MutableLiveData<DetailResponse> = MutableLiveData()
    private val loginResponse: MutableLiveData<LoginResponse> = MutableLiveData()
    private val registerResponse: MutableLiveData<RegisterResponse> = MutableLiveData()
    fun fetchStories(token : String, page: Int, size: Int, location: Boolean? = null): LiveData<List<StoryModel>> {
        api.getStories(token, page, size, location).apply {
            enqueue(object : Callback<StoriesResponse> {
                override fun onResponse(
                    call: Call<StoriesResponse>,
                    response: Response<StoriesResponse>
                ) {
                    if (response.isSuccessful) {
                        listStory.value = DataMapper.listStoryToModel(response.body()!!.listStory)
                    } else {
                        errorStory.value = true
                    }
                }

                override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                    if (t is IOException) {
                        errorConnection.value = true
                    }
                }
            })
        }
        return listStory
    }

    fun fetchLogin(loginRequest: LoginRequest): LiveData<LoginResponse> {
        api.login(loginRequest).apply {
            enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        loginResponse.value = response.body()
                    } else {
                        errorLogin.value = true
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    if (t is IOException) {
                        errorConnection.value = true
                    } else {
                        errorLogin.value = true
                    }
                }
            })
        }
        return loginResponse
    }

    fun fetchRegister(registerRequest: RegisterRequest): LiveData<RegisterResponse> {
        api.register(registerRequest).apply {
            enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    if (response.isSuccessful) {
                        registerResponse.value = response.body()
                    } else {
                        errorRegister.value = true
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    if (t is IOException) {
                        errorConnection.value = true
                    }
                }
            })
        }
        return registerResponse
    }

    fun fetchAddStoryUser(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody
    ): LiveData<AddStoryResponse> {
        api.addStoryWithAuth(token, file, description, latitude, longitude).apply {
            enqueue(object : Callback<AddStoryResponse> {
                override fun onResponse(
                    call: Call<AddStoryResponse>,
                    response: Response<AddStoryResponse>
                ) {
                    if (response.isSuccessful) {
                        addStoriesResponse.value = response.body()
                    } else {
                        errorMessage.value = response.message()
                        errorAddStory.value = true
                    }
                }

                override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                    if (t is IOException) {
                        Timber.tag("error_upload").e(t)
                        errorMessage.value = t.message
                        errorConnection.value = true
                    }
                }
            })
        }
        return addStoriesResponse
    }

    fun fetchGetDetail(token: String, id: String): LiveData<DetailResponse> {
        api.getDetail(token, id).apply {
            enqueue(object : Callback<DetailResponse> {
                override fun onResponse(
                    call: Call<DetailResponse>,
                    response: Response<DetailResponse>
                ) {
                    if (response.isSuccessful) {
                        detailResponse.value = response.body()
                    } else {
                        errorDetail.value = true
                    }
                }

                override fun onFailure(call: Call<DetailResponse>, t: Throwable) {
                    if (t is IOException) {
                        errorConnection.value = true
                    }
                }

            })
        }
        return detailResponse
    }
}
