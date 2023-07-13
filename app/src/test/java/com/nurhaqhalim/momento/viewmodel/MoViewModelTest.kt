package com.nurhaqhalim.momento.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import com.nurhaqhalim.momento.core.MoRepository
import com.nurhaqhalim.momento.core.local.model.StoryEntity
import com.nurhaqhalim.momento.core.remote.model.StoriesResponse
import com.nurhaqhalim.momento.utils.DataDummy
import com.nurhaqhalim.momento.utils.DataMapper
import com.nurhaqhalim.momento.utils.MainDispatcherRule
import com.nurhaqhalim.momento.utils.TestPagingSource
import com.nurhaqhalim.momento.utils.getOrAwaitValue
import com.nurhaqhalim.momento.view.adapter.MoPagingAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MoViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var viewModel: MoViewModel
    private val emptyData = emptyList<StoriesResponse.Story>()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var repository: MoRepository

    @Before
    fun setUp() {
        viewModel = MoViewModel(repository)
    }

    @Test
    fun fetchStories_onSuccess_responseShouldReturnDataAndNotNull() = runTest {
        val dummyStories = DataDummy.generateDummyStoryResponse()
        val data: PagingData<StoryEntity> = TestPagingSource.snapshot(dummyStories)
        val story = MutableLiveData<PagingData<StoryEntity>>()
        story.value = data

        `when`(repository.fetchPagingList()).thenReturn(story)

        val actualQuote: PagingData<StoryEntity> = viewModel.fetchPagingList().getOrAwaitValue()

        verify(repository).fetchPagingList()
        val differ = AsyncPagingDataDiffer(
            diffCallback = MoPagingAdapter.DIFF_CALLBACK,
            updateCallback = DataDummy.noopListUpdateCallback,
            mainDispatcher = mainDispatcherRules.testDispatcher,
            workerDispatcher = mainDispatcherRules.testDispatcher,
        )
        differ.submitData(actualQuote)

        //validate not null
        assertNotNull(differ.snapshot())
        //validate data size
        assertEquals(dummyStories.size, differ.snapshot().size)
        //validate first data
        assertEquals(DataMapper.storyToEntity(dummyStories[0]), differ.snapshot()[0])
    }

    @Test
    fun fetchStories_onSuccess_responseReturnEmptyData() = runTest {
        val pagedData = TestPagingSource.snapshot(emptyData)
        val story = MutableLiveData<PagingData<StoryEntity>>()
        story.value = pagedData

        `when`(repository.fetchPagingList()).thenReturn(story)

        val actualStory = viewModel.fetchPagingList().getOrAwaitValue()

        verify(repository).fetchPagingList()
        val differ = AsyncPagingDataDiffer(
            diffCallback = MoPagingAdapter.DIFF_CALLBACK,
            updateCallback = DataDummy.noopListUpdateCallback,
            mainDispatcher = mainDispatcherRules.testDispatcher,
            workerDispatcher = mainDispatcherRules.testDispatcher,
        )
        differ.submitData(actualStory)
        val actualStorySnapshot = differ.snapshot()
        advanceUntilIdle()
        // validate data size 0
        assertEquals(0, actualStorySnapshot.size)
    }
}