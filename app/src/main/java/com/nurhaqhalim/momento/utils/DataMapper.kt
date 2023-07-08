package com.nurhaqhalim.momento.utils

import com.nurhaqhalim.momento.core.local.model.StoryEntity
import com.nurhaqhalim.momento.core.remote.model.StoriesResponse
import com.nurhaqhalim.momento.model.StoryModel

object DataMapper {
    fun listStoryToModel(list: List<StoriesResponse.Story>): List<StoryModel> =
        list.map { story -> storyToModel(story) }.toList()

    fun listStoryToEntity(list: List<StoriesResponse.Story>): List<StoryEntity> =
        list.map { story -> storyToEntity(story) }.toList()

    fun storyToModel(story: StoriesResponse.Story): StoryModel = StoryModel(
        createdAt = story.createdAt,
        description = story.description,
        id = story.id,
        lat = story.lat,
        lon = story.lon,
        name = story.name,
        photoUrl = story.photoUrl
    )

    fun storyToEntity(story: StoriesResponse.Story): StoryEntity = StoryEntity(
        createdAt = story.createdAt,
        description = story.description,
        id = story.id,
        lat = story.lat,
        lon = story.lon,
        name = story.name,
        photoUrl = story.photoUrl
    )

    fun entityToStory(story: StoryEntity): StoryModel = StoryModel(
        createdAt = story.createdAt,
        description = story.description,
        id = story.id,
        lat = story.lat,
        lon = story.lon,
        name = story.name,
        photoUrl = story.photoUrl
    )
}