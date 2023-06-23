package com.nurhaqhalim.momento.core

import com.nurhaqhalim.momento.core.model.AddStoryResponse
import com.nurhaqhalim.momento.core.model.DetailResponse
import com.nurhaqhalim.momento.core.model.LoginRequest
import com.nurhaqhalim.momento.core.model.LoginResponse
import com.nurhaqhalim.momento.core.model.RegisterRequest
import com.nurhaqhalim.momento.core.model.RegisterResponse
import com.nurhaqhalim.momento.core.services.ApiEndpoint
import com.nurhaqhalim.momento.model.StoryModel
import com.nurhaqhalim.momento.utils.DataMapper
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MoRepository(private val api: ApiEndpoint) {

    fun fetchStories(
        token: String,
        page: Int,
        size: Int,
        location: Int? = null
    ): Result<List<StoryModel>> {
        return runBlocking {
            Result.Loading
            try {
                val response = api.getStories(token, page, size, location)
                val stories = response.listStory
                val storyModel: List<StoryModel> = stories.map { story ->
                    DataMapper.storyToModel(story)
                }
                Result.Success(storyModel)
            } catch (e: Exception) {
                Result.Error(e.message.toString())
            }
        }
    }

    fun fetchLogin(loginRequest: LoginRequest): Result<LoginResponse> {
        return runBlocking {
            Result.Loading
            try {
                val response = api.login(loginRequest)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e.message.toString())
            }
        }
    }

    fun fetchRegister(registerRequest: RegisterRequest): Result<RegisterResponse> {
        return runBlocking {
            Result.Loading
            try {
                val response = api.register(registerRequest)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e.message.toString())
            }
        }
    }


    fun fetchAddStoryUser(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody
    ): Result<AddStoryResponse> {
        return runBlocking {
            Result.Loading
            try {
                val response = api.addStoryWithAuth(token, file, description, latitude, longitude)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e.message.toString())
            }
        }
    }

    fun fetchGetDetail(token: String, id: String): Result<DetailResponse> {
        return runBlocking {
            Result.Loading
            try {
                val response = api.getDetail(token, id)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e.message.toString())
            }
        }
    }
}
