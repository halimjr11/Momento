package com.nurhaqhalim.momento.core

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.core.local.database.MoDatabase
import com.nurhaqhalim.momento.core.local.model.RemoteKeys
import com.nurhaqhalim.momento.core.local.model.StoryEntity
import com.nurhaqhalim.momento.core.remote.services.ApiEndpoint
import com.nurhaqhalim.momento.utils.DataMapper
import com.nurhaqhalim.momento.utils.StorageHelper

@OptIn(ExperimentalPagingApi::class)
class MoRemoteMediator(
    private val context: Context,
    private val moDatabase: MoDatabase,
    private val moApi: ApiEndpoint
) : RemoteMediator<Int, StoryEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        val userData = StorageHelper.getUserData(context)
        val token =
            context.resources.getString(R.string.token_text).replace("%token%", userData.token)


        return try {
            val responseData = moApi.getStories(token, page, state.config.pageSize, 1)
            val endOfPaginationReached = responseData.listStory.isEmpty()
            moDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    moDatabase.moDao().deleteAllKeys()
                    moDatabase.moDao().deleteAll()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = responseData.listStory.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                moDatabase.moDao().insertAll(keys)
                moDatabase.moDao().insertStory(DataMapper.listStoryToEntity(responseData.listStory))
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            moDatabase.moDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            moDatabase.moDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                moDatabase.moDao().getRemoteKeysId(id)
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}