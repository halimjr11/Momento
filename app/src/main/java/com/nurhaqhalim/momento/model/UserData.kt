package com.nurhaqhalim.momento.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    val name: String,
    val token: String,
    val userId: String
) : Parcelable
