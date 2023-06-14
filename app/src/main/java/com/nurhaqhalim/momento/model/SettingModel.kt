package com.nurhaqhalim.momento.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingModel(
    val icon : List<Int> = arrayListOf(),
    val title : List<Int> = arrayListOf()
) : Parcelable
