package com.nurhaqhalim.momento.utils

import androidx.recyclerview.widget.ListUpdateCallback
import com.nurhaqhalim.momento.core.remote.model.StoriesResponse

object DataDummy {
    fun generateDummyStoryResponse(): List<StoriesResponse.Story> {
        val items: MutableList<StoriesResponse.Story> = arrayListOf()
        for (i in 0..50) {
            val story = StoriesResponse.Story(
                "2023-07-09",
                "description $i",
                "id $i",
                -6.218574,
                106.823399,
                "name $i",
                "imageUrl"
            )
            items.add(story)
        }
        return items
    }

    val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}