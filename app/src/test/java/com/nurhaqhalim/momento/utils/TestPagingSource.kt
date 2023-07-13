package com.nurhaqhalim.momento.utils

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nurhaqhalim.momento.core.local.model.StoryEntity
import com.nurhaqhalim.momento.core.remote.model.StoriesResponse

class TestPagingSource : PagingSource<Int, LiveData<List<StoriesResponse.Story>>>() {
    companion object {
        fun snapshot(items: List<StoriesResponse.Story>): PagingData<StoryEntity> {
            return PagingData.from(DataMapper.listStoryToEntity(items))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<StoriesResponse.Story>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<StoriesResponse.Story>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}