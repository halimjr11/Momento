package com.nurhaqhalim.momento.core

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.core.remote.model.StoriesResponse
import com.nurhaqhalim.momento.core.remote.services.ApiEndpoint
import com.nurhaqhalim.momento.utils.StorageHelper

class MoPagingSource(private val context: Context, private val apiService: ApiEndpoint) :
    PagingSource<Int, StoriesResponse.Story>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoriesResponse.Story> {
        return try {
            val userData = StorageHelper.getUserData(context)
            val token =
                context.resources.getString(R.string.token_text).replace("%token%", userData.token)
            val page = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getStories(token, page, params.loadSize, 1)

            LoadResult.Page(
                data = responseData.listStory,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (responseData.listStory.isEmpty()) null else page + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoriesResponse.Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}