package com.nurhaqhalim.momento.core

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.nurhaqhalim.momento.core.local.database.MoDatabase
import com.nurhaqhalim.momento.core.local.model.StoryEntity
import com.nurhaqhalim.momento.core.remote.model.AddStoryResponse
import com.nurhaqhalim.momento.core.remote.model.DetailResponse
import com.nurhaqhalim.momento.core.remote.model.LoginRequest
import com.nurhaqhalim.momento.core.remote.model.LoginResponse
import com.nurhaqhalim.momento.core.remote.model.RegisterRequest
import com.nurhaqhalim.momento.core.remote.model.RegisterResponse
import com.nurhaqhalim.momento.core.remote.services.ApiEndpoint
import com.nurhaqhalim.momento.model.StoryModel
import com.nurhaqhalim.momento.utils.DataMapper
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MoRepository(
    private val context: Context,
    private val api: ApiEndpoint,
    private val moDatabase: MoDatabase
) {

    val storyPagingList = MutableLiveData<PagingData<StoryEntity>>()

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

    @OptIn(ExperimentalPagingApi::class)
    fun fetchPagingList(): LiveData<PagingData<StoryEntity>> {
        return Pager(
            config = PagingConfig(
                enablePlaceholders = true,
                pageSize = 10
            ),
            remoteMediator = MoRemoteMediator(context, moDatabase, api),
            pagingSourceFactory = {
                moDatabase.moDao().retrieveAllStory()
            }
        ).liveData
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
