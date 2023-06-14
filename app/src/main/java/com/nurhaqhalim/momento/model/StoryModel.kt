package com.nurhaqhalim.momento.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoryModel(
    val createdAt: String = "",
    val description: String = "",
    val id: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val name: String = "",
    val photoUrl: String = ""
) : Parcelable
