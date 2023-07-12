package com.nurhaqhalim.momento.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.test.core.app.ApplicationProvider
import com.nurhaqhalim.momento.DataDummy
import com.nurhaqhalim.momento.MainDispatcherRule
import com.nurhaqhalim.momento.core.MoRepository
import com.nurhaqhalim.momento.core.local.model.StoryEntity
import com.nurhaqhalim.momento.core.remote.model.StoriesResponse
import com.nurhaqhalim.momento.getOrAwaitValue
import com.nurhaqhalim.momento.utils.DataMapper
import com.nurhaqhalim.momento.view.adapter.MoPagingAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class MoViewModelTest {
    @Mock
    val context: Application = ApplicationProvider.getApplicationContext()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Mock
    private lateinit var moRepository: MoRepository

    @Test
    fun `when Get Stories Should Not Null and Return Data`() = runTest {
        val dummyStories = DataDummy.generateDummyStoryResponse()
        val data: PagingData<StoryEntity> = DummyPagingSource.snapshot(dummyStories)
        val expectedQuote = MutableLiveData<PagingData<StoryEntity>>()
        expectedQuote.value = data
        Mockito.`when`(moRepository.fetchPagingList()).thenReturn(expectedQuote)

        val viewModel = MoViewModel(context)
        val actualQuote: PagingData<StoryEntity> = viewModel.fetchPagingList().getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MoPagingAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(DataMapper.storyToEntity(dummyStories[0]), differ.snapshot()[0])
    }

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}

class DummyPagingSource : PagingSource<Int, LiveData<List<StoriesResponse.Story>>>() {
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