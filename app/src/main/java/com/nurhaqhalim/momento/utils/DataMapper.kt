package com.nurhaqhalim.momento.utils

import com.nurhaqhalim.momento.core.model.StoriesResponse
import com.nurhaqhalim.momento.model.StoryModel

object DataMapper {
    fun listStoryToModel(list: List<StoriesResponse.Story>): List<StoryModel> =
        list.map { story -> storyToModel(story) }.toList()

    fun storyToModel(story: StoriesResponse.Story): StoryModel = StoryModel(
        createdAt = story.createdAt,
        description = story.description,
        id = story.id,
        lat = story.lat,
        lon = story.lon,
        name = story.name,
        photoUrl = story.photoUrl
    )
}