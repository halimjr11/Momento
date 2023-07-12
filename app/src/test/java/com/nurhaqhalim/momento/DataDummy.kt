package com.nurhaqhalim.momento

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
}